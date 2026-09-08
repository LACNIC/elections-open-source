package net.lacnic.elections.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Reads runtime configuration generated in WildFly's configuration directory. */
public final class ElectionsProperties {

	private static final String FILE_NAME = "elections.properties";
	private static volatile Properties properties;

	private ElectionsProperties() {
		throw new IllegalStateException("Utility class");
	}

	public static String get(String key) {
		return getProperties().getProperty(key);
	}

	private static Properties getProperties() {
		Properties cached = properties;
		if (cached != null) {
			return cached;
		}

		Properties loaded = new Properties();
		String configDir = System.getProperty("jboss.server.config.dir");
		if (configDir != null && !configDir.isBlank()) {
			Path path = Paths.get(configDir, FILE_NAME);
			if (Files.isRegularFile(path)) {
				try (InputStream input = new FileInputStream(path.toFile())) {
					loaded.load(input);
				} catch (IOException ignored) {
					// Missing runtime configuration is handled by the caller as unavailable.
				}
			}
		}
		properties = loaded;
		return loaded;
	}
}
