package net.lacnic.elections.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;


/**
 * Clase utilizada para conversión y armados de excel para exportacion e
 * importacion del padrón
 * 
 */
public class ExcelUtils {

	private static final String TEMP_DIR ="jboss.server.temp.dir";

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");


	private ExcelUtils() {
		throw new IllegalStateException("Utility class");
	}


	public static List<UserVoter> processCensusExcel(byte[] census) throws Exception {
		List<UserVoter> userVotersList = new ArrayList<>();
		String filePath = System.getProperty(TEMP_DIR).concat("/padron");
		File file = FilesUtils.convertBytesArrayToFile(census, filePath);
		Workbook workbook;

		try {
			WorkbookSettings workbookSettings = new WorkbookSettings();
			workbookSettings.setEncoding("Cp1252");
			workbook = Workbook.getWorkbook(file, workbookSettings);
			int variablesAmount = 0;
			int languageIndex = -1;
			int nameIndex = -1;
			int mailIndex = -1;
			int orgIdIndex = -1;
			int countryIndex = -1;
			int voteAmountIndex = -1;
			Sheet sheet = workbook.getSheet(0);
			CountryUtils countryUtils = new CountryUtils();

			while (variablesAmount < sheet.getColumns() && !sheet.getCell(variablesAmount, 0).getContents().equals("")) {
				String value = sheet.getCell(variablesAmount, 0).getContents();
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

			int i = 1;
			Set<String> mails = new HashSet<>();
			// process each row
			while (i < sheet.getRows() && !sheet.getCell(0, i).getContents().equals("")) {
				UserVoter userVoter = new UserVoter();

				// name
				userVoter.setName(sheet.getCell(nameIndex, i).getContents());

				// mail
				String mail = sheet.getCell(mailIndex, i).getContents();
				if (!(EmailValidator.getInstance().isValid(mail)))
					throw new CensusValidationException("Fila: " + i + " no contiene un e-mail valido: " + mail);
				if(!mails.add(mail))
					throw new CensusValidationException("No puede haber dos votantes con el mismo e-mail, fila: " + i + " contiene un e-mail duplicado");
				userVoter.setMail(mail);

				// votes
				String votes = sheet.getCell(voteAmountIndex, i).getContents();
				try {
					userVoter.setVoteAmount(Integer.valueOf(votes));
				} catch (NumberFormatException ne) {
					throw new CensusValidationException("Fila: " + i + " no contiene una cantidad de votos válida: " + votes);
				}

				// org id
				if (orgIdIndex != -1)
					userVoter.setOrgID(sheet.getCell(orgIdIndex, i).getContents());

				// language
				if (languageIndex != -1) {
					String language = sheet.getCell(languageIndex, i).getContents();
					if (!language.matches("SP|EN|PT")) {
						throw new CensusValidationException("Fila: " + i + " no contiene un idioma reconocido por el sistema: " + language + ", debe ser SP, EN ó PT");
					}
					userVoter.setLanguage(language);
				}

				// country
				if (countryIndex != -1) {
					String country = sheet.getCell(countryIndex, i).getContents();
					if (!country.isEmpty() && !countryUtils.getIdsList().contains(country)) {
						throw new CensusValidationException("Fila: " + i + " contiene un país no vacío y no válido: " + country);
					}
					userVoter.setCountry(country);
				}
				userVotersList.add(userVoter);
				i++;
			}
			return userVotersList;
		} catch (CensusValidationException e) {
			appLogger.error(e);
			throw e;
		} catch (BiffException be) {
			appLogger.error(be);
			throw new Exception("BiffException");
		}
	}

	public static File exportToExcel(List<UserVoter> userVoters, String fileName) {
		File file = new File(System.getProperty(TEMP_DIR).concat(fileName));

		try {
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			WritableSheet sheet = workbook.createSheet("Padron_Electoral", 0);

			sheet.addCell(new Label(0, 0, "IDIOMA"));
			sheet.addCell(new Label(1, 0, "NOMBRE"));
			sheet.addCell(new Label(2, 0, "MAIL"));
			sheet.addCell(new Label(3, 0, "CANTVOTOS"));
			sheet.addCell(new Label(4, 0, "PAIS"));
			sheet.addCell(new Label(5, 0, "ORGID"));

			int verticalDataPosition = 1;
			for (UserVoter userVoter : userVoters) {
				sheet.addCell(new Label(0, verticalDataPosition, userVoter.getLanguage()));
				sheet.addCell(new Label(1, verticalDataPosition, userVoter.getName()));
				sheet.addCell(new Label(2, verticalDataPosition, userVoter.getMail()));
				sheet.addCell(new Label(3, verticalDataPosition, String.valueOf(userVoter.getVoteAmount())));
				sheet.addCell(new Label(4, verticalDataPosition, userVoter.getCountry()));
				sheet.addCell(new Label(5, verticalDataPosition, userVoter.getOrgID()));
				verticalDataPosition++;
			}

			// le digo a cada columna que se ajuste al texto
			CellView cellView = new CellView();
			cellView.setAutosize(true);

			for(int i = 0; i < sheet.getColumns(); i++) {
				sheet.setColumnView(i, cellView);
			}

			workbook.write();
			workbook.close();
			return file;
		} catch (IOException | WriteException  e) {
			appLogger.error(e);	
		}
		return null;
	}

}
