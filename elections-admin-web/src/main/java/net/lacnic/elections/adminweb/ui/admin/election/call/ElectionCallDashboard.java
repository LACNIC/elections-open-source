package net.lacnic.elections.adminweb.ui.admin.election.call;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class ElectionCallDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String CALL_FORM_ID = "callForm";

	private Election election;

	public ElectionCallDashboard(PageParameters params) {
		super(params);

		long electionId = UtilsParameters.getIdAsLong(params);
		election = reloadAndEnforceElectionAccess(electionId);
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabCall"));

		Form<Void> callForm = new Form<>(CALL_FORM_ID);
		callForm.setOutputMarkupId(true);
		callForm.setMarkupId(CALL_FORM_ID);
		add(callForm);

		TextArea<String> callSpanishField = new TextArea<>("callSpanish", new PropertyModel<>(election, "callSpanish"));
		callSpanishField.setOutputMarkupId(true);
		callSpanishField.setMarkupId("callSpanishEditor");
		callForm.add(callSpanishField);

		TextArea<String> callEnglishField = new TextArea<>("callEnglish", new PropertyModel<>(election, "callEnglish"));
		callEnglishField.setOutputMarkupId(true);
		callEnglishField.setMarkupId("callEnglishEditor");
		callForm.add(callEnglishField);

		TextArea<String> callPortugueseField = new TextArea<>("callPortuguese", new PropertyModel<>(election, "callPortuguese"));
		callPortugueseField.setOutputMarkupId(true);
		callPortugueseField.setMarkupId("callPortugueseEditor");
		callForm.add(callPortugueseField);

		callForm.add(new Link<Void>("back") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		callForm.add(new Link<Void>("skip") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		callForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistCallChanges(false);
			}
		});

		callForm.add(new Button("markDoneNext") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistCallChanges(true);
			}
		});
	}

	private void persistCallChanges(boolean markComplete) {
		try {
			Election currentElection = reloadAndEnforceElectionAccess(election.getElectionId());
			if (currentElection.isClosed()) {
				setResponsePage(ErrorElectionClosed.class);
				return;
			}
			Election updatedElection = AppContext.getInstance().getManagerBeanRemote().updateElection(
					election,
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());
			if (markComplete) {
				AppContext.getInstance().getManagerBeanRemote().persistElectionCallSet(
						updatedElection.getElectionId(),
						updatedElection.getTitleSpanish(),
						SecurityUtils.getUserAdminId(),
						SecurityUtils.getClientIp());
				setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(updatedElection.getElectionId()));
				return;
			}
			getSession().info(getString("electionCallSaveSuccess"));
			setResponsePage(ElectionCallDashboard.class, UtilsParameters.getId(updatedElection.getElectionId()));
		} catch (Exception e) {
			appLogger.error("Error updating election call for electionId={}", election != null ? election.getElectionId() : null, e);
			getSession().error(getString("electionCallSaveError"));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forUrl("js/plugins/tinymce/tinymce.min.js"));
		response.render(OnDomReadyHeaderItem.forScript(buildTinyMceScript()));
	}

	private String buildTinyMceScript() {
		return """
			(function () {
			  if (typeof tinymce === 'undefined') {
			    return;
			  }
			  tinymce.remove();
			  tinymce.init({
			    selector: '#callSpanishEditor,#callEnglishEditor,#callPortugueseEditor',
			    menubar: false,
			    branding: false,
			    statusbar: true,
			    height: 280,
			    plugins: [
			      'advlist autolink lists link charmap preview anchor',
			      'searchreplace visualblocks code fullscreen',
			      'insertdatetime table contextmenu paste code'
			    ],
			    toolbar: 'undo redo | code | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link table',
			    setup: function (editor) {
			      function syncEditor() {
			        editor.save();
			      }
			      editor.on('init', syncEditor);
			      editor.on('change keyup undo redo SetContent', syncEditor);
			    }
			  });
			  var form = document.getElementById('%s');
			  if (!form) {
			    return;
			  }
			  form.addEventListener('submit', function () {
			    tinymce.triggerSave();
			  });
			})();
			""".formatted(get(CALL_FORM_ID).getMarkupId());
	}
}
