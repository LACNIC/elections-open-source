package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "I46_POSTGRES_TEST_URL", matches = ".+")
class CandidateSupportAggregateLockPostgresIntegrationTest {

	private static final long CANDIDATE_ID = 1L;
	private static final int USER_SUPPORTS_5_REQUIRED = 5;

	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private String schema;

	@BeforeEach
	void setUp() throws Exception {
		jdbcUrl = System.getenv("I46_POSTGRES_TEST_URL");
		jdbcUser = environmentOrDefault("I46_POSTGRES_TEST_USER", "postgres");
		jdbcPassword = environmentOrDefault("I46_POSTGRES_TEST_PASSWORD", "postgres");
		schema = "i46_support_lock_" + UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
		try (Connection connection = connection(); Statement statement = connection.createStatement()) {
			statement.execute("CREATE SCHEMA " + schema);
			statement.execute("CREATE TABLE " + schema + ".candidate (id BIGINT PRIMARY KEY)");
			statement.execute("CREATE TABLE " + schema + ".support_nomination (id BIGINT PRIMARY KEY, candidate_id BIGINT NOT NULL REFERENCES " + schema + ".candidate(id), status VARCHAR(32) NOT NULL)");
			statement.execute("CREATE TABLE " + schema + ".task_progress (candidate_id BIGINT PRIMARY KEY REFERENCES " + schema + ".candidate(id), status VARCHAR(32) NOT NULL)");
			statement.execute("INSERT INTO " + schema + ".candidate(id) VALUES (1)");
			statement.execute("INSERT INTO " + schema + ".task_progress(candidate_id, status) VALUES (1, 'COMPLETED')");
			statement.execute("INSERT INTO " + schema + ".support_nomination(id, candidate_id, status) SELECT value, 1, 'ACCEPTED' FROM generate_series(1, 6) value");
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		if (schema == null) {
			return;
		}
		try (Connection connection = connection(); Statement statement = connection.createStatement()) {
			statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
		}
	}

	@Test
	void shouldSerializeRejectionsOnDifferentRowsAndReopenFiveSupportTask() throws Exception {
		runConcurrently(() -> changeSupportStatusAndReconcile(1L, "REJECTED"), () -> changeSupportStatusAndReconcile(2L, "REJECTED"));

		assertEquals(4, acceptedSupports());
		assertEquals("STARTED", taskStatus());
	}

	@Test
	void shouldSerializeRejectionAgainstAcceptanceAndKeepTaskConsistent() throws Exception {
		try (Connection connection = connection(); Statement statement = connection.createStatement()) {
			statement.execute("UPDATE " + schema + ".support_nomination SET status = 'ACCEPTED' WHERE id <= 5");
			statement.execute("UPDATE " + schema + ".support_nomination SET status = 'PROPOSED' WHERE id = 6");
			statement.execute("UPDATE " + schema + ".task_progress SET status = 'COMPLETED'");
		}

		runConcurrently(() -> changeSupportStatusAndReconcile(1L, "REJECTED"), () -> changeSupportStatusAndReconcile(6L, "ACCEPTED"));

		assertEquals(5, acceptedSupports());
		assertEquals("COMPLETED", taskStatus());
	}

	@Test
	void shouldSerializeRejectedSupportReactivationsAndRespectFiveSupportCapacity() throws Exception {
		try (Connection connection = connection(); Statement statement = connection.createStatement()) {
			statement.execute("UPDATE " + schema + ".support_nomination SET status = 'ACCEPTED' WHERE id <= 4");
			statement.execute("UPDATE " + schema + ".support_nomination SET status = 'REJECTED' WHERE id > 4");
			statement.execute("UPDATE " + schema + ".task_progress SET status = 'STARTED'");
		}

		runConcurrently(() -> reactivateSupportIfCapacityAvailable(5L), () -> reactivateSupportIfCapacityAvailable(6L));

		assertEquals(5, acceptedSupports());
		assertEquals("COMPLETED", taskStatus());
	}

	private void runConcurrently(SqlOperation first, SqlOperation second) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try {
			Future<Void> firstResult = executor.submit(() -> executeWhenReleased(first, ready, start));
			Future<Void> secondResult = executor.submit(() -> executeWhenReleased(second, ready, start));
			ready.await();
			start.countDown();
			firstResult.get();
			secondResult.get();
		} finally {
			executor.shutdownNow();
		}
	}

	private Void executeWhenReleased(SqlOperation operation, CountDownLatch ready, CountDownLatch start) throws Exception {
		ready.countDown();
		start.await();
		operation.execute();
		return null;
	}

	private void changeSupportStatusAndReconcile(long supportId, String newStatus) throws Exception {
		try (Connection connection = connection()) {
			connection.setAutoCommit(false);
			try {
				try (Statement statement = connection.createStatement()) {
					statement.execute("SET LOCAL lock_timeout = '5s'");
				}
				lockCandidateAggregate(connection);
				Thread.sleep(200L);
				try (PreparedStatement updateSupport = connection.prepareStatement("UPDATE " + schema + ".support_nomination SET status = ? WHERE id = ?")) {
					updateSupport.setString(1, newStatus);
					updateSupport.setLong(2, supportId);
					assertEquals(1, updateSupport.executeUpdate());
				}
				int acceptedSupports = acceptedSupports(connection);
				try (PreparedStatement lockTask = connection.prepareStatement("SELECT status FROM " + schema + ".task_progress WHERE candidate_id = ? FOR UPDATE")) {
					lockTask.setLong(1, CANDIDATE_ID);
					try (ResultSet result = lockTask.executeQuery()) {
						result.next();
					}
				}
				String reconciledStatus = acceptedSupports < USER_SUPPORTS_5_REQUIRED ? "STARTED" : "COMPLETED";
				try (PreparedStatement updateTask = connection.prepareStatement("UPDATE " + schema + ".task_progress SET status = ? WHERE candidate_id = ?")) {
					updateTask.setString(1, reconciledStatus);
					updateTask.setLong(2, CANDIDATE_ID);
					assertEquals(1, updateTask.executeUpdate());
				}
				connection.commit();
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}
	}

	private void reactivateSupportIfCapacityAvailable(long supportId) throws Exception {
		try (Connection connection = connection()) {
			connection.setAutoCommit(false);
			try {
				try (Statement statement = connection.createStatement()) {
					statement.execute("SET LOCAL lock_timeout = '5s'");
				}
				lockCandidateAggregate(connection);
				Thread.sleep(200L);
				if (acceptedSupports(connection) < USER_SUPPORTS_5_REQUIRED) {
					try (PreparedStatement updateSupport = connection.prepareStatement(
							"UPDATE " + schema + ".support_nomination SET status = 'ACCEPTED' WHERE id = ? AND status = 'REJECTED'")) {
						updateSupport.setLong(1, supportId);
						assertEquals(1, updateSupport.executeUpdate());
					}
				}
				String reconciledStatus = acceptedSupports(connection) < USER_SUPPORTS_5_REQUIRED ? "STARTED" : "COMPLETED";
				try (PreparedStatement lockTask = connection.prepareStatement(
						"SELECT status FROM " + schema + ".task_progress WHERE candidate_id = ? FOR UPDATE")) {
					lockTask.setLong(1, CANDIDATE_ID);
					try (ResultSet result = lockTask.executeQuery()) {
						result.next();
					}
				}
				try (PreparedStatement updateTask = connection.prepareStatement(
						"UPDATE " + schema + ".task_progress SET status = ? WHERE candidate_id = ?")) {
					updateTask.setString(1, reconciledStatus);
					updateTask.setLong(2, CANDIDATE_ID);
					assertEquals(1, updateTask.executeUpdate());
				}
				connection.commit();
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}
	}

	private void lockCandidateAggregate(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM " + schema + ".candidate WHERE id = ? FOR UPDATE")) {
			statement.setLong(1, CANDIDATE_ID);
			try (ResultSet result = statement.executeQuery()) {
				result.next();
			}
		}
	}

	private int acceptedSupports() throws SQLException {
		try (Connection connection = connection()) {
			return acceptedSupports(connection);
		}
	}

	private int acceptedSupports(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + schema + ".support_nomination WHERE candidate_id = ? AND status = 'ACCEPTED'")) {
			statement.setLong(1, CANDIDATE_ID);
			try (ResultSet result = statement.executeQuery()) {
				result.next();
				return result.getInt(1);
			}
		}
	}

	private String taskStatus() throws SQLException {
		try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement("SELECT status FROM " + schema + ".task_progress WHERE candidate_id = ?")) {
			statement.setLong(1, CANDIDATE_ID);
			try (ResultSet result = statement.executeQuery()) {
				result.next();
				return result.getString(1);
			}
		}
	}

	private Connection connection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
	}

	private String environmentOrDefault(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
	}

	@FunctionalInterface
	private interface SqlOperation {
		void execute() throws Exception;
	}
}
