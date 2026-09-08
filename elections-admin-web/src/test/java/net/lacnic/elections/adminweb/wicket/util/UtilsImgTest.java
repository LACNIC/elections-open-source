package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

class UtilsImgTest {

	@Test
	void constructorThrowsToPreventInstantiation() throws Exception {
		Constructor<UtilsImg> constructor = UtilsImg.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(IllegalStateException.class, exception.getCause());
		assertEquals("Utility class", exception.getCause().getMessage());
	}

	@Test
	void extractExtensionReturnsJpgWhenInputIsBlank() {
		assertEquals("jpg", UtilsImg.extractExtension(null));
		assertEquals("jpg", UtilsImg.extractExtension(""));
		assertEquals("jpg", UtilsImg.extractExtension("   "));
	}

	@Test
	void extractExtensionSupportsUnixAndWindowsPaths() {
		assertEquals("jpeg", UtilsImg.extractExtension("/tmp/folder/photo.JPEG"));
		assertEquals("gif", UtilsImg.extractExtension("C:\\tmp\\folder\\photo.GIF"));
	}

	@Test
	void extractExtensionReturnsLowercaseExtensionFromSimpleFileName() {
		assertEquals("png", UtilsImg.extractExtension("candidate.PNG"));
	}

	@Test
	void extractExtensionReturnsJpgWhenFileNameHasNoUsableExtension() {
		assertEquals("jpg", UtilsImg.extractExtension("candidate"));
		assertEquals("jpg", UtilsImg.extractExtension("candidate."));
		assertEquals("jpg", UtilsImg.extractExtension("/tmp/folder/"));
	}
}
