package net.lacnic.elections.adminweb.ui.admin.election.results;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.commons.ElectionResultLetterSupport;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.LanguageCode;

public class ElectionResultsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String RESULTS_FORM_ID = "resultsForm";

	private Election election;
	private ElectionAuditorResult electionAuditorResult;
	private Boolean removeResultLetterSpanish = Boolean.FALSE;
	private Boolean removeResultLetterEnglish = Boolean.FALSE;
	private Boolean removeResultLetterPortuguese = Boolean.FALSE;

	public ElectionResultsDashboard(PageParameters params) {
		super(params);

		long electionId = UtilsParameters.getIdAsLong(params);
		election = reloadAndEnforceElectionAccess(electionId);
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		ElectionAuditorResult loadedResult = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorResult(electionId);
		electionAuditorResult = loadedResult != null ? loadedResult : createEmptyElectionAuditorResult(electionId);

		add(new FeedbackPanel("feedback"));
		add(new Label("electionTitle", election.getTitle(getLanguage())));

		Form<Void> resultsForm = new Form<>(RESULTS_FORM_ID);
		resultsForm.setMultiPart(true);
		resultsForm.setMaxSize(Bytes.megabytes(10));
		resultsForm.setOutputMarkupId(true);
		resultsForm.setMarkupId(RESULTS_FORM_ID);
		add(resultsForm);

		resultsForm.add(buildRichTextArea("resultSpanish", "resultSpanishEditor"));
		resultsForm.add(buildRichTextArea("resultEnglish", "resultEnglishEditor"));
		resultsForm.add(buildRichTextArea("resultPortuguese", "resultPortugueseEditor"));

		FileUploadField resultLetterSpanishField = new FileUploadField("resultLetterSpanish");
		FileUploadField resultLetterEnglishField = new FileUploadField("resultLetterEnglish");
		FileUploadField resultLetterPortugueseField = new FileUploadField("resultLetterPortuguese");
		resultsForm.add(resultLetterSpanishField);
		resultsForm.add(resultLetterEnglishField);
		resultsForm.add(resultLetterPortugueseField);

		resultsForm.add(buildCurrentLetterDownload("resultLetterSpanishDownload", LanguageCode.SP, electionAuditorResult.getResultLetterSpanish()));
		resultsForm.add(buildUnavailableLabel("resultLetterSpanishUnavailable", electionAuditorResult.getResultLetterSpanish()));
		resultsForm.add(new CheckBox("removeResultLetterSpanish", new PropertyModel<>(this, "removeResultLetterSpanish")));

		resultsForm.add(buildCurrentLetterDownload("resultLetterEnglishDownload", LanguageCode.EN, electionAuditorResult.getResultLetterEnglish()));
		resultsForm.add(buildUnavailableLabel("resultLetterEnglishUnavailable", electionAuditorResult.getResultLetterEnglish()));
		resultsForm.add(new CheckBox("removeResultLetterEnglish", new PropertyModel<>(this, "removeResultLetterEnglish")));

		resultsForm.add(buildCurrentLetterDownload("resultLetterPortugueseDownload", LanguageCode.PT, electionAuditorResult.getResultLetterPortuguese()));
		resultsForm.add(buildUnavailableLabel("resultLetterPortugueseUnavailable", electionAuditorResult.getResultLetterPortuguese()));
		resultsForm.add(new CheckBox("removeResultLetterPortuguese", new PropertyModel<>(this, "removeResultLetterPortuguese")));

		resultsForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistResultsChanges(resultLetterSpanishField, resultLetterEnglishField, resultLetterPortugueseField);
			}
		});
	}

	private ElectionAuditorResult createEmptyElectionAuditorResult(long electionId) {
		ElectionAuditorResult result = new ElectionAuditorResult();
		result.setElectionId(electionId);
		return result;
	}

	private TextArea<String> buildRichTextArea(String propertyName, String markupId) {
		TextArea<String> textArea = new TextArea<>(propertyName, new PropertyModel<>(electionAuditorResult, propertyName));
		textArea.setOutputMarkupId(true);
		textArea.setMarkupId(markupId);
		return textArea;
	}

	private Component buildCurrentLetterDownload(String componentId, LanguageCode languageCode, byte[] content) {
		boolean hasFile = hasBinary(content);
		String contentType = ElectionResultLetterSupport.resolveContentType(content);
		String fileName = ElectionResultLetterSupport.resolveFileName(election != null ? election.getElectionId() : 0L, languageCode, content);
		ResourceLink<Void> download = new ResourceLink<>(
				componentId,
				new ByteArrayResource(contentType, hasFile ? content : new byte[0], fileName));
		download.setVisible(hasFile);
		return download;
	}

	private Label buildUnavailableLabel(String componentId, byte[] content) {
		Label unavailable = new Label(componentId, new ResourceModel("electionResultsNoLetter"));
		unavailable.setVisible(!hasBinary(content));
		return unavailable;
	}

	private void persistResultsChanges(FileUploadField resultLetterSpanishField, FileUploadField resultLetterEnglishField, FileUploadField resultLetterPortugueseField) {
		try {
			Election currentElection = reloadAndEnforceElectionAccess(election.getElectionId());
			if (currentElection.isClosed()) {
				setResponsePage(ErrorElectionClosed.class);
				return;
			}

			FileUpload resultLetterSpanishUpload = resultLetterSpanishField.getFileUpload();
			FileUpload resultLetterEnglishUpload = resultLetterEnglishField.getFileUpload();
			FileUpload resultLetterPortugueseUpload = resultLetterPortugueseField.getFileUpload();

			if (!validatePdfUpload(resultLetterSpanishUpload, LanguageCode.SP)
					|| !validatePdfUpload(resultLetterEnglishUpload, LanguageCode.EN)
					|| !validatePdfUpload(resultLetterPortugueseUpload, LanguageCode.PT)) {
				return;
			}

			applyUploadedLetter(resultLetterSpanishUpload, Boolean.TRUE.equals(removeResultLetterSpanish), LanguageCode.SP);
			applyUploadedLetter(resultLetterEnglishUpload, Boolean.TRUE.equals(removeResultLetterEnglish), LanguageCode.EN);
			applyUploadedLetter(resultLetterPortugueseUpload, Boolean.TRUE.equals(removeResultLetterPortuguese), LanguageCode.PT);

			AppContext.getInstance().getManagerBeanRemote().saveElectionAuditorResult(
					electionAuditorResult,
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());

			getSession().info(getString("electionResultsSaveSuccess"));
			setResponsePage(ElectionResultsDashboard.class, UtilsParameters.getId(election.getElectionId()));
		} catch (Exception e) {
			appLogger.error("Error updating election auditor results for electionId={}", election != null ? election.getElectionId() : null, e);
			getSession().error(getString("electionResultsSaveError"));
		}
	}

	private boolean validatePdfUpload(FileUpload fileUpload, LanguageCode languageCode) {
		if (fileUpload == null || fileUpload.getSize() <= 0) {
			return true;
		}
		if (ElectionResultLetterSupport.isPdf(fileUpload.getBytes())) {
			return true;
		}
		getSession().error(MessageFormat.format(
				getString("electionResultsInvalidPdf"),
				resolveLanguageLabel(languageCode)));
		return false;
	}

	private String resolveLanguageLabel(LanguageCode languageCode) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			return getString("candidateProfileLanguageEnglish");
		case PT:
			return getString("candidateProfileLanguagePortuguese");
		case SP:
		default:
			return getString("candidateProfileLanguageSpanish");
		}
	}

	private void applyUploadedLetter(FileUpload fileUpload, boolean removeCurrentFile, LanguageCode languageCode) throws Exception {
		if (removeCurrentFile) {
			setResultLetter(languageCode, null);
		}
		if (fileUpload != null && fileUpload.getSize() > 0) {
			setResultLetter(languageCode, fileUpload.getBytes());
		}
	}

	private void setResultLetter(LanguageCode languageCode, byte[] content) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			electionAuditorResult.setResultLetterEnglish(content);
			break;
		case PT:
			electionAuditorResult.setResultLetterPortuguese(content);
			break;
		case SP:
		default:
			electionAuditorResult.setResultLetterSpanish(content);
			break;
		}
	}

	private boolean hasBinary(byte[] content) {
		return content != null && content.length > 0;
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
			    selector: '#resultSpanishEditor,#resultEnglishEditor,#resultPortugueseEditor',
			    menubar: false,
			    branding: false,
			    statusbar: true,
			    height: 260,
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
			""".formatted(get(RESULTS_FORM_ID).getMarkupId());
	}

	public Boolean getRemoveResultLetterSpanish() {
		return removeResultLetterSpanish;
	}

	public void setRemoveResultLetterSpanish(Boolean removeResultLetterSpanish) {
		this.removeResultLetterSpanish = removeResultLetterSpanish;
	}

	public Boolean getRemoveResultLetterEnglish() {
		return removeResultLetterEnglish;
	}

	public void setRemoveResultLetterEnglish(Boolean removeResultLetterEnglish) {
		this.removeResultLetterEnglish = removeResultLetterEnglish;
	}

	public Boolean getRemoveResultLetterPortuguese() {
		return removeResultLetterPortuguese;
	}

	public void setRemoveResultLetterPortuguese(Boolean removeResultLetterPortuguese) {
		this.removeResultLetterPortuguese = removeResultLetterPortuguese;
	}
}
