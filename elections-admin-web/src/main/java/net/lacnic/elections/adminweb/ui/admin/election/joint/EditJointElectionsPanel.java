package net.lacnic.elections.adminweb.ui.admin.election.joint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;


public class EditJointElectionsPanel extends Panel {

	private static final long serialVersionUID = -7732839509000652567L;
	private static final String SELECTION_SEPARATOR = "-";
	private static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";
	private static final String UTC_TIME_ZONE = "UTC";

	private List<String> elections = AppContext.getInstance().getManagerBeanRemote().getElectionsAllIdAndTitle();
	private String selectedElectionA;
	private String selectedElectionB;

	public EditJointElectionsPanel(String id) {
		super(id);

		Form<Void> form = new Form<>("editJointElectionsForm");
		add(form);

		DropDownChoice<String> electionChoiceA = new DropDownChoice<>("electionA", new PropertyModel<>(this, "selectedElectionA"), elections);
		form.add(electionChoiceA);
		electionChoiceA.setRequired(true);

		DropDownChoice<String> electionChoiceB = new DropDownChoice<>("electionB", new PropertyModel<>(this, "selectedElectionB"), elections);
		form.add(electionChoiceB);
		electionChoiceB.setRequired(true);

		form.add(new Button("add") {
			private static final long serialVersionUID = -7498741440211186159L;

			@Override
			public void onSubmit() {
				long electionIdA = parseElectionId(selectedElectionA);
				long electionIdB = parseElectionId(selectedElectionB);

				Election electionA = AppContext.getInstance().getManagerBeanRemote().getElection(electionIdA); 
				Election electionB = AppContext.getInstance().getManagerBeanRemote().getElection(electionIdB);
				String electionLabelA = buildElectionLabel(electionA);
				String electionLabelB = buildElectionLabel(electionB);

				Date startDateElectionA = resolveVotingStartDate(electionA.getElectionId());
				Date startDateElectionB = resolveVotingStartDate(electionB.getElectionId());

				if (electionIdA == electionIdB) {
					error(new StringResourceModel("uniteElecEditError1", EditJointElectionsPanel.this, null)
							.setParameters(electionLabelA)
							.getString());
				} else if (AppContext.getInstance().getManagerBeanRemote().isJointElection(electionIdA)) {
					error(buildAlreadyJoinedError(electionIdA, electionLabelA));
				} else if (AppContext.getInstance().getManagerBeanRemote().isJointElection(electionIdB)) {
					error(buildAlreadyJoinedError(electionIdB, electionLabelB));
				} else if (startDateElectionA == null || startDateElectionB == null) {
					List<String> missingStartDateElections = new ArrayList<>();
					if (startDateElectionA == null) {
						missingStartDateElections.add(electionLabelA);
					}
					if (startDateElectionB == null) {
						missingStartDateElections.add(electionLabelB);
					}
					error(new StringResourceModel("uniteElecEditError4", EditJointElectionsPanel.this, null)
							.setParameters(String.join(", ", missingStartDateElections))
							.getString());
				} else if (startDateElectionA.compareTo(startDateElectionB) != 0) {
					error(new StringResourceModel("uniteElecEditError3", EditJointElectionsPanel.this, null)
							.setParameters(electionLabelA, formatUtcDateTime(startDateElectionA), electionLabelB, formatUtcDateTime(startDateElectionB))
							.getString());
				} else {
					JointElection supra = new JointElection();
					supra.setIdElectionA(electionIdA);
					supra.setIdElectionB(electionIdB);
					AppContext.getInstance().getManagerBeanRemote().updateJointElection(supra);
					setResponsePage(new JointElectionsDashboard());
				}
			}
		});
	}

	private long parseElectionId(String selectedElection) {
		int separatorPosition = selectedElection.indexOf(SELECTION_SEPARATOR);
		String electionId = separatorPosition >= 0 ? selectedElection.substring(0, separatorPosition) : selectedElection;
		return Long.parseLong(electionId.trim());
	}

	private String buildElectionLabel(Election election) {
		if (election == null) {
			return "";
		}
		return election.getElectionId() + " - " + election.getTitleSpanish();
	}

	private String buildAlreadyJoinedError(long electionId, String electionLabel) {
		JointElection jointElection = AppContext.getInstance().getManagerBeanRemote().getJointElectionForElection(electionId);
		long linkedElectionId = jointElection.getIdElectionA() == electionId ? jointElection.getIdElectionB() : jointElection.getIdElectionA();
		Election linkedElection = AppContext.getInstance().getManagerBeanRemote().getElection(linkedElectionId);
		String linkedElectionLabel = buildElectionLabel(linkedElection);

		return new StringResourceModel("uniteElecEditError2", this, null)
				.setParameters(electionLabel, linkedElectionLabel)
				.getString();
	}

	private Date resolveVotingStartDate(long electionId) {
		List<ElectionCalendar> calendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(electionId);
		for (ElectionCalendar calendar : calendars) {
			if (calendar != null && ElectionCalendarKey.N_16_PERIODO_VOTING.equals(calendar.getCalendarKey())) {
				return calendar.getStartDate();
			}
		}
		return null;
	}

	private String formatUtcDateTime(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_PATTERN);
		formatter.setTimeZone(TimeZone.getTimeZone(UTC_TIME_ZONE));
		return formatter.format(date) + " UTC";
	}


	public String getSelectedElectionA() {
		return selectedElectionA;
	}

	public void setSelectedElectionA(String selectedElectionA) {
		this.selectedElectionA = selectedElectionA;
	}

	public String getSelectedElectionB() {
		return selectedElectionB;
	}

	public void setSelectedElectionB(String selectedElectionB) {
		this.selectedElectionB = selectedElectionB;
	}

}
