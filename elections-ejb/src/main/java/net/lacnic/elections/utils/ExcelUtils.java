package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;


/**
 * Util class for Excel operations in xlsx format
 * 
 */
public class ExcelUtils {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private static final String TEMP_DIR = "jboss.server.temp.dir";
	private static final String CONTENT_TYPE_XLS = "application/vnd.ms-excel";
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";


	/**
	 * Processes the census in the Excel file and returns it as a list of UserVoter 
	 * 
	 * @param contentType Excel type (xls or xlsx)
	 * @param census the file
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
			if(contentType.equals(CONTENT_TYPE_XLS)) {
				workbook = new HSSFWorkbook(fis);
			} else if(contentType.equals(CONTENT_TYPE_XLSX)) {
				workbook = new XSSFWorkbook(fis);
			} else {
				throw new CensusValidationException("censusManagementUploadUnknownFileType", null, null);
			}

			int variablesAmount = 0;
			int languageIndex = -1;
			int nameIndex = -1;
			int mailIndex = -1;
			int voteAmountIndex = -1;
			int countryIndex = -1;
			int orgIdIndex = -1;
			Sheet sheet = workbook.getSheetAt(0);

			Row firstRow = sheet.getRow(0);
			// check that all required columns exist
			if(validateRequiredColumns(firstRow)) {
				// find column index for each field
				while (variablesAmount < firstRow.getLastCellNum() && !firstRow.getCell(variablesAmount).getStringCellValue().equals("")) {
					String value = firstRow.getCell(variablesAmount).getStringCellValue();
					if (value.equalsIgnoreCase(("idioma"))) {
						languageIndex = variablesAmount;
					} else if (value.equalsIgnoreCase(("nombre"))) {
						nameIndex = variablesAmount;
					} else if (value.equalsIgnoreCase(("mail"))) {
						mailIndex = variablesAmount;
					} else if (value.equalsIgnoreCase(("orgID"))) {
						orgIdIndex = variablesAmount;
					} else if (value.equalsIgnoreCase(("pais"))) {
						countryIndex = variablesAmount;
					} else if (value.equalsIgnoreCase(("cantVotos"))) {
						voteAmountIndex = variablesAmount;
					}
					variablesAmount++;
				}

				int i = 0;
				Set<String> mails = new HashSet<>();
				CountryUtils countryUtils = new CountryUtils();
				// process each row
				Iterator<Row> rowIterator = sheet.iterator();
				while(rowIterator.hasNext()) {
					Row row = rowIterator.next();
					// first row is titles
					if(i > 0) {
						// check that required fields are not null
						if(validateRequiredFields(row, languageIndex, nameIndex, mailIndex, voteAmountIndex)) {
							UserVoter userVoter = new UserVoter();

							// language
							String language = row.getCell(languageIndex).getStringCellValue();
							if (!language.matches("SP|EN|PT")) {
								throw new CensusValidationException("censusManagementUploadWrongLanguage", i, language);
							}
							userVoter.setLanguage(language);

							// name
							userVoter.setName(row.getCell(nameIndex).getStringCellValue());

							// mail
							String mail = row.getCell(mailIndex).getStringCellValue();
							if(!(EmailValidator.getInstance().isValid(mail))) {
								throw new CensusValidationException("censusManagementUploadWrongEmail", i, mail);
							}
							if(!mails.add(mail)) {
								throw new CensusValidationException("censusManagementUploadDuplicateEmail", i, mail);
							}
							userVoter.setMail(mail);

							// votes
							try {
								int votes = (int)row.getCell(voteAmountIndex).getNumericCellValue();
								if(votes > 0)
									userVoter.setVoteAmount(votes);
								else
									throw new CensusValidationException("censusManagementUploadWrongVoteAmount", i, null);
							} catch (Exception e) {
								throw new CensusValidationException("censusManagementUploadWrongVoteAmount", i, null);
							}

							// org id
							if (orgIdIndex != -1 && row.getCell(orgIdIndex) != null)
								userVoter.setOrgID(row.getCell(orgIdIndex).getStringCellValue());

							// country
							if (countryIndex != -1 && row.getCell(countryIndex) != null) {
								String country = row.getCell(countryIndex).getStringCellValue();
								if (!country.isEmpty() && !countryUtils.getIdsList().contains(country)) {
									throw new CensusValidationException("censusManagementUploadWrongCountry", i, country);
								}
								userVoter.setCountry(country);
							}

							userVotersList.add(userVoter);
						} else {
							throw new CensusValidationException("censusManagementUploadNullRequiredFields", i, null);
						}
					}

					i++;
				}
			} else {
				throw new CensusValidationException("censusManagementUploadMissingColumns", null, null);
			}

			return userVotersList;
		} catch (CensusValidationException cve) {
			appLogger.error(cve);
			throw cve;
		} catch (IOException ioe) {
			appLogger.error(ioe);
			throw new CensusValidationException("censusManagementUploadFileError", null, null);
		} finally {
			if(workbook != null) {
				try { workbook.close(); } catch (IOException ioe) { appLogger.error(ioe); }
			}
			if(fis != null) {
				try { fis.close(); } catch (IOException ioe) { appLogger.error(ioe); }
			}
		}
	}

	/**
	 * Validate that required columns exist
	 * 
	 * @param firstRow titles row
	 * @return true if valid, false otherwise
	 */
	private static boolean validateRequiredColumns(Row firstRow) {
		int variablesAmount = 0;
		int languageIndex = -1;
		int nameIndex = -1;
		int mailIndex = -1;
		int voteAmountIndex = -1;

		while (variablesAmount < firstRow.getLastCellNum() && !firstRow.getCell(variablesAmount).getStringCellValue().equals("")) {
			String value = firstRow.getCell(variablesAmount).getStringCellValue();
			if (value.equalsIgnoreCase("idioma")) {
				languageIndex = variablesAmount;
			} else if (value.equalsIgnoreCase("nombre")) {
				nameIndex = variablesAmount;
			} else if (value.equalsIgnoreCase("mail")) {
				mailIndex = variablesAmount;
			} else if (value.equalsIgnoreCase("cantVotos")) {
				voteAmountIndex = variablesAmount;
			}
			variablesAmount++;
		}

		return !(languageIndex == -1 || nameIndex == -1 || mailIndex == -1 || voteAmountIndex == -1);
	}

	/**
	 * Validate that required fields are not null in given row
	 * 
	 * @param row the row to check
	 * @param languageIndex the language column index
	 * @param nameIndex the name column index
	 * @param mailIndex the mail column index
	 * @param voteAmountIndex the voteAmount column index
	 * @return true if valid, false otherwise
	 */
	private static boolean validateRequiredFields(Row row, int languageIndex, int nameIndex, int mailIndex, int voteAmountIndex) {
		return !(row.getCell(languageIndex) == null || row.getCell(nameIndex) == null || row.getCell(mailIndex) == null || row.getCell(voteAmountIndex) == null);
	}


	public static File exportToExcel(List<UserVoter> userVoters, String fileName) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Padron_Electoral");

			int rowIndex = 0;
			int columnIndex = 0;
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue("IDIOMA");
			cell = row.createCell(columnIndex++);
			cell.setCellValue("NOMBRE");
			cell = row.createCell(columnIndex++);
			cell.setCellValue("MAIL");
			cell = row.createCell(columnIndex++);
			cell.setCellValue("CANTVOTOS");
			cell = row.createCell(columnIndex++);
			cell.setCellValue("PAIS");
			cell = row.createCell(columnIndex++);
			cell.setCellValue("ORGID");

			for(UserVoter userVoter : userVoters) {
				columnIndex = 0;
				row = sheet.createRow(rowIndex++);
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getLanguage());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getName());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getMail());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getVoteAmount());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getCountry());
				cell = row.createCell(columnIndex++);
				cell.setCellValue(userVoter.getOrgID());
			}

			for(int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(new FileOutputStream(file));
			workbook.close();
			return file;
		} catch(IOException ioe) {
			appLogger.error(ioe);
		}
		return null;
	}

}
