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
 * @author Antonymous
 * 
 */
public class UtilsExcel {

	private static final String TEMP_DIR ="jboss.server.temp.dir";

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private UtilsExcel() {
		throw new IllegalStateException("Utility class");
	}

	public static List<UserVoter> procesarExcelPadronNuevo(byte[] padron) throws Exception {
		List<UserVoter> listUsuarioPadron = new ArrayList<>();

		String rutaArchivo = System.getProperty(TEMP_DIR).concat("/padron");
		File f = UtilsFiles.convertirArrayBytesAFile(padron, rutaArchivo);
		Workbook libro;
		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("Cp1252");
			libro = Workbook.getWorkbook(f, ws);
			int cantVariables = 0;
			int indiceIdioma = -1;
			int indiceNombre = -1;
			int indiceMail = -1;
			int indiceOrgID = -1;
			int indicePais = -1;
			int indiceCantVotos = -1;
			Sheet hoja = libro.getSheet(0);
			UtilsPaises u = new UtilsPaises();

			while (cantVariables < hoja.getColumns() && !hoja.getCell(cantVariables, 0).getContents().equals("")) {
				String valor = hoja.getCell(cantVariables, 0).getContents();
				if (valor.equalsIgnoreCase(("idioma"))) {
					indiceIdioma = cantVariables;
				} else if (valor.equalsIgnoreCase(("nombre"))) {
					indiceNombre = cantVariables;
				} else if (valor.equalsIgnoreCase(("mail"))) {
					indiceMail = cantVariables;
				} else if (valor.equalsIgnoreCase(("orgID"))) {
					indiceOrgID = cantVariables;
				} else if (valor.equalsIgnoreCase(("pais"))) {
					indicePais = cantVariables;
				} else if (valor.equalsIgnoreCase(("cantVotos"))) {
					indiceCantVotos = cantVariables;
				}
				cantVariables++;
			}

			int i = 1;
			Set<String> mails = new HashSet<>();
			// process each row
			while (i < hoja.getRows() && !hoja.getCell(0, i).getContents().equals("")) {
				UserVoter usPadronData = new UserVoter();

				// name
				usPadronData.setName(hoja.getCell(indiceNombre, i).getContents());

				// mail
				String mail = hoja.getCell(indiceMail, i).getContents();
				if (!(EmailValidator.getInstance().isValid(mail)))
					throw new CensusValidationException("Fila: " + i + " no contiene un e-mail valido: " + mail);
				if(!mails.add(mail))
					throw new CensusValidationException("No puede haber dos votantes con el mismo e-mail, fila: " + i + " contiene un e-mail duplicado");
				usPadronData.setMail(mail);


				// votes
				String votos = hoja.getCell(indiceCantVotos, i).getContents();
				try {
					usPadronData.setVoteAmount(Integer.valueOf(votos));
				} catch (NumberFormatException ne) {
					throw new CensusValidationException("Fila: " + i + " no contiene una cantidad de votos válida: " + votos);
				}

				// org id
				if (indiceOrgID != -1)
					usPadronData.setOrgID(hoja.getCell(indiceOrgID, i).getContents());

				// language
				if (indiceIdioma != -1) {
					String idioma = hoja.getCell(indiceIdioma, i).getContents();
					if (!idioma.matches("SP|EN|PT")) {
						throw new CensusValidationException("Fila: " + i + " no contiene un idioma reconocido por el sistema: " + idioma + ", debe ser SP, EN ó PT");
					}
					usPadronData.setLanguage(idioma);
				}

				// country
				if (indicePais != -1) {
					String pais = hoja.getCell(indicePais, i).getContents();
					if (!pais.isEmpty() && !u.getIdPaises().contains(pais)) {
						throw new CensusValidationException("Fila: " + i + " contiene un país no vacío y no válido: " + pais);
					}
					usPadronData.setCountry(pais);
				}
				listUsuarioPadron.add(usPadronData);
				i++;
			}
			return listUsuarioPadron;
		} catch (CensusValidationException e) {
			appLogger.error(e);
			throw e;
		} catch (BiffException be) {
			appLogger.error(be);
			throw new Exception("BiffException");
		}
	}

	public static File exportarAExcel(List<UserVoter> usuariosPadron, String nombreArchivo) {
		File archivo = new File(System.getProperty(TEMP_DIR).concat(nombreArchivo));

		try {
			WritableWorkbook libro = Workbook.createWorkbook(archivo);
			WritableSheet hoja = libro.createSheet("Padron_Electoral", 0);

			hoja.addCell(new Label(0, 0, "IDIOMA"));
			hoja.addCell(new Label(1, 0, "NOMBRE"));
			hoja.addCell(new Label(2, 0, "MAIL"));
			hoja.addCell(new Label(3, 0, "CANTVOTOS"));
			hoja.addCell(new Label(4, 0, "PAIS"));
			hoja.addCell(new Label(5, 0, "ORGID"));

			int posicionDatosVertical = 1;
			for (UserVoter up : usuariosPadron) {
				hoja.addCell(new Label(0, posicionDatosVertical, up.getLanguage()));
				hoja.addCell(new Label(1, posicionDatosVertical, up.getName()));
				hoja.addCell(new Label(2, posicionDatosVertical, up.getMail()));
				hoja.addCell(new Label(3, posicionDatosVertical, String.valueOf(up.getVoteAmount())));
				hoja.addCell(new Label(4, posicionDatosVertical, up.getCountry()));
				hoja.addCell(new Label(5, posicionDatosVertical, up.getOrgID()));
				posicionDatosVertical++;
			}

			// le digo a cada columna que se ajuste al texto
			CellView cellView = new CellView();
			cellView.setAutosize(true);

			for(int i = 0; i < hoja.getColumns(); i++) {
				hoja.setColumnView(i, cellView);
			}

			libro.write();
			libro.close();
			return archivo;
		} catch (IOException | WriteException  e) {
			appLogger.error(e);	
		}
		return null;
	}

}
