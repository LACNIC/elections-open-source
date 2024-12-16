package net.lacnic.elections.adminweb.ui.admin.election;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class ManageElectionTabsPanel extends Panel {

	private static final long serialVersionUID = 7849505995342956937L;

	public ManageElectionTabsPanel(String id, Election election) {
		super(id);

		Link<Void> detail = new Link<Void>("detail") {
			private static final long serialVersionUID = 1791144645448735702L;

			@Override
			public void onClick() {
				setResponsePage(ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		detail.setEnabled(election.getElectionId() != 0);
		add(detail);

		Link<Void> census = new Link<Void>("census") {
			private static final long serialVersionUID = -846692535588349478L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		census.setEnabled(election.getElectionId() != 0);
		add(census);

		Link<Void> candidates = new Link<Void>("candidates") {
			private static final long serialVersionUID = 8317962582176759530L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		candidates.setEnabled(election.getElectionId() != 0);
		add(candidates);

		Link<Void> auditors = new Link<Void>("auditors") {
			private static final long serialVersionUID = 5621792633991539504L;

			@Override
			public void onClick() {
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		auditors.setEnabled(election.getElectionId() != 0);
		add(auditors);

		String atribute = "class";
		String current = " current";
		String done = " done";
		String disabled = " disabled";

		if (id.equalsIgnoreCase("tabDetail"))
			detail.add(new AttributeAppender(atribute, current));
		else
			detail.add(new AttributeAppender(atribute, done));

		if (id.equalsIgnoreCase("tabCensus"))
			census.add(new AttributeAppender(atribute, current));
		else if (election.isElectorsSet())
			census.add(new AttributeAppender(atribute, done));
		else
			census.add(new AttributeAppender(atribute, disabled));

		if (id.equalsIgnoreCase("tabCandidates"))
			candidates.add(new AttributeAppender(atribute, current));
		else if (election.isCandidatesSet())
			candidates.add(new AttributeAppender(atribute, done));
		else
			candidates.add(new AttributeAppender(atribute, disabled));

		if (id.equalsIgnoreCase("tabAuditors"))
			auditors.add(new AttributeAppender(atribute, current));
		else if (election.isAuditorsSet())
			auditors.add(new AttributeAppender(atribute, done));
		else
			auditors.add(new AttributeAppender(atribute, disabled));

	}

}
