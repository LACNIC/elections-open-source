package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class ImageResourceTest {

	@Test
	void constructorWithByteArrayUsesDefaultFormatAndReturnsSameData() {
		byte[] imageBytes = { 1, 2, 3, 4 };
		ExposedImageResource resource = new ExposedImageResource(imageBytes);

		assertEquals("jpg", resource.getFormat());
		assertArrayEquals(imageBytes, resource.readImageData());
	}

	@Test
	void constructorWithByteArrayAndCustomFormatUsesProvidedFormat() {
		byte[] imageBytes = { 9, 8, 7 };
		ExposedImageResource resource = new ExposedImageResource(imageBytes, "png");

		assertEquals("png", resource.getFormat());
		assertArrayEquals(imageBytes, resource.readImageData());
	}

	@Test
	void getImageDataReturnsEmptyArrayWhenImageIsNull() {
		ExposedImageResource resource = new ExposedImageResource((byte[]) null);
		assertEquals(0, resource.readImageData().length);
	}

	@Test
	void constructorWithBufferedImageConvertsImageToBinaryData() {
		BufferedImage bufferedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		bufferedImage.setRGB(0, 0, 0x00FF00);

		ExposedImageResource resource = new ExposedImageResource(bufferedImage);
		assertTrue(resource.readImageData().length > 0);
	}

	private static final class ExposedImageResource extends ImageResource {
		private static final long serialVersionUID = 1L;

		private ExposedImageResource(byte[] image) {
			super(image);
		}

		private ExposedImageResource(byte[] image, String format) {
			super(image, format);
		}

		private ExposedImageResource(BufferedImage image) {
			super(image);
		}

		private byte[] readImageData() {
			return super.getImageData(null);
		}
	}
}
