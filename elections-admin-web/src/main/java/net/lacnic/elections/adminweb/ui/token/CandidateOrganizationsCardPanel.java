package net.lacnic.elections.adminweb.ui.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidateOrganizationsCardPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private transient final List<OrganizationCardRowView> organizationRows;
	private final Boolean hasOrganizationRelationship;

	public CandidateOrganizationsCardPanel(String id, String badgeText, String badgeCssClass, List<? extends OrganizationCardRowView> rows) {
		this(id, badgeText, badgeCssClass, rows, null);
	}

	public CandidateOrganizationsCardPanel(String id, String badgeText, String badgeCssClass, List<? extends OrganizationCardRowView> rows, Boolean hasOrganizationRelationship) {
		super(id);
		this.organizationRows = rows != null ? new ArrayList<>(rows) : new ArrayList<OrganizationCardRowView>();
		this.hasOrganizationRelationship = hasOrganizationRelationship != null
				? hasOrganizationRelationship
				: (this.organizationRows.isEmpty() ? null : Boolean.TRUE);

		Label completionBadge = new Label("completionBadge", valueOrDash(badgeText));
		completionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(badgeCssClass)));
		add(completionBadge);

		add(new Label("organizationQuestionLabel", getString("acceptNominationOrganizationsQuestion")));
		add(new Label("organizationAnswerValue", resolveOrganizationAnswerLabel()));
		add(new Label("organizationInfoText", resolveOrganizationInfoText()));

		add(new ListView<OrganizationCardRowView>("organizationRows", organizationRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<OrganizationCardRowView> item) {
				OrganizationCardRowView row = item.getModelObject();
				item.add(new Label("organizationName", valueOrDash(row.getName())));
				item.add(new Label("organizationGroup", valueOrDash(row.getGroup())));
				Label typeBadge = new Label("organizationTypeBadge", valueOrDash(row.getTypeBadgeText()));
				typeBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(row.getTypeBadgeClass())));
				item.add(typeBadge);
			}
		});
	}

	private String resolveOrganizationAnswerLabel() {
		if (hasOrganizationRelationship == null) {
			return getString("acceptNominationOrganizationsAnswerNotProvided");
		}
		return hasOrganizationRelationship.booleanValue()
				? getString("acceptNominationOrganizationsOptionYes")
				: getString("acceptNominationOrganizationsOptionNo");
	}

	private String resolveOrganizationInfoText() {
		if (Boolean.FALSE.equals(hasOrganizationRelationship)) {
			return getString("auditPublicOrganizationsInfoNo");
		}
		if (Boolean.TRUE.equals(hasOrganizationRelationship) && organizationRows.isEmpty()) {
			return getString("auditPublicOrganizationsInfoYesNoRows");
		}
		if (Boolean.TRUE.equals(hasOrganizationRelationship)) {
			return getString("auditPublicOrganizationsInfoYesWithRows");
		}
		return getString("auditPublicOrganizationsInfoNotAnswered");
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
