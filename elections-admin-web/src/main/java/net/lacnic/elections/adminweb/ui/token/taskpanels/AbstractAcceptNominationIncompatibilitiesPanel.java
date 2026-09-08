package net.lacnic.elections.adminweb.ui.token.taskpanels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public abstract class AbstractAcceptNominationIncompatibilitiesPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private Candidate candidate;
	private final String token;

	private Boolean qCanSpeakSpanish;
	private Boolean qLegalLimitationAnyCountry;
	private Boolean qHealthTravelLimitation;
	private Boolean qHealthMentalLimitation;
	private Boolean qAdultInCountry;
	private Boolean qCivilRightsLimitation;

	protected AbstractAcceptNominationIncompatibilitiesPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		add(buildTaskTitle("cardTitle"));

		loadFormState();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> incompatibilitiesForm = new Form<>("incompatibilitiesForm");
		add(incompatibilitiesForm);

		incompatibilitiesForm.add(buildBooleanGroup("qAdultInCountryGroup", new PropertyModel<>(this, "qAdultInCountry"),
				getString("acceptNominationIncompatibilitiesQuestionAdultInCountry")));
		incompatibilitiesForm.add(buildBooleanGroup("qCanSpeakSpanishGroup", new PropertyModel<>(this, "qCanSpeakSpanish"),
				getString("acceptNominationIncompatibilitiesQuestionCanSpeakSpanish")));
		incompatibilitiesForm.add(buildBooleanGroup("qCivilRightsLimitationGroup", new PropertyModel<>(this, "qCivilRightsLimitation"),
				getString("acceptNominationIncompatibilitiesQuestionCivilRightsLimitation")));
		incompatibilitiesForm.add(buildBooleanGroup("qLegalLimitationAnyCountryGroup", new PropertyModel<>(this, "qLegalLimitationAnyCountry"),
				getString("acceptNominationIncompatibilitiesQuestionLegalLimitationAnyCountry")));
		incompatibilitiesForm.add(buildBooleanGroup("qHealthTravelLimitationGroup", new PropertyModel<>(this, "qHealthTravelLimitation"),
				getString("acceptNominationIncompatibilitiesQuestionHealthTravelLimitation")));
		incompatibilitiesForm.add(buildBooleanGroup("qHealthMentalLimitationGroup", new PropertyModel<>(this, "qHealthMentalLimitation"),
				getString("acceptNominationIncompatibilitiesQuestionHealthMentalLimitation")));

		Button backButton = new Button("backButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				setResponsePage(GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token));
			}
		};
		backButton.setDefaultFormProcessing(false);
		incompatibilitiesForm.add(backButton);

		Button finishAndSendButton = new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (isReadOnlyMode()) {
					return;
				}
				if (!persistIncompatibilities()) {
					error(getString("acceptNominationIncompatibilitiesSaveError"));
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		};
		finishAndSendButton.setVisible(!isReadOnlyMode());
		incompatibilitiesForm.add(finishAndSendButton);
	}

	private RadioGroup<Boolean> buildBooleanGroup(String id, PropertyModel<Boolean> model, String label) {
		RadioGroup<Boolean> group = new RadioGroup<>(id, model);
		group.add(new Radio<>("yes", Model.of(Boolean.TRUE)));
		group.add(new Radio<>("no", Model.of(Boolean.FALSE)));
		group.setEnabled(!isReadOnlyMode());
		group.setRequired(!isReadOnlyMode());
		group.setLabel(Model.of(label));
		return group;
	}

	private void loadFormState() {
		if (candidate == null) {
			qAdultInCountry = null;
			qCanSpeakSpanish = null;
			qCivilRightsLimitation = null;
			qLegalLimitationAnyCountry = null;
			qHealthTravelLimitation = null;
			qHealthMentalLimitation = null;
			return;
		}

		// If the task was never touched, force explicit Yes/No selection for all questions.
		if (isCompleteMode() && getSelectedTask() != null && getSelectedTask().getStartDate() == null && getSelectedTask().getEndDate() == null) {
			qAdultInCountry = null;
			qCanSpeakSpanish = null;
			qCivilRightsLimitation = null;
			qLegalLimitationAnyCountry = null;
			qHealthTravelLimitation = null;
			qHealthMentalLimitation = null;
			return;
		}

		qAdultInCountry = candidate.getQAdultInCountry();
		qCanSpeakSpanish = candidate.getQCanSpeakSpanish();
		qCivilRightsLimitation = candidate.getQCivilRightsLimitation();
		qLegalLimitationAnyCountry = candidate.getQLegalLimitationAnyCountry();
		qHealthTravelLimitation = candidate.getQHealthTravelLimitation();
		qHealthMentalLimitation = candidate.getQHealthMentalLimitation();
	}

	private boolean persistIncompatibilities() {
		try {
			Candidate candidateData = new Candidate();
			candidateData.setQAdultInCountry(isTrue(qAdultInCountry));
			candidateData.setQCanSpeakSpanish(isTrue(qCanSpeakSpanish));
			candidateData.setQCivilRightsLimitation(isTrue(qCivilRightsLimitation));
			candidateData.setQLegalLimitationAnyCountry(isTrue(qLegalLimitationAnyCountry));
			candidateData.setQHealthTravelLimitation(isTrue(qHealthTravelLimitation));
			candidateData.setQHealthMentalLimitation(isTrue(qHealthMentalLimitation));

			Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateIncompatibilities(token, candidateData, CANDIDATE_LINK_ACTIVITY_ACTOR,
					SecurityUtils.getClientIp());
			if (updatedCandidate == null) {
				return false;
			}

			candidate = updatedCandidate;
			loadFormState();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean isTrue(Boolean value) {
		return Boolean.TRUE.equals(value);
	}

	public Boolean getQCanSpeakSpanish() {
		return qCanSpeakSpanish;
	}

	public void setQCanSpeakSpanish(Boolean qCanSpeakSpanish) {
		this.qCanSpeakSpanish = qCanSpeakSpanish;
	}

	public Boolean getQLegalLimitationAnyCountry() {
		return qLegalLimitationAnyCountry;
	}

	public void setQLegalLimitationAnyCountry(Boolean qLegalLimitationAnyCountry) {
		this.qLegalLimitationAnyCountry = qLegalLimitationAnyCountry;
	}

	public Boolean getQHealthTravelLimitation() {
		return qHealthTravelLimitation;
	}

	public void setQHealthTravelLimitation(Boolean qHealthTravelLimitation) {
		this.qHealthTravelLimitation = qHealthTravelLimitation;
	}

	public Boolean getQHealthMentalLimitation() {
		return qHealthMentalLimitation;
	}

	public void setQHealthMentalLimitation(Boolean qHealthMentalLimitation) {
		this.qHealthMentalLimitation = qHealthMentalLimitation;
	}

	public Boolean getQAdultInCountry() {
		return qAdultInCountry;
	}

	public void setQAdultInCountry(Boolean qAdultInCountry) {
		this.qAdultInCountry = qAdultInCountry;
	}

	public Boolean getQCivilRightsLimitation() {
		return qCivilRightsLimitation;
	}

	public void setQCivilRightsLimitation(Boolean qCivilRightsLimitation) {
		this.qCivilRightsLimitation = qCivilRightsLimitation;
	}
}
