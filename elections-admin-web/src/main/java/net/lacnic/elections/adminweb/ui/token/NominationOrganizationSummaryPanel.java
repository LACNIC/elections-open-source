package net.lacnic.elections.adminweb.ui.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.utils.CountryUtils;

public class NominationOrganizationSummaryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private final String token;
	private final ElectionType electionType;
	private final List<String> restrictedCountryCodes = new ArrayList<>();
	private final List<RestrictedCountryItem> restrictedCountries = new ArrayList<>();

	public NominationOrganizationSummaryPanel(String id, Organization organization, List<String> restrictedCountryCodes, String token) {
		super(id);
		this.token = token;
		this.electionType = organization != null && organization.getElection() != null ? organization.getElection().getEffectiveElectionType() : null;

		loadRestrictedCountries(restrictedCountryCodes);

		add(new Label("orgName", valueOrDash(organization != null ? organization.getName() : null)));
		add(new Label("orgId", valueOrDash(organization != null ? organization.getOrgId() : null)));
		add(new Label("membershipContactId", valueOrDash(organization != null ? organization.getMembershipContactId() : null)));
		add(new Label("membershipContactName", valueOrDash(organization != null ? organization.getMembershipContactName() : null)));
		add(new Label("membershipContactEmail", valueOrDash(organization != null ? organization.getMembershipContactEmail() : null)));

		WebMarkupContainer restrictedCountriesLinkContainer = new WebMarkupContainer("restrictedCountriesLinkContainer");
		restrictedCountriesLinkContainer.setOutputMarkupPlaceholderTag(true);
		restrictedCountriesLinkContainer.setVisible(!restrictedCountries.isEmpty());
		add(restrictedCountriesLinkContainer);

		buildRestrictedCountriesModal();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (shouldAutoShowRestrictedCountriesModal()) {
			response.render(OnDomReadyHeaderItem.forScript(
					"var restrictedCountriesModalElement = document.getElementById('restricted-countries-modal');"
							+ "if (restrictedCountriesModalElement && window.bootstrap && bootstrap.Modal) {"
							+ "bootstrap.Modal.getOrCreateInstance(restrictedCountriesModalElement).show();"
							+ "}"));
			markRestrictedCountriesModalAsAcknowledged();
		}
	}

	private void buildRestrictedCountriesModal() {
		WebMarkupContainer modalContainer = new WebMarkupContainer("restrictedCountriesModalContainer");
		modalContainer.setOutputMarkupPlaceholderTag(true);
		modalContainer.setVisible(!restrictedCountries.isEmpty());
		add(modalContainer);

		modalContainer.add(new Label("restrictedCountriesSummary", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return RestrictedCountriesMessageResolver.resolve(NominationOrganizationSummaryPanel.this, electionType, restrictedCountryCodes);
			}
		}));

		modalContainer.add(new ListView<RestrictedCountryItem>("restrictedCountries", restrictedCountries) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<RestrictedCountryItem> item) {
				RestrictedCountryItem countryItem = item.getModelObject();
				ContextImage countryFlagImage = new ContextImage("countryFlag", countryItem.getFlagImagePath());
				countryFlagImage.add(AttributeModifier.replace("alt", countryItem.getCountryName()));
				countryFlagImage.add(AttributeModifier.replace("onerror",
						"this.classList.add('d-none');"
								+ "var fallback=this.nextElementSibling;"
								+ "if(fallback){fallback.classList.remove('d-none');}"
								+ "var label=this.closest('.text-center').querySelector('[data-country-label]');"
								+ "if(label){label.classList.add('d-none');}"));
				item.add(countryFlagImage);
				item.add(new Label("countryFallbackName", countryItem.getCountryName()));
				item.add(new Label("countryName", countryItem.getCountryName()));
			}
		});
	}

	private void loadRestrictedCountries(List<String> restrictedCountryCodes) {
		this.restrictedCountryCodes.clear();
		restrictedCountries.clear();

		if (restrictedCountryCodes == null || restrictedCountryCodes.isEmpty()) {
			return;
		}

		Set<String> addedCountryCodes = new LinkedHashSet<>();
		for (String countryCode : restrictedCountryCodes) {
			String normalizedCountryCode = normalizeCountryCode(countryCode);
			if (normalizedCountryCode == null || !addedCountryCodes.add(normalizedCountryCode)) {
				continue;
			}

			this.restrictedCountryCodes.add(normalizedCountryCode);
			restrictedCountries.add(new RestrictedCountryItem(
					resolveCountryName(normalizedCountryCode),
					"v2/images/flags/" + normalizedCountryCode.toLowerCase(Locale.ROOT) + ".svg"));
		}
	}

	private String resolveCountryName(String countryCode) {
		String displayLabel = COUNTRY_UTILS.getDisplayLabel(countryCode, getLocale(), true);
		if (StringUtils.isNotBlank(displayLabel)) {
			return displayLabel.trim();
		}
		return countryCode;
	}

	private String normalizeCountryCode(String countryCode) {
		String normalizedCountryCode = COUNTRY_UTILS.normalizeCountryCode(countryCode);
		if (StringUtils.isBlank(normalizedCountryCode) || normalizedCountryCode.length() != 2) {
			return null;
		}
		return normalizedCountryCode;
	}

	private boolean shouldAutoShowRestrictedCountriesModal() {
		return shouldAutoShowRestrictedCountriesModal(!restrictedCountries.isEmpty(), hasRestrictedCountriesModalBeenShown());
	}

	private void markRestrictedCountriesModalAsAcknowledged() {
		ElectionsWebAdminSession session = getElectionsWebAdminSession();
		if (session != null) {
			session.markDoNominationRestrictedCountriesModalAsShown(token);
		}
	}

	private boolean hasRestrictedCountriesModalBeenShown() {
		ElectionsWebAdminSession session = getElectionsWebAdminSession();
		return session != null && session.hasDoNominationRestrictedCountriesModalBeenShown(token);
	}

	private ElectionsWebAdminSession getElectionsWebAdminSession() {
		return getSession() instanceof ElectionsWebAdminSession ? (ElectionsWebAdminSession) getSession() : null;
	}

	static boolean shouldAutoShowRestrictedCountriesModal(boolean hasRestrictedCountries, boolean modalAlreadyShown) {
		return hasRestrictedCountries && !modalAlreadyShown;
	}

	private String valueOrDash(String value) {
		if (StringUtils.isBlank(value)) {
			return "-";
		}
		return value;
	}

	private static final class RestrictedCountryItem implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String countryName;
		private final String flagImagePath;

		private RestrictedCountryItem(String countryName, String flagImagePath) {
			this.countryName = countryName;
			this.flagImagePath = flagImagePath;
		}

		public String getCountryName() {
			return countryName;
		}

		public String getFlagImagePath() {
			return flagImagePath;
		}
	}
}
