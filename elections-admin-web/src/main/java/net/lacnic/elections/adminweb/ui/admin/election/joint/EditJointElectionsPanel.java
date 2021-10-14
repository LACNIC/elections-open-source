package net.lacnic.elections.adminweb.ui.admin.election.joint;

import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.JointElection;


public class EditJointElectionsPanel extends Panel {

	private static final long serialVersionUID = -7732839509000652567L;

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
				long electionIdA = Long.parseLong(selectedElectionA.split("-")[0]);
				long electionIdB = Long.parseLong(selectedElectionB.split("-")[0]);

				Election electionA = AppContext.getInstance().getManagerBeanRemote().getElection(electionIdA); 
				Election electionB = AppContext.getInstance().getManagerBeanRemote().getElection(electionIdB);

				Date startDateElectionA = electionA.getStartDate();
				Date startDateElectionB = electionB.getStartDate();

				if (selectedElectionA.equals(selectedElectionB)) {
					error(getString("uniteElecEditError1"));
				} else if (AppContext.getInstance().getManagerBeanRemote().isJointElection(electionIdA)) {
					error(getString("uniteElecEditError2") + selectedElectionA);
				} else if (AppContext.getInstance().getManagerBeanRemote().isJointElection(electionIdB)) {
					error(getString("uniteElecEditError2") + selectedElectionB);
				} else if (startDateElectionA.compareTo(startDateElectionB) != 0) {
					error(getString("uniteElecEditError3"));
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
