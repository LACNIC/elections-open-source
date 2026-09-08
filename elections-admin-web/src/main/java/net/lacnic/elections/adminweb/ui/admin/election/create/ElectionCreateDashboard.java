package net.lacnic.elections.adminweb.ui.admin.election.create;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.LanguageCode;

public class ElectionCreateDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Election election;

	public ElectionCreateDashboard(PageParameters params) {
		super(params);
		election = new Election();
		election.setLinkSpanish(AppContext.getInstance().getManagerBeanRemote().getDefaultWebsite());
		election.setDefaultSender(AppContext.getInstance().getManagerBeanRemote().getDefaultSender());
		election.setDefaultRecipient(AppContext.getInstance().getManagerBeanRemote().getDefaultRecipient());

		add(new FeedbackPanel("feedback"));
		add(new CreateElectionForm("createElectionForm"));
	}

	private final class CreateElectionForm extends Form<Void> {
		private static final long serialVersionUID = 1L;

		private CreateElectionForm(String id) {
			super(id);
			add(new ElectionCreatePanel("fields", election));

			add(new Button("submit") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					try {
						election.applyRestrictedCountryCodes();
						copyTexts();
						Election newElection = AppContext.getInstance().getManagerBeanRemote().updateElection(election, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
						getSession().info(getString("electionManagementCreateSuccess"));
						addPresetConfigurationFeedback(newElection);
						setResponsePage(ElectionCallDashboard.class, UtilsParameters.getId(newElection.getElectionId()));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						error(e.getMessage());
					}
				}
			});

			add(new Link<Void>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					setResponsePage(ElectionsDashboard.class);
				}
			});
		}

		private void copyTexts() throws Exception {
			if (election.isOnlySp()) {
				election.copyLanguageDescriptions(LanguageCode.SP.getCode());
				election.copyLanguageTitles(LanguageCode.SP.getCode());
				election.copyLanguageURLs(LanguageCode.SP.getCode());
			}
		}

		private void addPresetConfigurationFeedback(Election persistedElection) {
			if (persistedElection == null) {
				return;
			}

			ElectionType scope = persistedElection.getEffectiveElectionType();
			String feedbackKey = resolvePresetConfigurationFeedbackKey(scope);
			if (scope == null || feedbackKey == null) {
				return;
			}

			String scopeLabel = getString("electionDeclarationsScope." + scope.name());
			String feedback = new StringResourceModel(feedbackKey, ElectionCreateDashboard.this).setParameters(scopeLabel).getString();
			getSession().info(feedback);
		}

		private String resolvePresetConfigurationFeedbackKey(ElectionType scope) {
			if (scope == null) {
				return null;
			}
			switch (scope) {
			case BOARD:
				return "electionManagementPresetAppliedInfo";
			case ELECTORAL_COMMISSION:
			case FISCAL_COMMISSION:
				return "electionManagementPresetAppliedCommissionInfo";
			case MODERATORS:
				return "electionManagementPresetAppliedModeratorsInfo";
			case IANA:
				return "electionManagementPresetAppliedIanaInfo";
			case ASO:
				return "electionManagementPresetAppliedAsoInfo";
			case OTHER:
				return "electionManagementPresetAppliedOtherInfo";
			default:
				return null;
			}
		}
	}
}
