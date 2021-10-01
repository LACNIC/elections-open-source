package net.lacnic.elections.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class ExcelUtilsXLSX  {

	private static final String TEMP_DIR ="jboss.server.temp.dir";

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	public static List<UserVoter> processCensusExcel(byte[] census) throws CensusValidationException, IOException {
		List<UserVoter> userVotersList = new ArrayList<>();
		String filePath = System.getProperty(TEMP_DIR).concat("/padron");
		File file = FilesUtils.convertBytesArrayToFile(census, filePath);
		FileInputStream fis;
        
        	fis= new FileInputStream(file);
	

        XSSFWorkbook myWorkBook;
			myWorkBook = new XSSFWorkbook (fis);
		

			int variablesAmount = 0;
			int languageIndex = -1;
			int nameIndex = -1;
			int mailIndex = -1;
			int orgIdIndex = -1;
			int countryIndex = -1;
			int voteAmountIndex = -1;
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            XSSFSheet sheet=mySheet;
            XSSFSheet sheetToIterate=sheet;

			CountryUtils countryUtils = new CountryUtils();

            Iterator<Row> rowIterator = mySheet.iterator();
            
            		
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int index=sheet.getRow(0).getCell(cell.getColumnIndex()).getColumnIndex();
                String value=sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue();
                if (value.equalsIgnoreCase(("idioma"))) {
					languageIndex = index;
				} else if (value.equalsIgnoreCase(("nombre"))) {
					nameIndex = index;
				} else if (value.equalsIgnoreCase(("mail"))) {
					mailIndex = index;
				} else if (value.equalsIgnoreCase(("orgID"))) {
					orgIdIndex = index;
				} else if (value.equalsIgnoreCase(("pais"))) {
					countryIndex = index;
				} else if (value.equalsIgnoreCase(("cantVotos"))) {
					voteAmountIndex = index;
				}
                
            }

			Set<String> mails = new HashSet<>();
            Iterator<Row> rowIteratorToAdd = sheetToIterate.iterator();
            while (rowIteratorToAdd.hasNext()) {
                Row rowVoters = rowIteratorToAdd.next();
                if(rowVoters.getRowNum()==0 || rowVoters.getRowNum()==1) {
                	rowVoters = rowIteratorToAdd.next();
                }
                Iterator<Cell> cellIteratorToAdd = row.cellIterator();
                while(cellIteratorToAdd.hasNext()) {
    				UserVoter userVoter = new UserVoter();

                    Cell cell = cellIteratorToAdd.next();
                    appLogger.info(cell.getStringCellValue());

                    if(cell.getColumnIndex()==nameIndex) {
                    	String name=cell.getStringCellValue();
                    
        				userVoter.setName(name);

                    }else if(cell.getColumnIndex()==mailIndex) {
                    	String mail=cell.getStringCellValue();
                		int i=rowVoters.getRowNum();

                    	if (!(EmailValidator.getInstance().isValid(mail))) 
        					throw new CensusValidationException("Fila: " + i + " no contiene un e-mail valido: " + mail);
                    	
                    		
        				if(!mails.add(mail))
        					throw new CensusValidationException("No puede haber dos votantes con el mismo e-mail, fila: " + i + " contiene un e-mail duplicado");
                    }else if(cell.getColumnIndex()==orgIdIndex) {
                    	if (orgIdIndex != -1)
                    		userVoter.setOrgID(cell.getStringCellValue());
                    }
                    if(cell.getColumnIndex()==languageIndex) {
                    	if (languageIndex != -1) {
                    		String languaje= cell.getStringCellValue();
                    		int i= rowVoters.getRowNum();
        					if (!languaje.matches("SP|EN|PT")) {
        						throw new CensusValidationException("Fila: " + i + " no contiene un idioma reconocido por el sistema: " + languaje+"ser SP, EN ó PT");
        					}
        					userVoter.setLanguage(languaje);
        				}
                    }else if(cell.getColumnIndex()==countryIndex) {
                    	if (countryIndex != -1) {
                    		int i= rowVoters.getRowNum();
                    		String country=cell.getStringCellValue();
        					if (!country.isEmpty() && !countryUtils.getIdsList().contains(country)) {
        						throw new CensusValidationException("Fila: " + i + " contiene un país no vacío y no válido: " + country);
        					}
        					userVoter.setCountry(country);
        				}
                    }else if(cell.getColumnIndex()==voteAmountIndex) {
                    	String votes=cell.getStringCellValue();
                    	int i=rowVoters.getRowNum();
                    
                    	try {
        					userVoter.setVoteAmount(Integer.valueOf(votes));
        				} catch (NumberFormatException ne) {
        					throw new CensusValidationException("Fila: " + i + " no contiene una cantidad de votos válida: " + votes);
        				}
                    }
    				userVotersList.add(userVoter);


                }
            }
            }
			
			
			return userVotersList;
	
	}
	

	public File exportToExcel(List<UserVoter> userVoters, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

}
