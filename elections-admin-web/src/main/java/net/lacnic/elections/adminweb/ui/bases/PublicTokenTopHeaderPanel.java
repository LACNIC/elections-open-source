package net.lacnic.elections.adminweb.ui.bases;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;
import net.lacnic.elections.adminweb.ui.token.page.PublicElectionPage;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.ReminderFrequency;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicTokenTopHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public enum NotificationMode {
		NONE,
		AUDIT,
		NOMINATION
	}

	private final PublicTokenBasePage page;
	private final String token;
	private final NotificationMode notificationMode;
	private final ReminderFrequency currentReminderFrequency;

	private Link<Void> spanishLink;
	private Link<Void> englishLink;
	private Link<Void> portugueseLink;

	public PublicTokenTopHeaderPanel(
			String id,
			PublicTokenBasePage page,
			String sectionLabel,
			boolean showLanguageSelector,
			NotificationMode notificationMode,
			ReminderFrequency currentReminderFrequency) {
		super(id);
		this.page = page;
		this.token = page != null ? page.getToken() : null;
		this.notificationMode = notificationMode != null ? notificationMode : NotificationMode.NONE;
		this.currentReminderFrequency = currentReminderFrequency != null ? currentReminderFrequency : ReminderFrequency.defaultValue();

		add(new BookmarkablePageLink<Void>("homeLink", PublicHomeDashboard.class));
		PageParameters electionPageParams = page != null ? page.buildPublicElectionPageParameters() : new PageParameters();
		BookmarkablePageLink<Void> electionLink = new BookmarkablePageLink<Void>("electionLink", PublicElectionPage.class, electionPageParams);
		boolean electionLinkEnabled = page != null && page.hasPublicElectionPageToken();
		electionLink.setEnabled(electionLinkEnabled);
		if (!electionLinkEnabled) {
			electionLink.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, " disabled"));
			electionLink.add(AttributeModifier.replace("aria-disabled", "true"));
		}
		electionLink.add(new Label("headerElectionTitle", valueOrDash(page != null ? page.getHeaderElectionTitle() : null)));
		add(electionLink);
		Link<Void> headerSectionLink = new Link<Void>("headerSectionLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				reloadCurrentPage();
			}
		};
		headerSectionLink.add(new Label("headerSectionLabel", valueOrDash(sectionLabel)));
		add(headerSectionLink);

		spanishLink = new Link<Void>("es") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.SP);
			}
		};

		englishLink = new Link<Void>("en") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.EN);
			}
		};

		portugueseLink = new Link<Void>("pt") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.PT);
			}
		};

		WebMarkupContainer languageContainer = new WebMarkupContainer("languageContainer");
		languageContainer.setVisible(showLanguageSelector);
		languageContainer.add(spanishLink);
		languageContainer.add(englishLink);
		languageContainer.add(portugueseLink);
		add(languageContainer);
		if (showLanguageSelector) {
			initLocaleVisibilityLinks(SecurityUtils.getLanguageCode());
		}

		add(new Label("headerUserDisplay", valueOrDash(page.getHeaderUserDisplay())));

		WebMarkupContainer notificationContainer = new WebMarkupContainer("notificationContainer");
		boolean showNotification = this.notificationMode != NotificationMode.NONE;
		notificationContainer.setVisible(showNotification);
		WebMarkupContainer notificationBellIcon = new WebMarkupContainer("notificationBellIcon");
		notificationBellIcon.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveNotificationBellIconClass()));
		notificationContainer.add(notificationBellIcon);
		notificationContainer.add(new Label("headerNotificationStatus", valueOrDash(page.getHeaderNotificationStatus())));
		notificationContainer.add(new ListView<ReminderFrequency>("reminderFrequencyOptions", getOrderedReminderFrequencies()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ReminderFrequency> item) {
				final ReminderFrequency reminderFrequency = item.getModelObject();
				boolean isDisabledFrequency = reminderFrequency == ReminderFrequency.DISABLED;
				boolean isRecommendedFrequency = reminderFrequency == ReminderFrequency.EVERY_DAY;

				WebMarkupContainer disabledDivider = new WebMarkupContainer("disabledDivider");
				disabledDivider.setVisible(isDisabledFrequency);
				item.add(disabledDivider);

				Link<Void> reminderFrequencyLink = new Link<Void>("reminderFrequencyLink") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						updateReminderFrequency(reminderFrequency);
					}
				};
				reminderFrequencyLink.add(new Label("reminderFrequencyLabel", getString("reminderFrequency." + reminderFrequency.name())));
				WebMarkupContainer reminderFrequencyIcon = new WebMarkupContainer("reminderFrequencyIcon");
				reminderFrequencyIcon.add(AttributeModifier.replace("data-lucide", isDisabledFrequency ? "bell-off" : "bell-ring"));
				if (isDisabledFrequency) {
					reminderFrequencyIcon.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, " text-danger"));
					reminderFrequencyLink.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, " text-danger-emphasis"));
				}
				reminderFrequencyLink.add(reminderFrequencyIcon);
				WebMarkupContainer recommendedBadge = new WebMarkupContainer("recommendedBadge");
				recommendedBadge.setVisible(isRecommendedFrequency);
				reminderFrequencyLink.add(recommendedBadge);
				if (reminderFrequency == PublicTokenTopHeaderPanel.this.currentReminderFrequency) {
					reminderFrequencyLink.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS, " active fw-semibold"));
				}
				item.add(reminderFrequencyLink);
			}
		});
		add(notificationContainer);
	}

	private void reloadCurrentPage() {
		if (page != null) {
			page.reloadCurrentPageWithCurrentParameters();
		}
	}

	private void markLocaleSelectionByUser() {
		if (page != null) {
			page.markTopbarLocaleSelectionAsManual();
		}
	}

	private void changeLanguage(LanguageCode language) {
		markLocaleSelectionByUser();
		SecurityUtils.setLocale(language);
		if (page != null) {
			page.reloadCurrentPageWithLocale(language);
		}
	}

	private String valueOrDash(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value;
	}

	private List<ReminderFrequency> getOrderedReminderFrequencies() {
		return Arrays.asList(
				ReminderFrequency.EVERY_DAY,
				ReminderFrequency.MONDAY_TO_FRIDAY,
				ReminderFrequency.MONDAY_WEDNESDAY_FRIDAY,
				ReminderFrequency.TUESDAY_THURSDAY,
				ReminderFrequency.DISABLED);
	}

	private void updateReminderFrequency(ReminderFrequency reminderFrequency) {
		ReminderFrequency resolvedFrequency = reminderFrequency != null ? reminderFrequency : ReminderFrequency.defaultValue();
		boolean updated = false;
		switch (notificationMode) {
		case AUDIT:
			updated = AppContext.getInstance().getVoterBeanRemote()
					.updateAuditorReminderFrequency(token, resolvedFrequency, SecurityUtils.getClientIp());
			break;
		case NOMINATION:
			updated = AppContext.getInstance().getPreNominationBeanRemote()
					.updateCandidateReminderFrequency(token, resolvedFrequency, SecurityUtils.getClientIp());
			break;
		case NONE:
		default:
			return;
		}

		String frequencyLabel = getString("reminderFrequency." + resolvedFrequency.name(), null, resolvedFrequency.name());
		if (updated) {
			if (resolvedFrequency == ReminderFrequency.DISABLED) {
				getSession().info(getString("publicTokenNotificationDisabledSuccess", null, "Notificaciones desactivadas."));
			} else {
				String updateTemplate = getString("publicTokenNotificationUpdatedSuccess", null, "Configuración de notificaciones actualizada: {0}.");
				getSession().info(MessageFormat.format(updateTemplate, frequencyLabel));
			}
		} else {
			getSession().error(getString("publicTokenNotificationUpdateError", null, "No se pudo actualizar la configuración de notificaciones."));
		}
		reloadCurrentPage();
	}

	private String resolveNotificationBellIconClass() {
		if (currentReminderFrequency == ReminderFrequency.DISABLED) {
			return "fs-xxl text-danger animate__animated animate__tada animate__faster animate__repeat-3";
		}
		return "fs-xxl text-success animate__animated animate__tada animate__faster animate__repeat-3";
	}

	private void initLocaleVisibilityLinks(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		portugueseLink.setVisible(true);
		spanishLink.setVisible(true);
		englishLink.setVisible(true);

		switch (resolvedLanguage) {
		case PT:
			portugueseLink.setVisible(false);
			break;
		case EN:
			englishLink.setVisible(false);
			break;
		case SP:
		default:
			spanishLink.setVisible(false);
			break;
		}
	}
}
