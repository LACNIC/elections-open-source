package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class FilesUtils {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");


	private FilesUtils() {
		throw new IllegalStateException("Utility class");
	}


	public static Properties getEmailProperties() {
		Properties props = System.getProperties();
		try (InputStream input = new FileInputStream(System.getProperty("jboss.server.config.dir").concat("/email.properties"))) {
			props.load(input);
		} catch (Exception ex) {
		}
		return props;
	}

	public static Object[] getDefaultPhoto(String filePath) throws IOException {
		Object[] o = new Object[3];
		String ohotoPath = filePath.concat("/image/foto_candidato_default.jpg");
		File file = new File(ohotoPath);
		o[0] = getBytesFromFile(file);
		o[1] = "foto_candidato_default";
		o[2] = ".jpg";
		return o;
	}

	public static File getCensusExample(String filePath) {
		String censusExamplePath = filePath.concat("/static/padron_electoral_ejemplo.xls");
		return new File(censusExamplePath);
	}

	public static File getElectionRolesRevisionDocument(String filePath) {
		String electionRolesDocumentoPath = filePath.concat("/static/EleccionesRolesFuncionamientoRevision.pdf");
		return new File(electionRolesDocumentoPath);
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			long length = file.length();
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}

			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}

			return bytes;
		} finally {
			fis.close();
		}
	}

	public static byte[] getBytesFromFile(String ruta) throws IOException {
		return getBytesFromFile(new File(ruta));
	}

	public static File convertBytesArrayToFile(byte[] bytes, String ruta) {
		FileOutputStream fileOuputStream = null;
		try {
			fileOuputStream = new FileOutputStream(ruta);
			fileOuputStream.write(bytes);
			fileOuputStream.flush();
			return new File(ruta);
		} catch (FileNotFoundException e) {			
			appLogger.error(e);
		} catch (IOException e) {
			appLogger.error(e);
		} finally {
			if(fileOuputStream != null) {
				try {
					fileOuputStream.close();
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		return null;
	}

}
