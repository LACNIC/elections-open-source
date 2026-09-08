package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesUtils {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");
	private static final String DEFAULT_CANDIDATE_PHOTO_RESOURCE = "image/default_candidate_photo.jpg";
	private static final String DEFAULT_CANDIDATE_PHOTO_NAME = "default_candidate_photo.jpg";
	private static final String DEFAULT_CANDIDATE_PHOTO_EXTENSION = "jpg";
	private static final String DEFAULT_ABSTENTION_PHOTO_RESOURCE = "image/default_abstention_photo.jpg";
	private static final String DEFAULT_ABSTENTION_PHOTO_NAME = "default_abstention_photo.jpg";
	private static final String DEFAULT_ABSTENTION_PHOTO_EXTENSION = "jpg";
	private static final String CENSUS_EXAMPLE_ORG_ID_FILE = "exampleCensusOrgId.xlsx";
	private static final String CENSUS_EXAMPLE_EMAIL_FILE = "exampleCensusEmail.xlsx";
	private static final String ORGANIZATIONS_UPSERT_EXAMPLE_FILE = "exampleAddUpdateOrgs.xlsx";
	private static final String ORGANIZATIONS_DELETE_EXAMPLE_FILE = "exampleDeleteOrgs.xlsx";
	private static final String ORGANIZATIONS_DEBTORS_EXAMPLE_FILE = "exampleDebtors.xlsx";
	private static final String MILACNIC_CONF_FOLDER = "milacnic";

	private FilesUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Object[] getDefaultPhoto(String filePath) throws IOException {
		Object[] preferredPhoto = getPhotoIfPresent(DEFAULT_CANDIDATE_PHOTO_RESOURCE, DEFAULT_CANDIDATE_PHOTO_NAME, DEFAULT_CANDIDATE_PHOTO_EXTENSION, filePath);
		if (preferredPhoto != null) {
			return preferredPhoto;
		}
		return buildEmptyPhoto(DEFAULT_CANDIDATE_PHOTO_NAME, DEFAULT_CANDIDATE_PHOTO_EXTENSION);
	}

	public static Object[] getDefaultAbstentionPhoto(String filePath) throws IOException {
		Object[] photo = getPhotoIfPresent(DEFAULT_ABSTENTION_PHOTO_RESOURCE, DEFAULT_ABSTENTION_PHOTO_NAME, DEFAULT_ABSTENTION_PHOTO_EXTENSION, filePath);
		return photo != null ? photo : buildEmptyPhoto(DEFAULT_ABSTENTION_PHOTO_NAME, DEFAULT_ABSTENTION_PHOTO_EXTENSION);
	}

	private static Object[] getPhotoIfPresent(String resourcePath, String fileName, String extension, String filePath) throws IOException {
		Object[] o = new Object[3];
		InputStream resourceInput = FilesUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		if (resourceInput != null) {
			try (InputStream input = resourceInput) {
				o[0] = input.readAllBytes();
				o[1] = fileName;
				o[2] = extension;
				return o;
			}
		}

		if (filePath != null && !filePath.isEmpty()) {
			String photoPath = filePath.concat("/image/").concat(fileName);
			File file = new File(photoPath);
			if (file.exists() && file.isFile()) {
				o[0] = getBytesFromFile(file);
				o[1] = fileName;
				o[2] = extension;
				return o;
			}
		}

		return null;
	}

	private static Object[] buildEmptyPhoto(String fileName, String extension) {
		Object[] o = new Object[3];
		o[0] = new byte[0];
		o[1] = fileName;
		o[2] = extension;
		return o;
	}

	public static File getCensusExample(String filePath) {
		String censusExamplePath = filePath.concat("/static/padron_electoral_ejemplo.xls");
		return new File(censusExamplePath);
	}

	public static File getJbossTempCensusExample(boolean identityByOrgId) {
		return getServerExampleFile(identityByOrgId ? CENSUS_EXAMPLE_ORG_ID_FILE : CENSUS_EXAMPLE_EMAIL_FILE);
	}

	public static File getJbossTempOrganizationsUpsertExample() {
		return getServerExampleFile(ORGANIZATIONS_UPSERT_EXAMPLE_FILE);
	}

	public static File getJbossTempOrganizationsDeleteExample() {
		return getServerExampleFile(ORGANIZATIONS_DELETE_EXAMPLE_FILE);
	}

	public static File getJbossTempOrganizationsDebtorsExample() {
		return getServerExampleFile(ORGANIZATIONS_DEBTORS_EXAMPLE_FILE);
	}

	private static File getServerExampleFile(String fileName) {
		File configFile = buildServerFile(EJBFactory.getJbossConfUri(), fileName);
		if (configFile != null && configFile.exists()) {
			return configFile;
		}

		File legacyConfigFile = buildServerFile(EJBFactory.getJbossConfUri(), MILACNIC_CONF_FOLDER + "/" + fileName);
		if (legacyConfigFile != null && legacyConfigFile.exists()) {
			return legacyConfigFile;
		}

		File tempFile = buildServerFile(EJBFactory.getJbossTempUri(), fileName);
		if (tempFile != null && tempFile.exists()) {
			return tempFile;
		}

		if (configFile != null) {
			return configFile;
		}
		if (tempFile != null) {
			return tempFile;
		}
		return new File(fileName);
	}

	private static File buildServerFile(String basePath, String relativePath) {
		if (basePath == null || basePath.trim().isEmpty()) {
			return null;
		}
		return new File(basePath + relativePath);
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
			appLogger.error(e.getMessage());
		} catch (IOException e) {
			appLogger.error(e.getMessage());
		} finally {
			if (fileOuputStream != null) {
				try {
					fileOuputStream.close();
				} catch (IOException e) {
					appLogger.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

}
