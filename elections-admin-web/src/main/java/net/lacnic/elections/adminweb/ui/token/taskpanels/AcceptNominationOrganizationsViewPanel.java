package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.WorkOrganizationType;

public class AcceptNominationOrganizationsViewPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	private final List<CandidateWorkOrganization> workOrganizations = new ArrayList<>();
	private final Boolean hasOrganizationRelationship;

	public AcceptNominationOrganizationsViewPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));

		Candidate candidate = resolution.getCandidate();
		if (candidate != null && candidate.getWorkOrganizations() != null) {
			for (CandidateWorkOrganization organization : candidate.getWorkOrganizations()) {
				if (organization == null || StringUtils.isBlank(organization.getOrganizationName()) || organization.getWorkOrganizationType() == null) {
					continue;
				}
				CandidateWorkOrganization copy = new CandidateWorkOrganization();
				copy.setOrganizationName(StringUtils.trimToEmpty(organization.getOrganizationName()));
				copy.setOrganizationGroup(StringUtils.trimToNull(organization.getOrganizationGroup()));
				copy.setWorkOrganizationType(organization.getWorkOrganizationType());
				workOrganizations.add(copy);
			}
		}

		if (candidate == null) {
			hasOrganizationRelationship = null;
		} else if (Boolean.TRUE.equals(candidate.isQUnemployed())) {
			hasOrganizationRelationship = Boolean.FALSE;
		} else if (!workOrganizations.isEmpty()) {
			hasOrganizationRelationship = Boolean.TRUE;
		} else {
			hasOrganizationRelationship = null;
		}

		add(new Label("answerValue", buildAnswerLabel()));

		WebMarkupContainer organizationsTableContainer = new WebMarkupContainer("organizationsTableContainer");
		organizationsTableContainer.setVisible(Boolean.TRUE.equals(hasOrganizationRelationship));
		add(organizationsTableContainer);

		Label noOrganizationsLabel = new Label("noOrganizationsLabel", getString("acceptNominationOrganizationsNoRows"));
		noOrganizationsLabel.setVisible(workOrganizations.isEmpty());
		organizationsTableContainer.add(noOrganizationsLabel);

		organizationsTableContainer.add(new ListView<CandidateWorkOrganization>("organizationsRows", workOrganizations) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CandidateWorkOrganization> item) {
				CandidateWorkOrganization organization = item.getModelObject();
				item.add(new Label("organizationNameCell", StringUtils.defaultString(organization.getOrganizationName())));
				item.add(new Label("organizationGroupCell", StringUtils.defaultIfBlank(organization.getOrganizationGroup(), "-")));
				item.add(new Label("organizationTypeCell", getOrganizationTypeLabel(organization.getWorkOrganizationType())));
			}
		});
	}

	private String buildAnswerLabel() {
		if (hasOrganizationRelationship == null) {
			return getString("acceptNominationOrganizationsAnswerNotProvided");
		}
		return hasOrganizationRelationship ? getString("acceptNominationOrganizationsOptionYes") : getString("acceptNominationOrganizationsOptionNo");
	}

	private String getOrganizationTypeLabel(WorkOrganizationType type) {
		if (type == WorkOrganizationType.PAID) {
			return getString("acceptNominationOrganizationsTypePaidShort");
		}
		if (type == WorkOrganizationType.AD_HONOREM) {
			return getString("acceptNominationOrganizationsTypeAdHonoremShort");
		}
		return "";
	}
}
