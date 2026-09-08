package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.Arrays;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.page.PublicElectionPage;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.validators.LinkValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.utils.FilesUtils;
import net.lacnic.elections.utils.LinksUtils;

public class AddCandidatePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

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

			WebMarkupContainer mailContainer = new WebMarkupContainer("mailContainer");
			add(mailContainer);
			TextField<String> mail = new TextField<>("mail", new PropertyModel<>(candidate, "mail"));
			mail.setRequired(true);
			mail.add(EmailAddressValidator.getInstance());
			mailContainer.add(mail);

			WebMarkupContainer reminderFrequencyContainer = new WebMarkupContainer("reminderFrequencyContainer");
			add(reminderFrequencyContainer);
			DropDownChoice<ReminderFrequency> reminderFrequency = new DropDownChoice<>(
					"reminderFrequency",
					new PropertyModel<>(candidate, "reminderFrequency"),
					Arrays.asList(ReminderFrequency.values()),
					REMINDER_FREQUENCY_RENDERER);
			reminderFrequency.setNullValid(false);
			reminderFrequency.setRequired(true);
			reminderFrequencyContainer.add(reminderFrequency);

			addBios();

			final FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
			add(candidatePictureUploadField);

		SubmitLink addCandidateButton = new SubmitLink("addCandidate") {
			private static final long serialVersionUID = -8747001950049912880L;

				@Override
				public void onSubmit() {
					try {
						FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
						if (fileUpload != null) {
							CandidatePictureUploadValidator.PictureUploadResult pictureUploadResult = CandidatePictureUploadValidator.validateAndBuildForCandidate(fileUpload);
							if (!pictureUploadResult.isValid()) {
								handlePictureUploadError(pictureUploadResult.getFailureReason());
								return;
							}
							candidate.setPictureInfo(pictureUploadResult.getPictureInfo());
							candidate.setPictureName(pictureUploadResult.getPictureName());
							candidate.setPictureExtension(pictureUploadResult.getPictureExtension());
						} else {
							applyDefaultCandidatePicture();
						}
						candidate.copyBioToOtherLanguages();
						AppContext.getInstance().getManagerBeanRemote().addCandidate(election.getElectionId(), candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("candidateManagemenSuccessAdd"));
						setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
					} catch (Exception e) {
						getSession().error(getString("candidateManagemenErrorProc"));
					}
				}
		};
		addCandidateButton.add(new Label("submitLabel", getString("candidateEditBtnSave")));
		add(addCandidateButton);
		add(new org.apache.wicket.markup.html.link.Link<Void>("cancelAddCandidate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private void handlePictureUploadError(CandidatePictureUploadValidator.FailureReason failureReason) {
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_SIZE) {
			getSession().error(getString("candidateProfilePhotoSizeError"));
			return;
		}
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_FORMAT) {
			getSession().error(getString("candidateManagementErrorForm"));
			return;
		}
		getSession().error(getString("candidateProfileProcessingError"));
	}

	private void applyDefaultCandidatePicture() throws Exception {
		ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
		String filePath = context.getRealPath("/");
		Object[] defaultPhoto = FilesUtils.getDefaultPhoto(filePath);
		candidate.setPictureInfo((byte[]) defaultPhoto[0]);
		candidate.setPictureName((String) defaultPhoto[1]);
		candidate.setPictureExtension((String) defaultPhoto[2]);
	}

	private void addBios() {
		TextArea<String> bioSpanish = new TextArea<>("bioSpanish", new PropertyModel<>(candidate, "bioSpanish"));
		bioSpanish.add(StringValidator.maximumLength(2000));
		bioSpanish.add(new LinkValidator());
		bioSpanish.setRequired(true);
		add(bioSpanish);

		TextField<String> linkSpanish = new TextField<>("linkSpanish", new PropertyModel<>(candidate, "linkSpanish"));
		linkSpanish.add(StringValidator.maximumLength(1000));
		linkSpanish.add(new UrlValidator());
		add(linkSpanish);
		add(new Label("linkSpanishHelp", buildCandidateProfileLinkHelp(buildPublicCandidateProfileLink(candidate))));

		TextField<String> linkedinUrl = new TextField<>("linkedinUrl", new PropertyModel<>(candidate, "linkedinUrl"));
		linkedinUrl.add(StringValidator.maximumLength(1000));
		linkedinUrl.add(new UrlValidator());
		add(linkedinUrl);
	}

	private String buildCandidateProfileLinkHelp(String defaultProfileLink) {
		String resourceKey = hasText(defaultProfileLink) ? "candidateProfileLinkHelp" : "candidateProfileLinkHelpNoUrl";
		return new StringResourceModel(resourceKey, this, null).setParameters(defaultProfileLink).getString();
	}

	private String buildPublicCandidateProfileLink(Candidate currentCandidate) {
		if (currentCandidate == null || currentCandidate.getElection() == null || currentCandidate.getCandidateId() <= 0L) {
			return "";
		}
		String publicElectionToken = currentCandidate.getElection().getPublicElectionToken();
		if (!hasText(publicElectionToken)) {
			return "";
		}
		return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, currentCandidate.getCandidateId());
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private final IChoiceRenderer<ReminderFrequency> REMINDER_FREQUENCY_RENDERER = new IChoiceRenderer<ReminderFrequency>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(ReminderFrequency object) {
			return getString("reminderFrequency." + object.name());
		}

		@Override
		public String getIdValue(ReminderFrequency object, int index) {
			return object.name();
		}
	};

}
