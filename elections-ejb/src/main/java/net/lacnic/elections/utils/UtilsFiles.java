package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UtilsFiles {
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	private UtilsFiles() {
		throw new IllegalStateException("Utility class");
	}

	public static Object[] getDefaultPhoto() throws IOException {
		Object[] o = new Object[3];
		String rutaFoto = System.getProperty("jboss.server.config.dir").concat("/templates/foto_candidato_default.jpg");
		File file = new File(rutaFoto);
		o[0] = getBytesFromFile(file);
		o[1] = "foto_candidato_default";
		o[2] = ".jpg";
		return o;
	}
	
	public static Object[] getDefaultPhoto(String filePath) throws IOException {
		Object[] o = new Object[3];
		String rutaFoto = filePath.concat("/image/foto_candidato_default.jpg");
		File file = new File(rutaFoto);
		o[0] = getBytesFromFile(file);
		o[1] = "foto_candidato_default";
		o[2] = ".jpg";
		return o;
	}

	public static File getPadronElectoralEjemplo() {
		String rutaPadronElectoralEjemplo = System.getProperty("jboss.server.config.dir").concat("/templates/padron_electoral_ejemplo.xls");
		return new File(rutaPadronElectoralEjemplo);
		
	}
	
	public static File getPadronElectoralEjemplo(String filePath) {
		String rutaPadronElectoralEjemplo = filePath.concat("/static/padron_electoral_ejemplo.xls");
		return new File(rutaPadronElectoralEjemplo);
		
	}
	
	public static File getEleccionesRolesFuncionamientoRevision(String filePath) {
		String rutaEleccionesRolesFuncRev = filePath.concat("/static/EleccionesRolesFuncionamientoRevision.pdf");
		return new File(rutaEleccionesRolesFuncRev);
		
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
			// Close the input stream and return bytes
			is.close();
		}
	}

	public static File convertirArrayBytesAFile(byte[] bytes, String ruta) {

		// convert array of bytes into file
		try (
			FileOutputStream fileOuputStream = new FileOutputStream(ruta);
		){		
			
			fileOuputStream.write(bytes);
			fileOuputStream.close();

			return new File(ruta);
		} catch (FileNotFoundException e) {			
			appLogger.error(e);
		} catch (IOException e) {
			
			appLogger.error(e);
		}
		return null;
	}
}
