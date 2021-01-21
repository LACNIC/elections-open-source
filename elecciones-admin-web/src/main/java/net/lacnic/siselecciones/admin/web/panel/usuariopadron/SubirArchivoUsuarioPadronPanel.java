package net.lacnic.siselecciones.admin.web.panel.usuariopadron;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.utils.ValidacionPadronException;

public class SubirArchivoUsuarioPadronPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private byte[] archivoPadron;
	private String nombreArchivo;

	public SubirArchivoUsuarioPadronPanel(String id, Eleccion eleccion, Class classs) {
		super(id);
		try {

			setOutputMarkupId(true);

			final FileUploadField fufPadron = new FileUploadField("filePadron");
			add(fufPadron);

			SubmitLink agregarPadronAjaxSubmitLink = new SubmitLink("agregarUsuarioPadron") {

				private static final long serialVersionUID = 1204295872124958662L;

				@Override
				public void onSubmit() {
					if (fufPadron.getFileUpload() == null)
						getSession().error(getString("censusManagementErrNoFile"));
					else {
						setArchivoPadron(fufPadron.getFileUpload().getBytes());
						setNombreArchivo(fufPadron.getFileUpload().getClientFileName());
						try {
							Contexto.getInstance().getManagerBeanRemote().actualizarUsuariosPadron(eleccion.getIdEleccion(), getArchivoPadron(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							getSession().info(getString("censusManagementExitoUpFile"));
							if (classs != null)
								setResponsePage(classs, UtilsParameters.getId(eleccion.getIdEleccion()));
						} catch (ValidacionPadronException e) {
							getSession().error(e.getMessage());
							appLogger.error(e);
							setResponsePage(classs, UtilsParameters.getId(eleccion.getIdEleccion()));
						} catch (Exception e) {
							if (e.getMessage().equals("BiffException"))
								getSession().error(getString("censusManagementErrBif"));
							appLogger.error(e);
							setResponsePage(classs, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					}
				}
			};
			add(agregarPadronAjaxSubmitLink);
			
			ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
			String filePath = context.getRealPath("/");
			File archivoEjemplo = Contexto.getInstance().getManagerBeanRemote().exportarEjemploPadronElectoral(filePath);
			add(new DownloadLink("exportarEjemplo", archivoEjemplo));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	public byte[] getArchivoPadron() {
		return archivoPadron;
	}

	public void setArchivoPadron(byte[] archivoPadron) {
		this.archivoPadron = archivoPadron;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

}
