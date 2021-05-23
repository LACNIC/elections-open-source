package net.lacnic.elections.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase utilizada para el manejo de archivos, lectura y escritura
 * @author Antonymous
 *
 */
public class FileUtils {

	private FileUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String readFileAsString(String path, String file) throws java.io.IOException {
		byte[] buffer = new byte[(int) new File(path, file).length()];
		int count = 0;
		try (
				BufferedInputStream f = new BufferedInputStream(new FileInputStream(new File(path, file).toString()));
				) {			
			count = f.read(buffer);
		} catch ( java.io.IOException e) {
			throw e;
		}
		return new String(buffer);
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		try {
			// Get the size of the file
			long length = file.length();

			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}

			return bytes;
		} finally {
			is.close();
		}
	}

	public static byte[] getBytesFromFile(String ruta) throws IOException {
		return getBytesFromFile(new File(ruta));
	}

}
