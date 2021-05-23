package net.lacnic.elections.admin.web.elecciones;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.elections.admin.web.commons.LinkValidator;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidato;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.utils.UtilsFiles;

public class CamposEleccionCandidatosPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private byte[] archivoFoto;
	private String nombreArchivo;
	private Candidato candidatoNuevo;

	public CamposEleccionCandidatosPanel(String id, Eleccion eleccion) {
		super(id);
		try {
			candidatoNuevo = new Candidato();
			candidatoNuevo.setSolosp(true);		
			
			TextField<String> nombreCandidato = new TextField<>("nombre", new PropertyModel<>(candidatoNuevo, "nombre"));
			nombreCandidato.setRequired(true);
			nombreCandidato.add(StringValidator.maximumLength(255));
			add(nombreCandidato);

			addBios();

			final FileUploadField fileFotoCandidato = new FileUploadField("fotoCandidato");
			add(fileFotoCandidato);

			SubmitLink subirFotoAjaxLink = new SubmitLink("agregarCandidato") {

				private static final long serialVersionUID = -8747001950049912880L;

				@Override
				public void onSubmit() {
					try {
						
						ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
						String filePath = context.getRealPath("/");
						
						Object[] defaultPhoto = UtilsFiles.getDefaultPhoto(filePath);
						FileUpload fileUpload = fileFotoCandidato.getFileUpload();
						if (fileUpload != null && !(fileUpload.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
							getSession().error(getString("candidateManagemenErrorForm"));
						} else {
							candidatoNuevo.setContenidoFoto(fileUpload != null ? fileUpload.getBytes() : (byte[]) defaultPhoto[0]);
							candidatoNuevo.setNombreFoto(fileUpload != null ? fileUpload.getClientFileName() : (String) defaultPhoto[1]);
							candidatoNuevo.setExtensionFoto(fileUpload != null ? fileUpload.getClientFileName().split("\\.")[1] : (String) defaultPhoto[2]);
							if (candidatoNuevo.isSolosp())
								candidatoNuevo.copiarBiosIdiomaAlResto();
							AppContext.getInstance().getManagerBeanRemote().agregarCandidato(eleccion.getIdEleccion(), candidatoNuevo, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							getSession().info(getString("candidateManagemenExitoAdd"));
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					} catch (Exception e) {
						getSession().error(getString("candidateManagemenErrorProc"));
					}

				}

			};
			add(subirFotoAjaxLink);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	private void addBios() {
		TextArea<String> bioEspanol = new TextArea<>("bioEspanol", new PropertyModel<>(candidatoNuevo, "bioEspanol"));
		bioEspanol.add(StringValidator.maximumLength(2000));
		bioEspanol.add(new LinkValidator());
		bioEspanol.setRequired(true);
		add(bioEspanol);

		TextField<String> linkEspanol = new TextField<>("linkEspanol", new PropertyModel<>(candidatoNuevo, "linkEspanol"));
		linkEspanol.add(StringValidator.maximumLength(1000));
		linkEspanol.add(new UrlValidator());
		add(linkEspanol);

		WebMarkupContainer biosCandidato = new WebMarkupContainer("biosCandidato");
		biosCandidato.setOutputMarkupPlaceholderTag(true);
		biosCandidato.setVisible(!candidatoNuevo.isSolosp());
		add(biosCandidato);

		TextArea<String> bioIngles = new TextArea<>("bioIngles", new PropertyModel<>(candidatoNuevo, "bioIngles"));
		bioIngles.add(StringValidator.maximumLength(2000));
		bioIngles.add(new LinkValidator());
		bioIngles.setRequired(true);
		biosCandidato.add(bioIngles);

		TextField<String> linkIngles = new TextField<>("linkIngles", new PropertyModel<>(candidatoNuevo, "linkIngles"));
		linkIngles.add(StringValidator.maximumLength(1000));
		linkIngles.add(new UrlValidator());
		biosCandidato.add(linkIngles);

		TextArea<String> bioPortugues = new TextArea<>("bioPortugues", new PropertyModel<>(candidatoNuevo, "bioPortugues"));
		bioPortugues.add(StringValidator.maximumLength(2000));
		bioPortugues.add(new LinkValidator());
		bioPortugues.setRequired(true);
		biosCandidato.add(bioPortugues);

		TextField<String> linkPortugues = new TextField<>("linkPortugues", new PropertyModel<>(candidatoNuevo, "linkPortugues"));
		linkPortugues.add(StringValidator.maximumLength(1000));
		linkPortugues.add(new UrlValidator());
		biosCandidato.add(linkPortugues);

		AjaxCheckBox otrosIdiomasBioCheck = new AjaxCheckBox("checkBoxBios", new PropertyModel<>(candidatoNuevo, "solosp")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				biosCandidato.setVisible(!candidatoNuevo.isSolosp());
				target.add(biosCandidato);
			}
		};

		add(otrosIdiomasBioCheck);

	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public byte[] getArchivoFoto() {
		return archivoFoto;
	}

	public void setArchivoFoto(byte[] archivoFoto) {
		this.archivoFoto = archivoFoto;
	}

}