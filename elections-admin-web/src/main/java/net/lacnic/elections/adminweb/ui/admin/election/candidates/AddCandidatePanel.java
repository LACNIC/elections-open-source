package net.lacnic.elections.adminweb.ui.admin.election.candidates;

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
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.LinkValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.FilesUtils;


public class AddCandidatePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private byte[] pictureFile;
	private String fileName;
	private Candidate candidate;


	public AddCandidatePanel(String id, Election election) {
		super(id);

		try {
			candidate = new Candidate();
			candidate.setOnlySp(true);

			TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(candidate, "name"));
			nameTextField.setRequired(true);
			nameTextField.add(StringValidator.maximumLength(255));
			add(nameTextField);

			TextField<String> mail = new TextField<>("mail", new PropertyModel<>(candidate, "mail"));
			mail.setRequired(true);
			mail.add(EmailAddressValidator.getInstance());
			add(mail);

			addBios();

			final FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
			add(candidatePictureUploadField);

			SubmitLink addCandidateButton = new SubmitLink("addCandidate") {
				private static final long serialVersionUID = -8747001950049912880L;

				@Override
				public void onSubmit() {
					try {
						ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
						String filePath = context.getRealPath("/");

						Object[] defaultPhoto = FilesUtils.getDefaultPhoto(filePath);
						FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
						if (fileUpload != null && !(fileUpload.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
							getSession().error(getString("candidateManagementErrorForm"));
						} else {
							candidate.setPictureInfo(fileUpload != null ? fileUpload.getBytes() : (byte[]) defaultPhoto[0]);
							candidate.setPictureName(fileUpload != null ? fileUpload.getClientFileName() : (String) defaultPhoto[1]);
							candidate.setPictureExtension(fileUpload != null ? fileUpload.getClientFileName().split("\\.")[1] : (String) defaultPhoto[2]);
							if (candidate.isOnlySp())
								candidate.copyBioToOtherLanguages();
							AppContext.getInstance().getManagerBeanRemote().addCandidate(election.getElectionId(), candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("candidateManagemenSuccessAdd"));
							setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					} catch (Exception e) {
						getSession().error(getString("candidateManagemenErrorProc"));
					}
				}
			};
			add(addCandidateButton);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	private void addBios() {
		WebMarkupContainer candidateEnPtBios = new WebMarkupContainer("candidateEnPtBios");
		candidateEnPtBios.setOutputMarkupPlaceholderTag(true);
		candidateEnPtBios.setVisible(!candidate.isOnlySp());
		add(candidateEnPtBios);

		AjaxCheckBox onlySpCheckbox = new AjaxCheckBox("checkboxBios", new PropertyModel<>(candidate, "onlySp")) {
			private static final long serialVersionUID = -1529631212868674173L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				candidateEnPtBios.setVisible(!candidate.isOnlySp());
				target.add(candidateEnPtBios);
			}
		};
		add(onlySpCheckbox);

		TextArea<String> bioSpanish = new TextArea<>("bioSpanish", new PropertyModel<>(candidate, "bioSpanish"));
		bioSpanish.add(StringValidator.maximumLength(2000));
		bioSpanish.add(new LinkValidator());
		bioSpanish.setRequired(true);
		add(bioSpanish);

		TextField<String> linkSpanish = new TextField<>("linkSpanish", new PropertyModel<>(candidate, "linkSpanish"));
		linkSpanish.add(StringValidator.maximumLength(1000));
		linkSpanish.add(new UrlValidator());
		add(linkSpanish);

		TextArea<String> bioEnglish = new TextArea<>("bioEnglish", new PropertyModel<>(candidate, "bioEnglish"));
		bioEnglish.add(StringValidator.maximumLength(2000));
		bioEnglish.add(new LinkValidator());
		bioEnglish.setRequired(true);
		candidateEnPtBios.add(bioEnglish);

		TextField<String> linkEnglish = new TextField<>("linkEnglish", new PropertyModel<>(candidate, "linkEnglish"));
		linkEnglish.add(StringValidator.maximumLength(1000));
		linkEnglish.add(new UrlValidator());
		candidateEnPtBios.add(linkEnglish);

		TextArea<String> bioPortuguese = new TextArea<>("bioPortuguese", new PropertyModel<>(candidate, "bioPortuguese"));
		bioPortuguese.add(StringValidator.maximumLength(2000));
		bioPortuguese.add(new LinkValidator());
		bioPortuguese.setRequired(true);
		candidateEnPtBios.add(bioPortuguese);

		TextField<String> linkPortuguese = new TextField<>("linkPortuguese", new PropertyModel<>(candidate, "linkPortuguese"));
		linkPortuguese.add(StringValidator.maximumLength(1000));
		linkPortuguese.add(new UrlValidator());
		candidateEnPtBios.add(linkPortuguese);
	}


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getPictureFile() {
		return pictureFile;
	}

	public void setPictureFile(byte[] pictureFile) {
		this.pictureFile = pictureFile;
	}

}
