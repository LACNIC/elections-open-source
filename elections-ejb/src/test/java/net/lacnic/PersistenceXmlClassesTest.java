package net.lacnic;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

 class PersistenceXmlClassesTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testAllPersistenceXmlClassesExist() throws Exception {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/persistence.xml")) {
			assertNotNull("No se encontro META-INF/persistence.xml en el classpath de test", inputStream);

			String xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			Matcher matcher = Pattern.compile("<class>([^<]+)</class>").matcher(xml);

			while (matcher.find()) {
				String className = matcher.group(1).trim();
				try {
					Class.forName(className);
				} catch (ClassNotFoundException e) {
					fail("Clase listada en persistence.xml no encontrada: " + className);
				}
			}
		}
	}
}
