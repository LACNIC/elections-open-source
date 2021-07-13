package net.lacnic.elections.adminweb.ui.admin.election.candidates;

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

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.validators.LinkValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;


@AuthorizeInstantiation("elections-only-one")
public class EditCandidateDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Candidate candidate;
	private byte[] pictureInfo;
	private String pictureName;
	private String name;
	private String bioSpanish;
	private String bioEnglish;
	private String bioPortuguese;
	private String linkSpanish;
	private String linkEnglish;
	private String linkPortuguese;


	public EditCandidateDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long candidateId = UtilsParameters.getCandidateAsLong(params);
		candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
		name = candidate.getName();
		bioSpanish = candidate.getBioSpanish();
		bioEnglish = candidate.getBioEnglish();
		bioPortuguese = candidate.getBioPortuguese();
		linkSpanish = candidate.getLinkSpanish();
		linkEnglish = candidate.getLinkEnglish();
		linkPortuguese = candidate.getLinkPortuguese();
		pictureInfo = candidate.getPictureInfo();

		Form<Void> candidateForm = new Form<>("candidateForm");
		add(candidateForm);

		try {
			WebMarkupContainer candidateEnPtBios = new WebMarkupContainer("candidateEnPtBios");
			candidateEnPtBios.setOutputMarkupPlaceholderTag(true);
			candidateEnPtBios.setVisible(!candidate.isOnlySp());
			candidateForm.add(candidateEnPtBios);

			AjaxCheckBox onlySpCheckbox = new AjaxCheckBox("checkboxBios", new PropertyModel<>(candidate, "onlySp")) {
				private static final long serialVersionUID = -6342948109436896963L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					candidateEnPtBios.setVisible(!candidate.isOnlySp());
					target.add(candidateEnPtBios);
				}
			};
			candidateForm.add(onlySpCheckbox);

			TextField<String> nameTextField = new TextField<>("name", new PropertyModel<>(candidate, "name"));
			nameTextField.setRequired(true);
			nameTextField.add(StringValidator.maximumLength(255));
			candidateForm.add(nameTextField);

			TextArea<String> bioSpanishTextField = new TextArea<>("bioSpanish", new PropertyModel<>(candidate, "bioSpanish"));
			bioSpanishTextField.add(StringValidator.maximumLength(2000));
			bioSpanishTextField.add(new LinkValidator());
			bioSpanishTextField.setRequired(true);
			candidateForm.add(bioSpanishTextField);

			TextField<String> linkSpanishTextField = new TextField<>("linkSpanish", new PropertyModel<>(candidate, "linkSpanish"));
			linkSpanishTextField.add(StringValidator.maximumLength(1000));
			linkSpanishTextField.add(new UrlValidator());
			candidateForm.add(linkSpanishTextField);

			TextArea<String> bioEnglishTextField = new TextArea<>("bioEnglish", new PropertyModel<>(candidate, "bioEnglish"));
			bioEnglishTextField.add(StringValidator.maximumLength(2000));
			bioEnglishTextField.add(new LinkValidator());
			bioEnglishTextField.setRequired(true);
			candidateEnPtBios.add(bioEnglishTextField);

			TextField<String> linkEnglishTextField = new TextField<>("linkEnglish", new PropertyModel<>(candidate, "linkEnglish"));
			linkEnglishTextField.add(StringValidator.maximumLength(1000));
			linkEnglishTextField.add(new UrlValidator());
			candidateEnPtBios.add(linkEnglishTextField);

			TextArea<String> bioPortugueseTextField = new TextArea<>("bioPortuguese", new PropertyModel<>(candidate, "bioPortuguese"));
			bioPortugueseTextField.add(StringValidator.maximumLength(2000));
			bioPortugueseTextField.add(new LinkValidator());
			bioPortugueseTextField.setRequired(true);
			candidateEnPtBios.add(bioPortugueseTextField);

			TextField<String> linkPortugueseTextField = new TextField<>("linkPortuguese", new PropertyModel<>(candidate, "linkPortuguese"));
			linkPortugueseTextField.add(StringValidator.maximumLength(1000));
			linkPortugueseTextField.add(new UrlValidator());
			candidateEnPtBios.add(linkPortugueseTextField);

			final FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
			candidateForm.add(candidatePictureUploadField);

			candidateForm.add(new Button("saveCandidate") {
				private static final long serialVersionUID = 5423459004548252541L;

				@Override
				public void onSubmit() {
					try {
						FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
						if (fileUpload != null) {
							if (!(fileUpload.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
								getSession().error(getString("candidateManagementErrorForm"));
							} else {
								candidate.setPictureInfo(fileUpload.getBytes());
								candidate.setPictureName(fileUpload.getClientFileName());
								candidate.setPictureExtension(fileUpload.getClientFileName().split("\\.")[1]);
								save();
							}
						} else {
							save();
						}
					} catch (Exception e) {
						getSession().error(getString("candidateManagemenErrorProc"));
					}
				}

				private void save() {
					if (!(name.equalsIgnoreCase(candidate.getName())) || !(Arrays.equals(pictureInfo, candidate.getPictureInfo()))
							|| !(bioSpanish.equalsIgnoreCase(candidate.getBioSpanish())) || !(bioEnglish.equalsIgnoreCase(candidate.getBioEnglish()))
							|| !(bioPortuguese.equalsIgnoreCase(candidate.getBioPortuguese())) || !(linkSpanish != null && linkSpanish.equalsIgnoreCase(candidate.getLinkSpanish())) 
							|| !(linkPortuguese != null && linkPortuguese.equalsIgnoreCase(candidate.getLinkPortuguese())) || !(linkEnglish != null && linkEnglish.equalsIgnoreCase(candidate.getLinkEnglish()))) {
						if (candidate.isOnlySp())
							candidate.copyBioToOtherLanguages();
						AppContext.getInstance().getManagerBeanRemote().editCandidate(getCandidate(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("candidateEditSuccess"));
					} else
						getSession().error(getString("candidateEditError"));

					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(candidate.getElection().getElectionId()));
				}
			});

			candidateForm.add(new Link<Void>("cancelEdit") {
				private static final long serialVersionUID = -2380441384773838117L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(candidate.getElection().getElectionId()));
				}
			});

		} catch (Exception e) {
			appLogger.error(e);
		}
	}


	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public byte[] getPictureInfo() {
		return pictureInfo;
	}

	public void setPictureInfo(byte[] pictureInfo) {
		this.pictureInfo = pictureInfo;
	}

}
