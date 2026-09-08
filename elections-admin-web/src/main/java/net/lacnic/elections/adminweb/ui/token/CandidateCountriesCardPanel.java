package net.lacnic.elections.adminweb.ui.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidateCountriesCardPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CandidateCountriesCardPanel(String id, String badgeText, String badgeCssClass, List<? extends CountryCardRowView> rows, boolean showNoRegionalCitizenshipWarning, String noRegionalCitizenshipWarningMessage) {
		super(id);

		Label completionBadge = new Label("completionBadge", valueOrDash(badgeText));
		completionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(badgeCssClass)));
		add(completionBadge);

		List<CountryCardRowView> primaryRows = filterRows(rows, true);
		List<CountryCardRowView> secondaryRows = filterRows(rows, false);

		WebMarkupContainer primaryCountrySection = new WebMarkupContainer("primaryCountrySection");
		primaryCountrySection.setVisible(!primaryRows.isEmpty());
		primaryCountrySection.add(buildCountryRowsView("primaryCountryRows", primaryRows));
		add(primaryCountrySection);

		WebMarkupContainer secondaryCountrySection = new WebMarkupContainer("secondaryCountrySection");
		secondaryCountrySection.setVisible(!secondaryRows.isEmpty());
		secondaryCountrySection.add(buildCountryRowsView("secondaryCountryRows", secondaryRows));
		add(secondaryCountrySection);

		Label noRegionalWarning = new Label("noRegionalCitizenshipWarning", valueOrDash(noRegionalCitizenshipWarningMessage));
		noRegionalWarning.setVisible(showNoRegionalCitizenshipWarning);
		add(noRegionalWarning);
	}

	private ListView<CountryCardRowView> buildCountryRowsView(String id, List<CountryCardRowView> rows) {
		return new ListView<CountryCardRowView>(id, rows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CountryCardRowView> item) {
				CountryCardRowView row = item.getModelObject();
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, 
						row.isPrimaryCountry()
								? "border rounded p-3 mb-3 border-primary-subtle bg-primary-subtle bg-opacity-10"
								: "border rounded p-3 mb-3"));
				item.add(new Label("countryLabel", valueOrDash(row.getCountryLabel())));

				Label restrictedCountryBadge = new Label("restrictedCountryBadge", "Este país tiene limitaciones en esta elección.");
				restrictedCountryBadge.setOutputMarkupPlaceholderTag(true);
				restrictedCountryBadge.setVisible(row.isRestrictedCountry());
				restrictedCountryBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge bg-danger-subtle text-danger border border-danger-subtle"));
				item.add(restrictedCountryBadge);

				Label primaryBadge = new Label("primaryBadge", row.isPrimaryCountry() ? "Principal" : "Secundario");
				primaryBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.isPrimaryCountry()
						? "badge bg-primary-subtle text-primary"
						: "badge bg-light-subtle text-muted border"));
				item.add(primaryBadge);

				item.add(new Label("qCitizen", valueOrDash(row.getQCitizen())));
				item.add(new Label("qResidenceOver5y", valueOrDash(row.getQResidenceOver5y())));
				item.add(new Label("qLongEmploymentOrAdvisory5y", valueOrDash(row.getQLongEmploymentOrAdvisory5y())));
				item.add(new Label("qFamilyResidenceOver5y", valueOrDash(row.getQFamilyResidenceOver5y())));
				item.add(new Label("qInternetCommunityOrgParticipation", valueOrDash(row.getQInternetCommunityOrgParticipation())));
				item.add(new Label("qEligibleForCitizenship", valueOrDash(row.getQEligibleForCitizenship())));
			}
		};
	}

	private List<CountryCardRowView> filterRows(List<? extends CountryCardRowView> source, boolean primary) {
		List<CountryCardRowView> rows = new ArrayList<>();
		if (source == null) {
			return rows;
		}
		for (CountryCardRowView row : source) {
			if (row != null && row.isPrimaryCountry() == primary) {
				rows.add(row);
			}
		}
		return rows;
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
