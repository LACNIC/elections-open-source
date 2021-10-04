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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class ExcelUtilsXLSX implements IExcelUtils {

	private static final String TEMP_DIR ="jboss.server.temp.dir";
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	private static final String stringCellType="STRING";
	private static final String numericCellType="NUMERIC";
	
	public  List<UserVoter> processCensusExcel(byte[] census) throws IOException, CensusValidationException  {
		List<UserVoter> userVotersList = new ArrayList<>();
		String filePath = System.getProperty(TEMP_DIR).concat("/padron");
		File file = FilesUtils.convertBytesArrayToFile(census, filePath);
		FileInputStream fileInputStream= new FileInputStream(file);
	    @SuppressWarnings("resource")
		XSSFWorkbook workbook= new XSSFWorkbook (fileInputStream);
		Set<String> mails = new HashSet<>();
		int languageIndex = -1;
		int nameIndex = -1;
		int mailIndex = -1;
		int orgIdIndex = -1;
		int countryIndex = -1;
		int voteAmountIndex = -1;
        XSSFSheet sheet = workbook.getSheetAt(0);
        CountryUtils countryUtils = new CountryUtils();
        int index=-1;
        boolean isEmpty=false;
        Iterator<Row> rowIteratorToAdd = sheet.iterator();
        while (rowIteratorToAdd.hasNext()) {
        	UserVoter userVoter= new UserVoter();
                Row rowVoters = rowIteratorToAdd.next();
                Iterator<Cell> cellIteratorToAdd = rowVoters.cellIterator();
                int rowNumber=rowVoters.getRowNum();
                while(cellIteratorToAdd.hasNext() && !isEmpty) {
                    Cell cell = cellIteratorToAdd.next();
                    CellType cellType =cell.getCellType();
                    Object obj = null;
                    switch(cellType.toString()) {
                    	case stringCellType:
                    		obj=cell.getStringCellValue();
                    		break;
                    	case numericCellType:
                    		obj=cell.getNumericCellValue();
                    		break;
                    }
                    if(rowNumber>0) {
                 
                        if(obj.toString().trim().isEmpty()) {
                        	isEmpty=true;
                        }
                       if(!isEmpty) {
                    	   int cellNumber=cell.getColumnIndex();
                           if(cellNumber==nameIndex) {
                           	String name=cell.getStringCellValue();
               				userVoter.setName(name);

                           }else if(cellNumber==mailIndex) {
                           	if (!(EmailValidator.getInstance().isValid(obj.toString()))) 
               					throw new CensusValidationException("Fila: " + rowNumber + " no contiene un e-mail valido: " + obj.toString());
            				userVoter.setMail(obj.toString());

               				if(!mails.add(obj.toString()))
               					throw new CensusValidationException("No puede haber dos votantes con el mismo e-mail, fila: " + rowNumber + " contiene un e-mail duplicado");
                           }else if(cellNumber==orgIdIndex) {
                           	if (orgIdIndex != -1)
                           		userVoter.setOrgID(obj.toString());
                           }
                           if(cellNumber==languageIndex) {
                           	if (languageIndex != -1) {
               					if (!obj.toString().matches("SP|EN|PT")) {
               						throw new CensusValidationException("Fila: " + rowNumber + " no contiene un idioma reconocido por el sistema: " + obj.toString()+"debe ser SP, EN ó PT");
               					}
               					userVoter.setLanguage(obj.toString());
               				}
                           }else if(cellNumber==countryIndex) {
                           	String country=obj.toString().toUpperCase();
                           	if (countryIndex != -1) {
               					if (!obj.toString().isEmpty() && !countryUtils.getIdsList().contains(country)) {
               						throw new CensusValidationException("Fila: " + rowNumber + " contiene un país no vacío y no válido: " + obj.toString());
               					}
               					userVoter.setCountry(obj.toString());
               				}
                           }else if(cellNumber==voteAmountIndex) {
                           	try {
                           		appLogger.info(obj.toString());
               					userVoter.setVoteAmount(Integer.valueOf("11"));
               				} catch (NumberFormatException ne) {
               					throw new CensusValidationException("Fila: " + rowNumber + " no contiene una cantidad de votos válida: " );
               				}
                           }
                       }

                       }else {
                    index=sheet.getRow(0).getCell(cell.getColumnIndex()).getColumnIndex();
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
                     
                }
                if(!isEmpty && rowNumber>0)
   				userVotersList.add(userVoter);	

            }

           
			
			return userVotersList;
	
	}
	

		
	
}
