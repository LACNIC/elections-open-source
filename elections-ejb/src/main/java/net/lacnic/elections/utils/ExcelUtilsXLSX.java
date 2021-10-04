package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class ExcelUtilsXLSX implements IExcelUtils {

	private static final String TEMP_DIR = "jboss.server.temp.dir";
	private static final String stringCellType = "STRING";
	private static final String numericCellType = "NUMERIC";
	CountryUtils countryUtils = new CountryUtils();

	public List<UserVoter> processCensusExcel(byte[] census) throws IOException, CensusValidationException {
		List<UserVoter> userVotersList = new ArrayList<>();
		String filePath = System.getProperty(TEMP_DIR).concat("/padron");
		File file = FilesUtils.convertBytesArrayToFile(census, filePath);
		FileInputStream fileInputStream = new FileInputStream(file);
		@SuppressWarnings("resource")
		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
		Set<String> mails = new HashSet<>();
		int languageIndex = -1;
		int nameIndex = -1;
		int mailIndex = -1;
		int orgIdIndex = -1;
		int countryIndex = -1;
		int voteAmountIndex = -1;
		XSSFSheet sheet = workbook.getSheetAt(0);
		int index = -1;
		boolean isEmpty = false;
		Iterator<Row> rowIteratorToAdd = sheet.iterator();
		while (rowIteratorToAdd.hasNext()) {
			UserVoter userVoter = new UserVoter();
			Row rowVoters = rowIteratorToAdd.next();
			Iterator<Cell> cellIteratorToAdd = rowVoters.cellIterator();
			int rowNumber = rowVoters.getRowNum();
			Object obj = null;

			while (cellIteratorToAdd.hasNext() && !isEmpty) {
				Cell cell = cellIteratorToAdd.next();
				CellType cellType = cell.getCellType();
				double votes;
				switch (cellType.toString()) {
				case stringCellType:
					obj = cell.getStringCellValue();
					break;
				case numericCellType:
					votes = cell.getNumericCellValue();
					break;
				}
				String value=obj.toString();
				if (rowNumber > 0) {
					if (obj != null && value.trim().isEmpty()) {
						isEmpty = true;
					}
					int cellNumber = cell.getColumnIndex();
					if (cellNumber == nameIndex) {
						this.throwExceptionIfNameIsEmpty(isEmpty, rowNumber);
						userVoter.setName(value);

					} else if (cellNumber == mailIndex) {
						this.throwExceptionIfMailIsNotValid( obj, rowNumber,mails);
						userVoter.setMail(value);

					} else if (cellNumber == orgIdIndex) {
						if (this.isValidIndex(orgIdIndex))
							userVoter.setOrgID(value);
					}
					if (cellNumber == languageIndex) {
						if (this.isValidIndex(languageIndex)) {
							this.throwExceptionIfLanguageIsNotValid(rowNumber, obj);
						userVoter.setLanguage(value);
						}
					} else if (cellNumber == countryIndex) {
						if (this.isValidIndex(countryIndex)) {
							this.throwExceptionIfCountryIsNotValid(value.toUpperCase(),rowNumber);
							userVoter.setCountry(value.toUpperCase());
						}
					} else if (cellNumber == voteAmountIndex) {
						this.throwExceptionIfCellVoteIsEmpty(cell, rowNumber);
						userVoter=this.trySettingUserVoterVotes(userVoter, cell, rowNumber);
					}

				} else {
					index = sheet.getRow(0).getCell(cell.getColumnIndex()).getColumnIndex();
					String cellValue = sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue();
					if (cellValue.equalsIgnoreCase(("idioma"))) {
						languageIndex = index;
					} else if (cellValue.equalsIgnoreCase(("nombre"))) {
						nameIndex = index;
					} else if (cellValue.equalsIgnoreCase(("mail"))) {
						mailIndex = index;
					} else if (cellValue.equalsIgnoreCase(("orgID"))) {
						orgIdIndex = index;
					} else if (cellValue.equalsIgnoreCase(("pais"))) {
						countryIndex = index;
					} else if (cellValue.equalsIgnoreCase(("cantVotos"))) {
						voteAmountIndex = index;
					}
				}

			}

			this.validateNullAttributes(rowNumber, userVoter);

			if (!isEmpty && rowNumber > 0)
				userVotersList.add(userVoter);
		}

		return userVotersList;

	}
	
	private UserVoter trySettingUserVoterVotes(UserVoter userVoter, Cell cell, int rowNumber) throws CensusValidationException {
		try {
			userVoter.setVoteAmount((int) cell.getNumericCellValue());
		} catch (NumberFormatException ne) {
			throw new CensusValidationException("Fila: " + rowNumber + " no contiene una cantidad de votos válida");
		}
		return userVoter;
	}
	
	private void throwExceptionIfCellVoteIsEmpty(Cell cell, int rowNumber) throws CensusValidationException {
		if (cell.getCellType() == CellType.BLANK) {
			throw new CensusValidationException("Fila: " + rowNumber + " no contiene una cantidad de votos válida ");
		}
	}
	
	
	private boolean isValidIndex(int index) {
		return index!=-1;
	}
	
	private void  throwExceptionIfMailIsNotValid(Object obj, int rowNumber,Set<String> mails) throws CensusValidationException {
		if (!(EmailValidator.getInstance().isValid(obj.toString()))) {
			throw new CensusValidationException("Fila: " + rowNumber + " no contiene un e-mail valido: " + obj.toString());
		}
		if (!mails.add(obj.toString()))
			throw new CensusValidationException("No puede haber dos votantes con el mismo e-mail, fila: " + rowNumber	+ " contiene un e-mail duplicado");
	}

	private void throwExceptionIfLanguageIsNotValid(int rowNumber, Object obj) throws CensusValidationException {
		if (!obj.toString().matches("SP|EN|PT")) {
			throw new CensusValidationException("Fila: " + rowNumber + " no contiene un idioma reconocido por el sistema: "	+ obj.toString() + "debe ser SP, EN ó PT");
		}
	}
	
	private void throwExceptionIfCountryIsNotValid(String country,int rowNumber) throws CensusValidationException {
		if (!country.isEmpty() && !countryUtils.getIdsList().contains(country)) {
			throw new CensusValidationException("Fila: " + rowNumber	+ " contiene un país no vacío y no válido: " + country);
		}
	}
	
	private void throwExceptionIfNameIsEmpty(boolean isEmpty, int rowNumber) throws CensusValidationException {
		if (isEmpty)
			throw new CensusValidationException("El nombre  no puede ser nulo en fila : " + rowNumber);
	}

	private void validateNullAttributes(int rowNumber, UserVoter userVoter) throws CensusValidationException {
		this.validateAttribute(rowNumber, userVoter.getVoteAmount(), "votos");
		this.validateAttribute(rowNumber, userVoter.getMail(), "Mail");
		this.validateAttribute(rowNumber, userVoter.getLanguage(), "Idioma");
		this.validateAttribute(rowNumber, userVoter.getName(), "Nombre");
	}

	private void validateAttribute(int rowNumber, Object obj, String messageException)
			throws CensusValidationException {
		if (rowNumber > 0 && obj == null) {
			throw new CensusValidationException(messageException + " no puede ser nulo en fila : " + rowNumber);
		}
	}

}
