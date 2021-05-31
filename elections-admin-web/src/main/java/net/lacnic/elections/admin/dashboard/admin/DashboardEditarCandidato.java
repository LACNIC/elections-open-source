package net.lacnic.elections.admin.dashboard.admin;

import java.util.Arrays;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.commons.LinkValidator;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEditarCandidato extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Candidate candidato;
	private byte[] archivoFoto;
	private String nombreArchivo;
	private String nombre;
	private String bioSP;
	private String bioEN;
	private String bioPT;

	private String linkSP;
	private String linkEN;
	private String linkPT;

	public DashboardEditarCandidato(PageParameters params) {
		super(params);
		long idCandidato = UtilsParameters.getCandidateAsLong(params);
		candidato = AppContext.getInstance().getManagerBeanRemote().obtenerCandidato(idCandidato);
		nombre = candidato.getName();
		bioSP = candidato.getBioSpanish();
		bioEN = candidato.getBioEnglish();
		bioPT = candidato.getBioPortuguese();

		linkSP = candidato.getLinkSpanish();
		linkEN = candidato.getLinkEnglish();
		linkPT = candidato.getLinkPortuguese();
		archivoFoto = candidato.getPictureInfo();
		add(new FeedbackPanel("feedback"));
		Form<Void> formCandidato = new Form<>("formCandidato");
		add(formCandidato);

		try {

			TextField<String> nombreCandidato = new TextField<>("nombre", new PropertyModel<>(candidato, "nombre"));
			nombreCandidato.setRequired(true);
			nombreCandidato.add(StringValidator.maximumLength(255));
			formCandidato.add(nombreCandidato);

			TextArea<String> bioEspanol = new TextArea<>("bioEspanol", new PropertyModel<>(candidato, "bioEspanol"));
			bioEspanol.add(StringValidator.maximumLength(2000));
			bioEspanol.add(new LinkValidator());
			bioEspanol.setRequired(true);
			formCandidato.add(bioEspanol);

			TextField<String> linkEspanol = new TextField<>("linkEspanol", new PropertyModel<>(candidato, "linkEspanol"));
			linkEspanol.add(StringValidator.maximumLength(1000));
			linkEspanol.add(new UrlValidator());
			formCandidato.add(linkEspanol);

			WebMarkupContainer biosCandidato = new WebMarkupContainer("biosCandidato");
			biosCandidato.setOutputMarkupPlaceholderTag(true);
			biosCandidato.setVisible(!candidato.isOnlySp());
			formCandidato.add(biosCandidato);

			TextArea<String> bioIngles = new TextArea<>("bioIngles", new PropertyModel<>(candidato, "bioIngles"));
			bioIngles.add(StringValidator.maximumLength(2000));
			bioIngles.add(new LinkValidator());
			bioIngles.setRequired(true);
			biosCandidato.add(bioIngles);

			TextField<String> linkIngles = new TextField<>("linkIngles", new PropertyModel<>(candidato, "linkIngles"));
			linkIngles.add(StringValidator.maximumLength(1000));
			linkIngles.add(new UrlValidator());
			biosCandidato.add(linkIngles);

			TextArea<String> bioPortugues = new TextArea<>("bioPortugues", new PropertyModel<>(candidato, "bioPortugues"));
			bioPortugues.add(StringValidator.maximumLength(2000));
			bioPortugues.add(new LinkValidator());
			bioPortugues.setRequired(true);
			biosCandidato.add(bioPortugues);

			TextField<String> linkPortugues = new TextField<>("linkPortugues", new PropertyModel<>(candidato, "linkPortugues"));
			linkPortugues.add(StringValidator.maximumLength(1000));
			linkPortugues.add(new UrlValidator());
			biosCandidato.add(linkPortugues);

			AjaxCheckBox otrosIdiomasBioCheck = new AjaxCheckBox("checkBoxBios", new PropertyModel<>(candidato, "solosp")) {

				private static final long serialVersionUID = -6342948109436896963L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					biosCandidato.setVisible(!candidato.isOnlySp());
					target.add(biosCandidato);
				}
			};
			formCandidato.add(otrosIdiomasBioCheck);

			final FileUploadField fileFotoCandidato = new FileUploadField("fotoCandidato");
			formCandidato.add(fileFotoCandidato);

			formCandidato.add(new Button("editarCandidato") {

				private static final long serialVersionUID = 5423459004548252541L;

				@Override
				public void onSubmit() {
					try {
						FileUpload fileUpload = fileFotoCandidato.getFileUpload();
						if (fileUpload != null) {
							if (!(fileUpload.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
								getSession().error(getString("candidateManagemenErrorForm"));
							} else {
								candidato.setPictureInfo(fileUpload.getBytes());
								candidato.setPictureName(fileUpload.getClientFileName());
								candidato.setPictureExtension(fileUpload.getClientFileName().split("\\.")[1]);
								cambiarDatos();
							}
						} else {
							cambiarDatos();
						}

					} catch (Exception e) {
						getSession().error(getString("candidateManagemenErrorProc"));
					}
				}

				private void cambiarDatos() {
					if (!(nombre.equalsIgnoreCase(candidato.getName())) || !(Arrays.equals(archivoFoto, candidato.getPictureInfo()))
							|| !(bioSP.equalsIgnoreCase(candidato.getBioSpanish())) || !(bioEN.equalsIgnoreCase(candidato.getBioEnglish())) || !(bioPT.equalsIgnoreCase(candidato.getBioPortuguese())) 
							|| !(linkSP != null && linkSP.equalsIgnoreCase(candidato.getLinkSpanish())) || !(linkPT != null && linkPT.equalsIgnoreCase(candidato.getLinkPortuguese())) || !(linkEN != null && linkEN.equalsIgnoreCase(candidato.getLinkEnglish()))) {
						if (candidato.isOnlySp())
							candidato.copyBioToOtherLanguages();
						AppContext.getInstance().getManagerBeanRemote().editarCandidato(getCandidato(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
						getSession().info(getString("candidateEditExito"));
					} else
						getSession().error(getString("candidateEditError"));

					setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(candidato.getElection().getElectionId()));
				}

			});

			formCandidato.add(new Link<Void>("cancelarEditar") {

				private static final long serialVersionUID = -2380441384773838117L;

				@Override
				public void onClick() {
					setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(candidato.getElection().getElectionId()));
				}

			});

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	public Candidate getCandidato() {
		return candidato;
	}

	public void setCandidato(Candidate candidato) {
		this.candidato = candidato;
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