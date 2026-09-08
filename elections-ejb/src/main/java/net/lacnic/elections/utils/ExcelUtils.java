package net.lacnic.elections.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.data.OrganizationBulkImportResult;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.services.detail.OrganizationDebtorImportResult;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.evra.registro.CategoriasEnum;

/**
 * Util class for Excel operations
 * 
 */
public class ExcelUtils {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private static final String TEMP_DIR = "jboss.server.temp.dir";
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String HEADER_LANGUAGE = "IDIOMA";
	private static final String HEADER_NAME = "NOMBRE";
	private static final String HEADER_MAIL = "MAIL";
	private static final String HEADER_VOTE_AMOUNT = "CANTVOTOS";
	private static final String HEADER_COUNTRY = "PAIS";
	private static final String HEADER_ORG_ID = "ORGID";
	private static final String HEADER_ORG_NAME = "ORGNAME";
	private static final String SHEET_ORGANIZATIONS_UPSERT = "organizaciones_alta_actualizacion";
	private static final String SAMPLE_ORG_ID = "UY-EXAMPLE-LACNIC";
	private static final String ERROR_ORGANIZATIONS_UPLOAD_UNKNOWN_FILE_TYPE = "organizationsManagementUploadUnknownFileType";
	private static final String ERROR_ORGANIZATIONS_UPLOAD_FILE = "organizationsManagementUploadFileError";
	private static final String ERROR_ORGANIZATIONS_UPLOAD_NO_DATA_ROWS = "organizationsManagementUploadNoDataRows";
	private static final String ERROR_ORGANIZATIONS_UPSERT_MISSING_COLUMNS = "organizationsManagementUpsertMissingColumns";
	private static final String ORGANIZATION_COLUMN_ORG_ID = "orgId";
	private static final String ORGANIZATION_COLUMN_NAME = "name";
	private static final String ORGANIZATION_COLUMN_VOTES = "votes";
	private static final String ORGANIZATION_COLUMN_CATEGORY = "category";
	private static final String ORGANIZATION_COLUMN_COUNTRY = "country";
	private static final String ORGANIZATION_COLUMN_CNPJ = "cnpj";
	private static final String ORGANIZATION_COLUMN_ASN = "asn";
	private static final String ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_ID = "membershipContactId";
	private static final String ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_NAME = "membershipContactName";
	private static final String ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL = "membershipContactEmail";
	private static final String ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE = "membershipContactLanguage";
	private static final String ERROR_ROW_PREFIX = "Fila ";
	private static final String[] ORGANIZATION_UPSERT_REQUIRED_COLUMNS = new String[] {
			ORGANIZATION_COLUMN_ORG_ID,
			ORGANIZATION_COLUMN_NAME,
			ORGANIZATION_COLUMN_VOTES,
			ORGANIZATION_COLUMN_CATEGORY,
			ORGANIZATION_COLUMN_COUNTRY,
			ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_ID,
			ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_NAME,
			ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL,
			ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE
	};
	private static final int MAX_ORGANIZATION_STRING_LENGTH = 255;
	private static final Pattern ORG_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{1,254}$");

	/**
	 * Processes the census in the Excel file and returns it as a list of UserVoter
	 * 
	 * @param contentType Excel type (xls or xlsx)
	 * @param census      the file
	 * @return the UserVoter list
	 * @throws CensusValidationException
	 */
	public static List<UserVoter> processCensusExcel(String contentType, byte[] census) throws CensusValidationException {
		List<UserVoter> userVotersList = new ArrayList<>();
		String filePath = System.getProperty(TEMP_DIR).concat("/padron" + System.currentTimeMillis());
		File file = FilesUtils.convertBytesArrayToFile(census, filePath);
		FileInputStream fis = null;
		Workbook workbook = null;

		try {
			fis = new FileInputStream(file);
			if (CONTENT_TYPE_XLSX.equals(contentType)) {
				workbook = new XSSFWorkbook(fis);
			} else {
				throw new CensusValidationException("censusManagementUploadUnknownFileType", null, null);
			}

			Sheet sheet = workbook.getSheetAt(0);

			Row firstRow = sheet.getRow(0);
			Map<String, Integer> header = parseHeader(firstRow);
			Integer languageIndex = getHeaderIndexIgnoreCase(header, HEADER_LANGUAGE);
			Integer nameIndex = getHeaderIndexIgnoreCase(header, HEADER_NAME);
			Integer mailIndex = getHeaderIndexIgnoreCase(header, HEADER_MAIL);
			Integer voteAmountIndex = getHeaderIndexIgnoreCase(header, HEADER_VOTE_AMOUNT);
			Integer countryIndex = getHeaderIndexIgnoreCase(header, HEADER_COUNTRY);
			Integer orgIdIndex = getHeaderIndexIgnoreCase(header, HEADER_ORG_ID);
			Integer orgNameIndex = getHeaderIndexIgnoreCase(header, HEADER_ORG_NAME);

			// check that all required columns exist
			if (languageIndex != null && nameIndex != null && mailIndex != null && voteAmountIndex != null && orgIdIndex != null) {
				int i = 0;
				CountryUtils countryUtils = new CountryUtils();
				// process each row
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
						// first row is titles
						if (i > 0) {
							int excelRowNumber = i + 1;
							if (isRowEmpty(row)) {
								i++;
								continue;
							}
						// check that required fields are not null
						String missingRequiredFields = getMissingRequiredCensusFields(row, languageIndex, nameIndex, mailIndex, voteAmountIndex, orgIdIndex);
						if (missingRequiredFields.isEmpty()) {
							UserVoter userVoter = new UserVoter();

							// language
								String language = getCellString(row, languageIndex);
								LanguageCode languageCode = LanguageCode.fromValue(language);
								if (languageCode == null) {
									throw new CensusValidationException("censusManagementUploadWrongLanguage", excelRowNumber, language);
								}
								userVoter.setLanguageEnum(languageCode);

							// name
							userVoter.setName(getCellString(row, nameIndex));
							userVoter.setOrgName(getCellString(row, orgNameIndex));

							// mail
								String mail = getCellString(row, mailIndex);
								if (!isValid(mail)) {
									throw new CensusValidationException("censusManagementUploadWrongEmail", excelRowNumber, mail);
								}
							userVoter.setMail(mail);

							// votes
								Integer votes = parseCensusVoteAmount(row.getCell(voteAmountIndex));
								if (votes == null || votes < 1 || votes > 11) {
									throw new CensusValidationException("censusManagementUploadWrongVoteAmount", excelRowNumber, null);
								}
							userVoter.setVoteAmount(votes);

							// org id
							userVoter.setOrgID(getCellString(row, orgIdIndex));

							// country
								if (countryIndex != null && row.getCell(countryIndex) != null) {
									String country = getCellString(row, countryIndex);
									if (!country.isEmpty() && !countryUtils.getIdsList().contains(country)) {
										throw new CensusValidationException("censusManagementUploadWrongCountry", excelRowNumber, country);
									}
									userVoter.setCountry(country);
								}

							userVotersList.add(userVoter);
							} else {
								throw new CensusValidationException("censusManagementUploadNullRequiredFields", excelRowNumber, missingRequiredFields);
							}
						}

						i++;
					}
					if (userVotersList.isEmpty()) {
						throw new CensusValidationException("censusManagementUploadNoDataRows", null, null);
					}
				} else {
					throw new CensusValidationException("censusManagementUploadMissingColumns", null, null);
				}

			return userVotersList;
		} catch (CensusValidationException cve) {
			appLogger.error(cve.getMessage(), cve);
			throw cve;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			throw new CensusValidationException("censusManagementUploadFileError", null, null);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
		}
	}

	public static OrganizationDebtorImportResult processOrganizationsDebtorsExcel(String contentType, byte[] content) throws CensusValidationException {
		OrganizationDebtorImportResult result = new OrganizationDebtorImportResult();
		String filePath = System.getProperty(TEMP_DIR).concat("/organizaciones_deudoras" + System.currentTimeMillis());
		File file = FilesUtils.convertBytesArrayToFile(content, filePath);
		FileInputStream fis = null;
		Workbook workbook = null;

		try {
			fis = new FileInputStream(file);
			if (contentType.equals(CONTENT_TYPE_XLSX)) {
				workbook = new XSSFWorkbook(fis);
			} else {
				throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_UNKNOWN_FILE_TYPE, null, null);
			}

			Sheet sheet = workbook.getSheetAt(0);
			Row firstRow = sheet.getRow(0);
			Map<String, Integer> header = parseHeader(firstRow);
			Integer orgIdIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_ORG_ID);
			if (orgIdIndex == null) {
				throw new CensusValidationException("organizationsManagementUploadMissingColumns", null, null);
			}

				List<String> orgIds = new ArrayList<>();
				Set<String> normalizedOrgIds = new HashSet<>();
				int i = 0;
				Iterator<Row> rowIterator = sheet.iterator();
					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						if (i > 0) {
							String orgId = getCellString(row, orgIdIndex);
							if (orgId.isEmpty()) {
							throw new CensusValidationException("organizationsManagementUploadNullRequiredFields", i, null);
						}
						String normalizedOrgId = orgId.trim().toUpperCase(Locale.ROOT);
						if (!normalizedOrgIds.add(normalizedOrgId)) {
							throw new CensusValidationException("organizationsManagementUploadDuplicateOrgId", null, null);
						}
						orgIds.add(orgId);
						}
						i++;
					}
				if (orgIds.isEmpty()) {
					throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_NO_DATA_ROWS, null, null);
				}

				result.setOrgIds(orgIds);
				result.setProcessedRows(orgIds.size());
				return result;
		} catch (CensusValidationException cve) {
			appLogger.error(cve.getMessage(), cve);
			throw cve;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_FILE, null, null);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
		}
	}

	private static boolean isValid(String mail) {
		return mail.contains("@") && mail.contains(".");
	}

	private static boolean isCellBlank(Row row, Integer index) {
		if (row == null || index == null || row.getCell(index) == null) {
			return true;
		}
		return getCellString(row, index).isBlank();
	}

	private static String getMissingRequiredCensusFields(Row row, Integer languageIndex, Integer nameIndex, Integer mailIndex, Integer voteAmountIndex, Integer orgIdIndex) {
		List<String> missingFields = new ArrayList<>();
		if (isCellBlank(row, languageIndex)) {
			missingFields.add(HEADER_LANGUAGE);
		}
		if (isCellBlank(row, nameIndex)) {
			missingFields.add(HEADER_NAME);
		}
		if (isCellBlank(row, mailIndex)) {
			missingFields.add(HEADER_MAIL);
		}
		if (isCellBlank(row, voteAmountIndex)) {
			missingFields.add(HEADER_VOTE_AMOUNT);
		}
		if (isCellBlank(row, orgIdIndex)) {
			missingFields.add(HEADER_ORG_ID);
		}
		return String.join(", ", missingFields);
	}

	private static Integer parseCensusVoteAmount(Cell cell) {
		if (cell == null) {
			return null;
		}

		if (cell.getCellType() == CellType.NUMERIC) {
			double numeric = cell.getNumericCellValue();
			if (numeric % 1 != 0) {
				return null;
			}
			return (int) numeric;
		}

		String value = cell.toString().trim();
		if (value.isEmpty()) {
			return null;
		}
		if (value.matches("\\d+")) {
			return Integer.parseInt(value);
		}
		if (value.matches("\\d+\\.0+")) {
			return Integer.parseInt(value.substring(0, value.indexOf('.')));
		}
		return null;
	}

	/**
	 * Export the list of voters to a xlsx Excel file
	 * 
	 * @param userVoters the voters list
	 * @param fileName   the file name
	 * @return the File object
	 */
	public static File exportToExcel(List<UserVoter> userVoters, String fileName) {
		return exportToExcel(userVoters, fileName, true);
	}

	public static File exportToExcel(List<UserVoter> userVoters, String fileName, boolean includeOrgIdColumn) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));

		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
			XSSFSheet sheet = workbook.createSheet("Padron_Electoral");

			int rowIndex = 0;
			int columnIndex = 0;
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_LANGUAGE);
			cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_NAME);
			cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_ORG_NAME);
			cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_MAIL);
			cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_VOTE_AMOUNT);
			cell = row.createCell(columnIndex++);
			cell.setCellValue(HEADER_COUNTRY);
			if (includeOrgIdColumn) {
				cell = row.createCell(columnIndex++);
				cell.setCellValue(HEADER_ORG_ID);
			}

			for (UserVoter userVoter : userVoters) {
				columnIndex = 0;
				row = sheet.createRow(rowIndex++);
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getLanguage());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getName());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(defaultString(userVoter.getOrgName()));
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getMail());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getVoteAmount());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(defaultString(userVoter.getCountry()));
				if (includeOrgIdColumn) {
					cell = row.createCell(columnIndex++);
					cell.setCellValue(defaultString(userVoter.getOrgID()));
				}
			}

			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(fos);
			return file;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
		}
		return null;
	}

	public static File exportOrganizationsUpsertExample(String fileName) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));
		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
			XSSFSheet sheet = workbook.createSheet(SHEET_ORGANIZATIONS_UPSERT);
			String[] columns = getOrganizationUpsertExportColumns();
			Row header = sheet.createRow(0);
			for (int i = 0; i < columns.length; i++) {
				header.createCell(i).setCellValue(columns[i]);
			}
			Row sample = sheet.createRow(1);
			sample.createCell(0).setCellValue(SAMPLE_ORG_ID);
			sample.createCell(1).setCellValue("Organization Example");
			sample.createCell(2).setCellValue(1);
			sample.createCell(3).setCellValue("LEGACY");
			sample.createCell(4).setCellValue("UY");
			sample.createCell(5).setCellValue("");
			sample.createCell(6).setCellValue("");
			sample.createCell(7).setCellValue("example-contact");
			sample.createCell(8).setCellValue("Contact Example");
			sample.createCell(9).setCellValue("example@domain.org");
			sample.createCell(10).setCellValue("ES");

			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}
			workbook.write(fos);
			return file;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			return null;
		}
	}

	public static File exportOrganizationsSingleColumnExample(String fileName, String columnName) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));
		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
			XSSFSheet sheet = workbook.createSheet("organizaciones");
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue(columnName == null || columnName.isBlank() ? HEADER_ORG_ID : columnName);
			Row sample = sheet.createRow(1);
			sample.createCell(0).setCellValue(SAMPLE_ORG_ID);
			sheet.autoSizeColumn(0);
			workbook.write(fos);
			return file;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			return null;
		}
	}

	public static File exportOrganizationsUpsertToExcel(List<Organization> organizations, String fileName) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));

		try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
			XSSFSheet sheet = workbook.createSheet(SHEET_ORGANIZATIONS_UPSERT);
			int rowIndex = 0;
			Row header = sheet.createRow(rowIndex++);
			String[] columns = getOrganizationUpsertExportColumns();
			for (int i = 0; i < columns.length; i++) {
				header.createCell(i).setCellValue(columns[i]);
			}

			for (Organization organization : organizations) {
				Row row = sheet.createRow(rowIndex++);
				row.createCell(0).setCellValue(defaultString(organization.getOrgId()));
				row.createCell(1).setCellValue(defaultString(organization.getName()));
				if (organization.getVotes() == null) {
					row.createCell(2).setCellValue("");
				} else {
					row.createCell(2).setCellValue(organization.getVotes());
				}
				row.createCell(3).setCellValue(defaultString(organization.getCategory()));
				row.createCell(4).setCellValue(defaultString(organization.getCountry()));
				row.createCell(5).setCellValue(defaultString(organization.getCnpj()));
				row.createCell(6).setCellValue(defaultString(organization.getAsn()));
				row.createCell(7).setCellValue(defaultString(organization.getMembershipContactId()));
				row.createCell(8).setCellValue(defaultString(organization.getMembershipContactName()));
				row.createCell(9).setCellValue(defaultString(organization.getMembershipContactEmail()));
				row.createCell(10).setCellValue(defaultString(organization.getMembershipContactLanguage()));
			}

			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(fos);
			return file;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			return null;
		}
	}

	public static OrganizationBulkImportResult processOrganizationsDeleteExcel(String contentType, byte[] content) throws CensusValidationException {
		OrganizationBulkImportResult result = new OrganizationBulkImportResult();
		OrganizationDebtorImportResult debtor = processOrganizationsDebtorsExcel(contentType, content);
		result.setProcessedRows(debtor.getProcessedRows());
		result.setCreatedRows(0);
		result.setUpdatedRows(0);
		return result;
	}

	private static String[] getOrganizationUpsertExportColumns() {
		return new String[] {
				HEADER_ORG_ID,
				"NAME",
				"VOTES",
				"CATEGORY",
				"COUNTRY",
				"CNPJ",
				"ASN",
				"MEMBERSHIPCONTACTID",
				"MEMBERSHIPCONTACTNAME",
				"MEMBERSHIPCONTACTEMAIL",
				"MEMBERSHIPCONTACTLANGUAGE"
		};
	}

	private static String getRequiredOrganizationUpsertColumnsDescription() {
		return ORGANIZATION_COLUMN_ORG_ID + ", "
				+ ORGANIZATION_COLUMN_NAME + ", "
				+ ORGANIZATION_COLUMN_VOTES + ", "
				+ ORGANIZATION_COLUMN_CATEGORY + ", "
				+ ORGANIZATION_COLUMN_COUNTRY + ", "
				+ ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_ID + ", "
				+ ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_NAME + ", "
				+ ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL + " y "
				+ ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE;
	}

	public static byte[] generateSingleColumnErrorReportXlsx(String sheetName, String columnName, List<String> values) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet(sheetName == null || sheetName.isBlank() ? "errores" : sheetName);
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue(columnName == null || columnName.isBlank() ? "error" : columnName);
			for (int i = 0; i < values.size(); i++) {
				Row row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(values.get(i));
			}
			sheet.autoSizeColumn(0);
			workbook.write(bos);
			return bos.toByteArray();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	public static OrganizationsUpsertExcelData processOrganizationsUpsertExcel(String contentType, byte[] content) throws CensusValidationException {
		String filePath = System.getProperty(TEMP_DIR).concat("/organizaciones_alta_actualizacion" + System.currentTimeMillis());
		File file = FilesUtils.convertBytesArrayToFile(content, filePath);
		FileInputStream fis = null;
		Workbook workbook = null;
		try {
			fis = new FileInputStream(file);
			if (CONTENT_TYPE_XLSX.equals(contentType)) {
				workbook = new XSSFWorkbook(fis);
			} else {
				throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_UNKNOWN_FILE_TYPE, null, null);
			}

			Sheet sheet = workbook.getSheetAt(0);
			Row firstRow = sheet.getRow(0);
			Map<String, Integer> header = parseHeader(firstRow);
			for (String requiredColumn : ORGANIZATION_UPSERT_REQUIRED_COLUMNS) {
				if (getHeaderIndexIgnoreCase(header, requiredColumn) == null) {
					throw new CensusValidationException(ERROR_ORGANIZATIONS_UPSERT_MISSING_COLUMNS, null, null);
				}
			}

			Integer orgIdIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_ORG_ID);
			Integer nameIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_NAME);
			Integer votesIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_VOTES);
			Integer categoryIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_CATEGORY);
			Integer countryIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_COUNTRY);
			Integer cnpjIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_CNPJ);
			Integer asnIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_ASN);
			Integer membershipContactIdIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_ID);
			Integer membershipContactNameIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_NAME);
			Integer membershipContactEmailIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL);
			Integer membershipContactLanguageIndex = getHeaderIndexIgnoreCase(header, ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE);

				if (orgIdIndex == null || nameIndex == null || votesIndex == null || categoryIndex == null || countryIndex == null || membershipContactIdIndex == null || membershipContactNameIndex == null || membershipContactEmailIndex == null
						|| membershipContactLanguageIndex == null) {
					throw new CensusValidationException(ERROR_ORGANIZATIONS_UPSERT_MISSING_COLUMNS, null, null);
				}

				List<Organization> organizations = new ArrayList<>();
				List<String> errors = new ArrayList<>();
				Set<String> normalizedOrgIds = new HashSet<>();
				Set<String> validCountryCodes = new HashSet<>();
				for (String countryCode : new CountryUtils().getIdsListExcludingDefault()) {
					if (countryCode != null && !countryCode.trim().isEmpty()) {
						validCountryCodes.add(countryCode.trim().toUpperCase(Locale.ROOT));
					}
			}

			int rowIndex = 0;
			Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					rowIndex++;
					if (rowIndex == 1) {
					continue;
				}
				if (isRowEmpty(row)) {
					continue;
				}

					String orgId = getCellString(row, orgIdIndex);
					String name = getCellString(row, nameIndex);
				String votes = getCellString(row, votesIndex);
				String category = getCellString(row, categoryIndex);
				String country = getCellString(row, countryIndex);
				String cnpj = getCellString(row, cnpjIndex);
				String asn = getCellString(row, asnIndex);
				String membershipContactId = getCellString(row, membershipContactIdIndex);
				String membershipContactName = getCellString(row, membershipContactNameIndex);
				String membershipContactEmail = getCellString(row, membershipContactEmailIndex);
				String membershipContactLanguage = getCellString(row, membershipContactLanguageIndex);

					if (orgId.isBlank() || name.isBlank() || votes.isBlank() || category.isBlank() || country.isBlank() || membershipContactId.isBlank() || membershipContactName.isBlank() || membershipContactEmail.isBlank()
							|| membershipContactLanguage.isBlank()) {
						errors.add(ERROR_ROW_PREFIX + rowIndex
								+ ": los campos obligatorios son: " + getRequiredOrganizationUpsertColumnsDescription() + ".");
						continue;
					}
					String firstFieldTooLong = firstFieldExceedingMaxLength(orgId, name, category, country, cnpj, asn, membershipContactId, membershipContactName, membershipContactEmail, membershipContactLanguage);
					if (firstFieldTooLong != null) {
						errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + firstFieldTooLong + " inválido. Máximo " + MAX_ORGANIZATION_STRING_LENGTH + " caracteres.");
						continue;
					}

					if (!ORG_ID_PATTERN.matcher(orgId).matches()) {
						errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_ORG_ID + " inválido. Debe comenzar con letra o número y solo puede contener letras, números, '.', '-' o '_'.");
						continue;
					}
					String normalizedOrgId = orgId.trim().toUpperCase(Locale.ROOT);
					if (!normalizedOrgIds.add(normalizedOrgId)) {
						errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_ORG_ID + " duplicado en el archivo (" + orgId + ").");
						continue;
					}
					Integer parsedVotes = parseVotes(votes);
					if (parsedVotes == null || parsedVotes < 1 || parsedVotes > 11) {
						errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_VOTES + " inválido. Debe ser un entero entre 1 y 11.");
						continue;
					}
				if (CategoriasEnum.fromValue(category) == null) {
					errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_CATEGORY + " inválido. Debe ser una categoría vigente del sistema.");
					continue;
				}
				if (!validCountryCodes.contains(country.trim().toUpperCase(Locale.ROOT))) {
					errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_COUNTRY + " inválido. Debe ser un código ISO de 2 letras (por ejemplo: UY, BO, AR).");
					continue;
				}
				String normalizedAsn = asn == null ? "" : asn.trim();
				if (!isBasicEmailValue(membershipContactEmail)) {
					errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL + " inválido. Debe contener al menos '@'.");
					continue;
				}
				LanguageCode membershipLanguage = LanguageCode.fromValue(membershipContactLanguage);
				if (membershipLanguage == null) {
					errors.add(ERROR_ROW_PREFIX + rowIndex + ": " + ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE + " inválido. Valores válidos: SP/ES, EN, PT.");
					continue;
				}

				Organization org = new Organization();
				org.setOrgId(orgId);
				org.setName(name);
				org.setVotes(parsedVotes);
				org.setCategory(category);
				org.setCountry(country.trim().toUpperCase(Locale.ROOT));
				org.setCnpj(cnpj.isBlank() ? null : cnpj);
				org.setAsn(normalizedAsn.isBlank() ? null : normalizedAsn);
				org.setMembershipContactId(membershipContactId);
				org.setMembershipContactName(membershipContactName);
				org.setMembershipContactEmail(membershipContactEmail);
					org.setMembershipContactLanguage(membershipLanguage.getCode());
					organizations.add(org);
				}
				if (organizations.isEmpty() && errors.isEmpty()) {
					throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_NO_DATA_ROWS, null, null);
				}
				OrganizationsUpsertExcelData data = new OrganizationsUpsertExcelData();
				data.setOrganizations(organizations);
				data.setErrors(errors);
			data.setProcessedRows(organizations.size());
			return data;
		} catch (CensusValidationException cve) {
			appLogger.error(cve.getMessage(), cve);
			throw cve;
		} catch (IOException ioe) {
			appLogger.error(ioe.getMessage(), ioe);
			throw new CensusValidationException(ERROR_ORGANIZATIONS_UPLOAD_FILE, null, null);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
					appLogger.error(ioe.getMessage(), ioe);
				}
			}
		}
	}

	private static Map<String, Integer> parseHeader(Row firstRow) {
		Map<String, Integer> header = new LinkedHashMap<>();
		if (firstRow == null) {
			return header;
		}
		for (int i = 0; i < firstRow.getLastCellNum(); i++) {
			if (firstRow.getCell(i) == null) {
				continue;
			}
			String value = firstRow.getCell(i).toString();
			if (value == null || value.trim().isEmpty()) {
				continue;
			}
			header.put(value.trim(), i);
		}
		return header;
	}

	private static Integer getHeaderIndexIgnoreCase(Map<String, Integer> header, String columnName) {
		if (header == null || columnName == null) {
			return null;
		}
		for (Entry<String, Integer> entry : header.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(columnName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static String getCellString(Row row, Integer index) {
		if (index == null || row == null || row.getCell(index) == null) {
			return "";
		}
		return row.getCell(index).toString().trim();
	}

	private static boolean isRowEmpty(Row row) {
		if (row == null) {
			return true;
		}
		for (int i = 0; i < row.getLastCellNum(); i++) {
			if (!getCellString(row, i).isBlank()) {
				return false;
			}
		}
		return true;
	}

	private static String firstFieldExceedingMaxLength(String orgId, String name, String category, String country, String cnpj, String asn, String membershipContactId, String membershipContactName, String membershipContactEmail,
			String membershipContactLanguage) {
		if (isTooLong(orgId)) {
			return ORGANIZATION_COLUMN_ORG_ID;
		}
		if (isTooLong(name)) {
			return ORGANIZATION_COLUMN_NAME;
		}
		if (isTooLong(category)) {
			return ORGANIZATION_COLUMN_CATEGORY;
		}
		if (isTooLong(country)) {
			return ORGANIZATION_COLUMN_COUNTRY;
		}
		if (isTooLong(cnpj)) {
			return ORGANIZATION_COLUMN_CNPJ;
		}
		if (isTooLong(asn)) {
			return ORGANIZATION_COLUMN_ASN;
		}
		if (isTooLong(membershipContactId)) {
			return ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_ID;
		}
		if (isTooLong(membershipContactName)) {
			return ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_NAME;
		}
		if (isTooLong(membershipContactEmail)) {
			return ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_EMAIL;
		}
		if (isTooLong(membershipContactLanguage)) {
			return ORGANIZATION_COLUMN_MEMBERSHIP_CONTACT_LANGUAGE;
		}
		return null;
	}

	private static boolean isTooLong(String value) {
		return value != null && value.length() > MAX_ORGANIZATION_STRING_LENGTH;
	}

	private static String defaultString(String value) {
		return value == null ? "" : value;
	}

	private static Integer parseVotes(String votes) {
		String normalizedVotes = votes == null ? "" : votes.trim();
		if (normalizedVotes.isEmpty()) {
			return null;
		}
		int dotIndex = normalizedVotes.indexOf('.');
		if (dotIndex >= 0) {
			normalizedVotes = normalizedVotes.substring(0, dotIndex);
		}
		try {
			return Integer.parseInt(normalizedVotes);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean isBasicEmailValue(String email) {
		if (email == null) {
			return false;
		}
		String value = email.trim();
		return !value.isEmpty() && value.contains("@");
	}

	public static class OrganizationsUpsertExcelData implements java.io.Serializable {
		private static final long serialVersionUID = 7483722627938140302L;
		private List<Organization> organizations = new ArrayList<>();
		private List<String> errors = new ArrayList<>();
		private int processedRows;

		public List<Organization> getOrganizations() {
			return organizations;
		}

		public void setOrganizations(List<Organization> organizations) {
			this.organizations = organizations;
		}

		public List<String> getErrors() {
			return errors;
		}

		public void setErrors(List<String> errors) {
			this.errors = errors;
		}

		public int getProcessedRows() {
			return processedRows;
		}

		public void setProcessedRows(int processedRows) {
			this.processedRows = processedRows;
		}
	}

}
