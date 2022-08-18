package net.lacnic.elections.adminweb.ui.admin.election.joint;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.domain.JointElection;


public class JointElectionsListPanel extends Panel {

	private static final long serialVersionUID = -3823908256532916047L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public JointElectionsListPanel(String id) {
		super(id);

		List<JointElection> jointElectionsList = AppContext.getInstance().getManagerBeanRemote().getJointElectionsAll();
		if(jointElectionsList == null)
			jointElectionsList = new ArrayList<>();
		init(jointElectionsList);
	}


	private void init(List<JointElection> jointElectionsList) {
		try {
			final ListView<JointElection> jointElectionsListView = new ListView<JointElection>("jointElectionsList", jointElectionsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<JointElection> item) {
					final JointElection current = item.getModelObject();
					try {
						item.add(new Label("electionA", AppContext.getInstance().getManagerBeanRemote().getElection(current.getIdElectionA()).getTitleSpanish()));
						item.add(new Label("electionB", AppContext.getInstance().getManagerBeanRemote().getElection(current.getIdElectionB()).getTitleSpanish()));

						ButtonDeleteWithConfirmation buttonDelete = new ButtonDeleteWithConfirmation("remove", current.getJointElectionId()) {
							private static final long serialVersionUID = 6986190296016629836L;

							@Override
							public void onConfirm() {
								try {
									AppContext.getInstance().getManagerBeanRemote().removeJointElection(current);
									getSession().info(getString("unitedElecListExitoDel"));
									setResponsePage(JointElectionsDashboard.class);
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(buttonDelete);
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			};
			add(jointElectionsListView);
		} catch (Exception e) {
			error(e.getMessage());
		}
	}

}
