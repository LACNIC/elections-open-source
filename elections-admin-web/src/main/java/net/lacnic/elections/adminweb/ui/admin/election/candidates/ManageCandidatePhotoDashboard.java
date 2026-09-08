package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.Arrays;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.utils.FilesUtils;

public class ManageCandidatePhotoDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Candidate candidate;
	private byte[] originalPictureInfo;
	private String originalPictureName;
	private String originalPictureExtension;
	private boolean restoreDefaultPicture;

	public ManageCandidatePhotoDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long electionId = UtilsParameters.getIdAsLong(params);
		long candidateId = UtilsParameters.getCandidateAsLong(params);
		candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);

		if (!isValidCandidate(candidate, electionId)) {
			getSession().error(getString("candidateAnswersCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			return;
		}

		originalPictureInfo = candidate.getPictureInfo();
		originalPictureName = candidate.getPictureName();
		originalPictureExtension = candidate.getPictureExtension();

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));

		Form<Void> photoForm = new Form<>("photoForm");
		photoForm.setMultiPart(true);
		photoForm.setMaxSize(Bytes.bytes(CandidatePictureUploadValidator.MAX_CANDIDATE_INPUT_SIZE_BYTES));
		add(photoForm);

		FileUploadField candidatePictureUploadField = new FileUploadField("candidatePicture");
		photoForm.add(candidatePictureUploadField);
		photoForm.add(new CheckBox("restoreDefaultPicture", new PropertyModel<>(this, "restoreDefaultPicture")));

		photoForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistPhotoChanges(electionId, candidatePictureUploadField);
			}
		});

		photoForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private boolean isValidCandidate(Candidate loadedCandidate, long electionId) {
		return loadedCandidate != null
				&& loadedCandidate.getElection() != null
				&& loadedCandidate.getElection().getElectionId() == electionId;
	}

	private void persistPhotoChanges(long electionId, FileUploadField candidatePictureUploadField) {
		try {
			FileUpload fileUpload = candidatePictureUploadField.getFileUpload();
			if (!restoreDefaultPicture && fileUpload == null) {
				getSession().info(getString("candidatePhotoNoChanges"));
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
				return;
			}

			if (restoreDefaultPicture) {
				applyDefaultCandidatePicture();
			} else {
				CandidatePictureUploadValidator.PictureUploadResult pictureUploadResult = CandidatePictureUploadValidator.validateAndBuildForCandidate(fileUpload);
				if (!pictureUploadResult.isValid()) {
					handlePictureUploadError(pictureUploadResult.getFailureReason());
					return;
				}
				candidate.setPictureInfo(pictureUploadResult.getPictureInfo());
				candidate.setPictureName(pictureUploadResult.getPictureName());
				candidate.setPictureExtension(pictureUploadResult.getPictureExtension());
			}

			if (samePhotoState()) {
				getSession().info(getString("candidatePhotoNoChanges"));
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
				return;
			}

			AppContext.getInstance().getManagerBeanRemote().editCandidate(candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			getSession().info(getString("candidatePhotoSaveSuccess"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
		} catch (Exception e) {
			appLogger.error("Error updating candidate photo for candidateId={}", candidate.getCandidateId(), e);
			getSession().error(getString("candidatePhotoSaveError"));
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
		Object[] defaultPhoto = candidate.isAbstention() ? FilesUtils.getDefaultAbstentionPhoto(filePath) : FilesUtils.getDefaultPhoto(filePath);
		candidate.setPictureInfo((byte[]) defaultPhoto[0]);
		candidate.setPictureName((String) defaultPhoto[1]);
		candidate.setPictureExtension((String) defaultPhoto[2]);
	}

	private boolean samePhotoState() {
		return Arrays.equals(originalPictureInfo, candidate.getPictureInfo())
				&& sameValue(originalPictureName, candidate.getPictureName())
				&& sameValue(originalPictureExtension, candidate.getPictureExtension());
	}

	private boolean sameValue(String left, String right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.equals(right);
	}

	private String valueOrDash(String value) {
		return value != null && !value.trim().isEmpty() ? value : getString("candidateManagemenListNoLink");
	}

	public boolean isRestoreDefaultPicture() {
		return restoreDefaultPicture;
	}

	public void setRestoreDefaultPicture(boolean restoreDefaultPicture) {
		this.restoreDefaultPicture = restoreDefaultPicture;
	}
}
