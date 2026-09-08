package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.ElectionsVoterEJB;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.ejb.commons.MailsSendingEJB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesUtilsAndEjbFactoryTest {

	private String originalTempUri;
	private String originalConfUri;
	private Object originalFactoryInstance;

	@BeforeEach
	void captureEjbFactoryStatics() throws Exception {
		originalTempUri = (String) getStaticField(EJBFactory.class, "JBOSSTEMPURI");
		originalConfUri = (String) getStaticField(EJBFactory.class, "JBOSSCONFURI");
		originalFactoryInstance = getStaticField(EJBFactory.class, "instance");
	}

	@AfterEach
	void restoreEjbFactoryStatics() throws Exception {
		setStaticField(EJBFactory.class, "JBOSSTEMPURI", originalTempUri);
		setStaticField(EJBFactory.class, "JBOSSCONFURI", originalConfUri);
		setStaticField(EJBFactory.class, "instance", originalFactoryInstance);
	}

	@Test
	void jbossUrisShouldSupportConfiguredAndNullValues() throws Exception {
		setStaticField(EJBFactory.class, "JBOSSTEMPURI", "/tmp/ejb-temp");
		setStaticField(EJBFactory.class, "JBOSSCONFURI", "/tmp/ejb-conf");

		assertEquals("/tmp/ejb-temp/", EJBFactory.getJbossTempUri());
		assertEquals("/tmp/ejb-conf/", EJBFactory.getJbossConfUri());

		setStaticField(EJBFactory.class, "JBOSSCONFURI", null);
		assertEquals("", EJBFactory.getJbossConfUri());
	}

	@Test
	void ejbFactoryShouldBehaveAsSingletonAndExposeAllAccessors() throws Exception {
		setStaticField(EJBFactory.class, "instance", null);
		EJBFactory first = EJBFactory.getInstance();
		EJBFactory second = EJBFactory.getInstance();

		assertNotNull(first);
		assertSame(first, second);

		ElectionsManagerEJB manager = mock(ElectionsManagerEJB.class);
		ElectionsMonitorEJB monitor = mock(ElectionsMonitorEJB.class);
		ElectionsVoterEJB voter = mock(ElectionsVoterEJB.class);
		ElectionsParametersEJB parameters = mock(ElectionsParametersEJB.class);
		MailsSendingEJB mails = mock(MailsSendingEJB.class);

		first.setElectionsManagerEJB(manager);
		first.setElectionsMonitorEJB(monitor);
		first.setElectionsVoterEJB(voter);
		first.setElectionsParametersEJB(parameters);
		first.setMailsSendingEJB(mails);

		assertSame(manager, first.getElectionsManagerEJB());
		assertSame(monitor, first.getElectionsMonitorEJB());
		assertSame(voter, first.getElectionsVoterEJB());
		assertSame(parameters, first.getElectionsParametersEJB());
		assertSame(mails, first.getMailsSendingEJB());
	}

	@Test
	void defaultPhotosShouldLoadFromBundledResources() throws Exception {
		Object[] candidate = FilesUtils.getDefaultPhoto(null);
		Object[] abstention = FilesUtils.getDefaultAbstentionPhoto(null);

		assertInstanceOf(byte[].class, candidate[0]);
		assertTrue(((byte[]) candidate[0]).length > 0);
		assertEquals("default_candidate_photo.jpg", candidate[1]);
		assertEquals("jpg", candidate[2]);

		assertInstanceOf(byte[].class, abstention[0]);
		assertTrue(((byte[]) abstention[0]).length > 0);
		assertEquals("default_abstention_photo.jpg", abstention[1]);
		assertEquals("jpg", abstention[2]);
	}

	@Test
	void serverExampleResolutionShouldHonorFallbackOrder(@TempDir Path tempDir) throws Exception {
		Path confDir = Files.createDirectories(tempDir.resolve("conf"));
		Path tempServerDir = Files.createDirectories(tempDir.resolve("tmp"));
		Path legacyDir = Files.createDirectories(confDir.resolve("milacnic"));
		String fileName = "exampleDebtors.xlsx";

		setStaticField(EJBFactory.class, "JBOSSCONFURI", confDir.toString());
		setStaticField(EJBFactory.class, "JBOSSTEMPURI", tempServerDir.toString());

		Path confFile = confDir.resolve(fileName);
		Files.write(confFile, "conf".getBytes(StandardCharsets.UTF_8));
		File resolved = FilesUtils.getJbossTempOrganizationsDebtorsExample();
		assertEquals(confFile.toFile().getAbsolutePath(), resolved.getAbsolutePath());

		Files.delete(confFile);
		Path legacyFile = legacyDir.resolve(fileName);
		Files.write(legacyFile, "legacy".getBytes(StandardCharsets.UTF_8));
		resolved = FilesUtils.getJbossTempOrganizationsDebtorsExample();
		assertEquals(legacyFile.toFile().getAbsolutePath(), resolved.getAbsolutePath());

		Files.delete(legacyFile);
		Path tempFile = tempServerDir.resolve(fileName);
		Files.write(tempFile, "tmp".getBytes(StandardCharsets.UTF_8));
		resolved = FilesUtils.getJbossTempOrganizationsDebtorsExample();
		assertEquals(tempFile.toFile().getAbsolutePath(), resolved.getAbsolutePath());

		Files.delete(tempFile);
		resolved = FilesUtils.getJbossTempOrganizationsDebtorsExample();
		assertEquals(confFile.toFile().getAbsolutePath(), resolved.getAbsolutePath());

		File censusByOrg = FilesUtils.getJbossTempCensusExample(true);
		File censusByEmail = FilesUtils.getJbossTempCensusExample(false);
		File organizationsUpsert = FilesUtils.getJbossTempOrganizationsUpsertExample();
		File organizationsDelete = FilesUtils.getJbossTempOrganizationsDeleteExample();

		assertEquals(confDir.resolve("exampleCensusOrgId.xlsx").toFile().getAbsolutePath(), censusByOrg.getAbsolutePath());
		assertEquals(confDir.resolve("exampleCensusEmail.xlsx").toFile().getAbsolutePath(), censusByEmail.getAbsolutePath());
		assertEquals(confDir.resolve("exampleAddUpdateOrgs.xlsx").toFile().getAbsolutePath(), organizationsUpsert.getAbsolutePath());
		assertEquals(confDir.resolve("exampleDeleteOrgs.xlsx").toFile().getAbsolutePath(), organizationsDelete.getAbsolutePath());
	}

	@Test
	void filesHelpersShouldCreateResolveAndReadFiles(@TempDir Path tempDir) throws Exception {
		File censusExample = FilesUtils.getCensusExample("/opt/elections");
		File electionRoles = FilesUtils.getElectionRolesRevisionDocument("/opt/elections");
		assertTrue(censusExample.getPath().endsWith("/static/padron_electoral_ejemplo.xls"));
		assertTrue(electionRoles.getPath().endsWith("/static/EleccionesRolesFuncionamientoRevision.pdf"));

		byte[] sourceData = "hola".getBytes(StandardCharsets.UTF_8);
		Path sourceFile = tempDir.resolve("source.bin");
		Files.write(sourceFile, sourceData);

		assertArrayEquals(sourceData, FilesUtils.getBytesFromFile(sourceFile.toFile()));
		assertArrayEquals(sourceData, FilesUtils.getBytesFromFile(sourceFile.toString()));

		Path outputFile = tempDir.resolve("output.bin");
		File converted = FilesUtils.convertBytesArrayToFile(sourceData, outputFile.toString());
		assertNotNull(converted);
		assertTrue(converted.exists());
		assertArrayEquals(sourceData, Files.readAllBytes(outputFile));

		Path invalidPath = tempDir.resolve("missing").resolve("folder").resolve("out.bin");
		assertNull(FilesUtils.convertBytesArrayToFile(sourceData, invalidPath.toString()));
	}

	private static void setStaticField(Class<?> type, String name, Object value) throws Exception {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		field.set(null, value);
	}

	private static Object getStaticField(Class<?> type, String name) throws Exception {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}
}
