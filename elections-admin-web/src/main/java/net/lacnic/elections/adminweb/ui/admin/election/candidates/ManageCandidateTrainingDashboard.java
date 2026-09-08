package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.campus.CampusClient;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class ManageCandidateTrainingDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = -9084166164636771472L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Candidate candidate;
	private CandidateCampusCourseStatus campusCourseStatus;
	private CandidateEvaluationStatus evaluationStatus;
	private String campusCourseCalification;
	private Boolean removeProctorioFile = Boolean.FALSE;

	public ManageCandidateTrainingDashboard(PageParameters params) {
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

		campusCourseStatus = candidate.getCampusCourseStatus();
		evaluationStatus = candidate.getEvaluationStatus();
		campusCourseCalification = candidate.getCampusCourseCalification();

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));
		final boolean campusConfigured = CampusClient.isCampusIntegrationEnabled();
		WebMarkupContainer campusSummary = new WebMarkupContainer("campusSummary");
		campusSummary.setVisible(campusConfigured);
		campusSummary.add(new Label("currentCampusStatus", new ResourceModel(resolveCampusStatusKey(candidate.getCampusCourseStatus()))));
		campusSummary.add(new Label("currentCampusSelectedCourse", valueOrDash(candidate.getCampusCourseSelected())));
		campusSummary.add(new Label("currentCampusCalification", valueOrDash(candidate.getCampusCourseCalification())));
		add(campusSummary);
		add(new Label("currentEvaluationStatus", new ResourceModel(resolveEvaluationStatusKey(candidate.getEvaluationStatus()))));

		byte[] proctorioResultFile = candidate.getProctorioResultFile();
		boolean hasProctorioFile = proctorioResultFile != null && proctorioResultFile.length > 0;
		String proctorioContentType = ProctorioFileSupport.resolveContentType(proctorioResultFile);
		String proctorioFileName = ProctorioFileSupport.resolveFileName(candidate.getCandidateId(), proctorioResultFile);
		ResourceLink<Void> proctorioDownload = new ResourceLink<>(
				"proctorioDownload",
				new ByteArrayResource(proctorioContentType, hasProctorioFile ? proctorioResultFile : new byte[0], proctorioFileName));
		proctorioDownload.setVisible(hasProctorioFile);
		add(proctorioDownload);
		add(new Label("proctorioDownloadUnavailable", new ResourceModel("candidateTrainingProctorioNoFile")).setVisible(!hasProctorioFile));

		Form<Void> trainingForm = new Form<>("trainingForm");
		trainingForm.setMultiPart(true);
		trainingForm.setMaxSize(Bytes.megabytes(10));
		add(trainingForm);

		List<CandidateCampusCourseStatus> statusOptions = Arrays.asList(CandidateCampusCourseStatus.values());
		DropDownChoice<CandidateCampusCourseStatus> campusCourseStatusField = new DropDownChoice<>(
				"campusCourseStatus",
				new PropertyModel<>(this, "campusCourseStatus"),
				statusOptions,
				CAMPUS_STATUS_RENDERER);
		campusCourseStatusField.setNullValid(true);
		WebMarkupContainer campusStatusField = new WebMarkupContainer("campusStatusField");
		campusStatusField.setVisible(campusConfigured);
		campusStatusField.add(campusCourseStatusField);
		trainingForm.add(campusStatusField);

		List<CandidateEvaluationStatus> evaluationStatusOptions = Arrays.asList(CandidateEvaluationStatus.values());
		DropDownChoice<CandidateEvaluationStatus> evaluationStatusField = new DropDownChoice<>(
				"evaluationStatus",
				new PropertyModel<>(this, "evaluationStatus"),
				evaluationStatusOptions,
				EVALUATION_STATUS_RENDERER);
		evaluationStatusField.setNullValid(true);
		trainingForm.add(evaluationStatusField);

		TextField<String> campusCourseCalificationField = new TextField<>("campusCourseCalification", new PropertyModel<>(this, "campusCourseCalification"));
		campusCourseCalificationField.add(StringValidator.maximumLength(255));
		WebMarkupContainer campusCalificationField = new WebMarkupContainer("campusCalificationField");
		campusCalificationField.setVisible(campusConfigured);
		campusCalificationField.add(campusCourseCalificationField);
		trainingForm.add(campusCalificationField);

		FileUploadField proctorioResultFileField = new FileUploadField("proctorioResultFile");
		trainingForm.add(proctorioResultFileField);

		CheckBox removeProctorioFileField = new CheckBox("removeProctorioFile", new PropertyModel<>(this, "removeProctorioFile"));
		trainingForm.add(removeProctorioFileField);

		trainingForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				try {
					FileUpload fileUpload = proctorioResultFileField.getFileUpload();
					if (Boolean.TRUE.equals(removeProctorioFile)) {
						candidate.setProctorioResultFile(null);
					}
					if (fileUpload != null && fileUpload.getSize() > 0) {
						candidate.setProctorioResultFile(fileUpload.getBytes());
					}

					if (campusConfigured) {
						candidate.setCampusCourseStatus(campusCourseStatus);
						if (campusCourseStatus == CandidateCampusCourseStatus.PENDING) {
							candidate.setCampusCourseSelected(null);
						}
						candidate.setCampusCourseCalification(hasText(campusCourseCalification) ? campusCourseCalification.trim() : null);
					}
					candidate.setEvaluationStatus(evaluationStatus);
					AppContext.getInstance().getManagerBeanRemote().editCandidate(candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

					getSession().info(getString("candidateTrainingSaveSuccess"));
					if (shouldMarkTrainingTaskCompleted(campusCourseStatus)) {
						if (markTrainingTaskCompleted(electionId, candidate.getCandidateId())) {
							getSession().info(getString("candidateTrainingTaskMarkedCompleted"));
						} else {
							getSession().warn(getString("candidateTrainingTaskMarkedCompletedWarning"));
						}
					}
					if (shouldMarkEvaluationTaskCompleted(evaluationStatus)) {
						if (markEvaluationTaskCompleted(electionId, candidate.getCandidateId())) {
							getSession().info(getString("candidateTrainingEvaluationTaskMarkedCompleted"));
						} else {
							getSession().warn(getString("candidateTrainingEvaluationTaskMarkedCompletedWarning"));
						}
					}
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
				} catch (Exception e) {
					appLogger.error(e.getMessage(), e);
					getSession().error(getString("candidateManagemenErrorProc"));
				}
			}
		});

		trainingForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private boolean isValidCandidate(Candidate candidate, long electionId) {
		return candidate != null
				&& !candidate.isAbstention()
				&& candidate.getElection() != null
				&& candidate.getElection().getElectionId() == electionId;
	}

	private String resolveCampusStatusLabel(CandidateCampusCourseStatus status) {
		return getString(resolveCampusStatusKey(status));
	}

	private String resolveCampusStatusKey(CandidateCampusCourseStatus status) {
		if (status == null) {
			return "candidateManagemenListCampusNoStatus";
		}
		switch (status) {
		case SENT:
			return "candidateTrainingCampusStatusSent";
		case STARTED:
			return "candidateTrainingCampusStatusStarted";
		case COMPLETED:
			return "candidateTrainingCampusStatusCompleted";
		case NOT_APPLICABLE:
			return "candidateTrainingCampusStatusNotApplicable";
		case PENDING:
		default:
			return "candidateTrainingCampusStatusPending";
		}
	}

	private String resolveEvaluationStatusLabel(CandidateEvaluationStatus status) {
		return getString(resolveEvaluationStatusKey(status));
	}

	private String resolveEvaluationStatusKey(CandidateEvaluationStatus status) {
		if (status == null) {
			return "candidateManagemenListCampusNoStatus";
		}
		switch (status) {
		case CREDENTIALS_REQUESTED:
			return "candidateTrainingEvaluationStatusRequested";
		case CREDENTIALS_SENT:
			return "candidateTrainingEvaluationStatusSent";
		case COMPLETED:
			return "candidateTrainingEvaluationStatusCompleted";
		case NOT_APPLICABLE:
			return "candidateTrainingEvaluationStatusNotApplicable";
		case PENDING:
		default:
			return "candidateTrainingEvaluationStatusPending";
		}
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String valueOrDash(Long value) {
		return value != null ? String.valueOf(value) : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean shouldMarkTrainingTaskCompleted(CandidateCampusCourseStatus status) {
		return status == CandidateCampusCourseStatus.COMPLETED || status == CandidateCampusCourseStatus.NOT_APPLICABLE;
	}

	private boolean shouldMarkEvaluationTaskCompleted(CandidateEvaluationStatus status) {
		return status == CandidateEvaluationStatus.COMPLETED || status == CandidateEvaluationStatus.NOT_APPLICABLE;
	}

	private boolean markTrainingTaskCompleted(long electionId, long candidateId) {
		String token = findNominationToken(electionId, candidateId);
		if (!hasText(token)) {
			return false;
		}
		return AppContext.getInstance().getPreNominationBeanRemote().updateCandidateTaskStatus(
				token,
				ElectionTaskKey.COURSE,
				CandidateElectionTaskStatus.COMPLETED,
				SecurityUtils.getClientIp());
	}

	private boolean markEvaluationTaskCompleted(long electionId, long candidateId) {
		String token = findNominationToken(electionId, candidateId);
		if (!hasText(token)) {
			return false;
		}
		return AppContext.getInstance().getPreNominationBeanRemote().updateCandidateTaskStatus(
				token,
				ElectionTaskKey.EVALUATION,
				CandidateElectionTaskStatus.COMPLETED,
				SecurityUtils.getClientIp());
	}

	private String findNominationToken(long electionId, long candidateId) {
		List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(electionId);
		if (nominations == null || nominations.isEmpty()) {
			return null;
		}
		for (Nomination nomination : nominations) {
			if (nomination == null || nomination.getCandidate() == null) {
				continue;
			}
			if (nomination.getCandidate().getCandidateId() == candidateId) {
				return nomination.getAcceptNominationToken();
			}
		}
		return null;
	}

	private final IChoiceRenderer<CandidateCampusCourseStatus> CAMPUS_STATUS_RENDERER = new IChoiceRenderer<CandidateCampusCourseStatus>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(CandidateCampusCourseStatus object) {
			return resolveCampusStatusLabel(object);
		}

		@Override
		public String getIdValue(CandidateCampusCourseStatus object, int index) {
			return object != null ? object.name() : "";
		}
	};

	private final IChoiceRenderer<CandidateEvaluationStatus> EVALUATION_STATUS_RENDERER = new IChoiceRenderer<CandidateEvaluationStatus>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(CandidateEvaluationStatus object) {
			return resolveEvaluationStatusLabel(object);
		}

		@Override
		public String getIdValue(CandidateEvaluationStatus object, int index) {
			return object != null ? object.name() : "";
		}
	};

	public CandidateCampusCourseStatus getCampusCourseStatus() {
		return campusCourseStatus;
	}

	public void setCampusCourseStatus(CandidateCampusCourseStatus campusCourseStatus) {
		this.campusCourseStatus = campusCourseStatus;
	}

	public CandidateEvaluationStatus getEvaluationStatus() {
		return evaluationStatus;
	}

	public void setEvaluationStatus(CandidateEvaluationStatus evaluationStatus) {
		this.evaluationStatus = evaluationStatus;
	}

	public String getCampusCourseCalification() {
		return campusCourseCalification;
	}

	public void setCampusCourseCalification(String campusCourseCalification) {
		this.campusCourseCalification = campusCourseCalification;
	}

	public Boolean getRemoveProctorioFile() {
		return removeProctorioFile;
	}

	public void setRemoveProctorioFile(Boolean removeProctorioFile) {
		this.removeProctorioFile = removeProctorioFile;
	}

}
