package net.lacnic.elections.adminweb.wicket.util;

import java.awt.image.BufferedImage;

import org.apache.wicket.request.resource.DynamicImageResource; 


public class ImageResource extends DynamicImageResource {

	private static final long serialVersionUID = 5728654383103623879L;

	private byte[] image;

	public ImageResource(byte[] image) {
		this(image, "jpg");
	}

	public ImageResource(byte[] image, String format) {
		this.image = image;
		setFormat(format);
	}

	public ImageResource(BufferedImage image) {
		this.image = toImageData(image);
	}

	@Override
	protected byte[] getImageData(Attributes arg0) {
		if (image != null) {
			return image;
		} else {
			return new byte[0];
		}

	}

}
