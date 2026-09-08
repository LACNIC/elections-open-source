package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.validators.CandidateProfileLinkedinUrlValidator;
import net.lacnic.elections.adminweb.validators.CandidateProfileNameValidator;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.domain.pre.CandidateTextImprovementResponse;

public class ManageCandidateBiographyDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;
	private static final int BIOGRAPHY_MAX_LENGTH = 3000;
	private static final int CANDIDATE_NAME_MIN_LENGTH = 5;
	private static final int CANDIDATE_NAME_MAX_LENGTH = 255;
	private static final int CANDIDATE_MAIL_MAX_LENGTH = 255;
	private static final int CANDIDATE_LINKEDIN_MAX_LENGTH = 1000;
	private static final String BIOGRAPHY_FORM_ID = "biographyForm";
	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
	private static final Pattern HTML_SCRIPT_STYLE_PATTERN = Pattern.compile("(?is)<(script|style)\\b[^>]*>.*?</\\1>");
	private static final Pattern HTML_BREAK_PATTERN = Pattern.compile("(?is)<br\\s*/?>");
	private static final Pattern HTML_LIST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*>");
	private static final Pattern HTML_CLOSING_LI_PATTERN = Pattern.compile("(?is)</li\\s*>");
	private static final Pattern HTML_BLOCK_PATTERN = Pattern.compile("(?is)</?(p|div|section|article|header|footer|aside|blockquote|pre|ul|ol|table|tr|td|th|h[1-6])\\b[^>]*>");
	private static final Pattern HTML_NUMERIC_ENTITY_PATTERN = Pattern.compile("&#(x?[0-9A-Fa-f]+);");

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Candidate candidate;
	private String candidateName;
	private String candidateMail;
	private String candidateLinkedinUrl;
	private String bioSpanish;
	private String bioEnglish;
	private String bioPortuguese;

	private enum BiographyImprovementStatus {
		APPLIED,
		NO_CHANGES,
		EMPTY,
		DAILY_LIMIT_REACHED,
		PROCESSING_ERROR
	}

	private static final class BiographyImprovementResult {
		private final BiographyImprovementStatus status;
		private final String text;

		private BiographyImprovementResult(BiographyImprovementStatus status, String text) {
			this.status = status;
			this.text = text;
		}

		private static BiographyImprovementResult applied(String text) {
			return new BiographyImprovementResult(BiographyImprovementStatus.APPLIED, text);
		}

		private static BiographyImprovementResult noChanges() {
			return new BiographyImprovementResult(BiographyImprovementStatus.NO_CHANGES, null);
		}

		private static BiographyImprovementResult empty() {
			return new BiographyImprovementResult(BiographyImprovementStatus.EMPTY, null);
		}

		private static BiographyImprovementResult dailyLimitReached() {
			return new BiographyImprovementResult(BiographyImprovementStatus.DAILY_LIMIT_REACHED, null);
		}

		private static BiographyImprovementResult processingError() {
			return new BiographyImprovementResult(BiographyImprovementStatus.PROCESSING_ERROR, null);
		}
	}

	public ManageCandidateBiographyDashboard(PageParameters params) {
		super(params);

		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		long electionId = UtilsParameters.getIdAsLong(params);
		long candidateId = UtilsParameters.getCandidateAsLong(params);
		candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);

		if (!isValidCandidate(candidate, electionId)) {
			getSession().error(getString("candidateAnswersCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			return;
		}

		candidateName = candidate.getName();
		candidateMail = candidate.getMail();
		candidateLinkedinUrl = candidate.getLinkedinUrl();
		bioSpanish = candidate.getBioSpanish();
		bioEnglish = candidate.getBioEnglish();
		bioPortuguese = candidate.getBioPortuguese();

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));

		Form<Void> biographyForm = new Form<>(BIOGRAPHY_FORM_ID);
		biographyForm.setOutputMarkupId(true);
		biographyForm.setMarkupId(BIOGRAPHY_FORM_ID);
		add(biographyForm);

		TextField<String> candidateNameField = new TextField<>("editableCandidateName", new PropertyModel<>(this, "candidateName"));
		candidateNameField.setRequired(true);
		candidateNameField.setConvertEmptyInputStringToNull(true);
		candidateNameField.add(new NonBlankStringValidator("candidateProfileNameRequired"));
		candidateNameField.add(new CandidateProfileNameValidator(CANDIDATE_NAME_MIN_LENGTH, CANDIDATE_NAME_MAX_LENGTH));
		biographyForm.add(candidateNameField);

		EmailTextField candidateMailField = new EmailTextField("editableCandidateMail", new PropertyModel<>(this, "candidateMail"));
		candidateMailField.setRequired(true);
		candidateMailField.setConvertEmptyInputStringToNull(true);
		candidateMailField.add(StringValidator.maximumLength(CANDIDATE_MAIL_MAX_LENGTH));
		biographyForm.add(candidateMailField);

		TextField<String> candidateLinkedinUrlField = new TextField<>("editableCandidateLinkedinUrl", new PropertyModel<>(this, "candidateLinkedinUrl"));
		candidateLinkedinUrlField.setConvertEmptyInputStringToNull(true);
		candidateLinkedinUrlField.add(new CandidateProfileLinkedinUrlValidator(CANDIDATE_LINKEDIN_MAX_LENGTH));
		biographyForm.add(candidateLinkedinUrlField);

		TextArea<String> bioSpanishField = new TextArea<>("bioSpanish", new PropertyModel<>(this, "bioSpanish"));
		bioSpanishField.setRequired(true);
		bioSpanishField.setOutputMarkupId(true);
		bioSpanishField.setMarkupId("bioSpanishEditor");
		bioSpanishField.add(StringValidator.maximumLength(BIOGRAPHY_MAX_LENGTH));
		biographyForm.add(bioSpanishField);

		TextArea<String> bioEnglishField = new TextArea<>("bioEnglish", new PropertyModel<>(this, "bioEnglish"));
		bioEnglishField.setRequired(false);
		bioEnglishField.setOutputMarkupId(true);
		bioEnglishField.setMarkupId("bioEnglishEditor");
		bioEnglishField.add(StringValidator.maximumLength(BIOGRAPHY_MAX_LENGTH));
		biographyForm.add(bioEnglishField);

		TextArea<String> bioPortugueseField = new TextArea<>("bioPortuguese", new PropertyModel<>(this, "bioPortuguese"));
		bioPortugueseField.setRequired(false);
		bioPortugueseField.setOutputMarkupId(true);
		bioPortugueseField.setMarkupId("bioPortugueseEditor");
		bioPortugueseField.add(StringValidator.maximumLength(BIOGRAPHY_MAX_LENGTH));
		biographyForm.add(bioPortugueseField);

		WebMarkupContainer aiActionsContainer = new WebMarkupContainer("aiActionsContainer");
		aiActionsContainer.setVisible(AiAssistTextAreaPanel.isAiTextImprovementAvailable());
		biographyForm.add(aiActionsContainer);
		aiActionsContainer.add(new Button("spellcheckBiographies") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				applySpellcheckToAllBiographies();
			}
		});
		aiActionsContainer.add(new Button("translateToPortugueseFromSpanish") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				applySpanishBiographyTranslation(CandidateTextImprovementInstruction.TRANSLATE_TO_PORTUGUESE);
			}
		});
		aiActionsContainer.add(new Button("translateToEnglishFromSpanish") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				applySpanishBiographyTranslation(CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH);
			}
		});

		biographyForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistBiographyChanges(electionId);
			}
		});

		biographyForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private boolean isValidCandidate(Candidate loadedCandidate, long electionId) {
		return loadedCandidate != null
				&& loadedCandidate.getElection() != null
				&& loadedCandidate.getElection().getElectionId() == electionId;
	}

	private void applySpellcheckToAllBiographies() {
		BiographyImprovementResult spanishResult = improveBiographyText(bioSpanish, CandidateTextImprovementInstruction.SPELLING_REVIEW);
		BiographyImprovementResult englishResult = improveBiographyText(bioEnglish, CandidateTextImprovementInstruction.SPELLING_REVIEW);
		BiographyImprovementResult portugueseResult = improveBiographyText(bioPortuguese, CandidateTextImprovementInstruction.SPELLING_REVIEW);

		boolean anyProcessedText = spanishResult.status != BiographyImprovementStatus.EMPTY
				|| englishResult.status != BiographyImprovementStatus.EMPTY
				|| portugueseResult.status != BiographyImprovementStatus.EMPTY;
		boolean anyApplied = spanishResult.status == BiographyImprovementStatus.APPLIED
				|| englishResult.status == BiographyImprovementStatus.APPLIED
				|| portugueseResult.status == BiographyImprovementStatus.APPLIED;
		boolean anyProcessingError = spanishResult.status == BiographyImprovementStatus.PROCESSING_ERROR
				|| englishResult.status == BiographyImprovementStatus.PROCESSING_ERROR
				|| portugueseResult.status == BiographyImprovementStatus.PROCESSING_ERROR;
		boolean anyDailyLimitReached = spanishResult.status == BiographyImprovementStatus.DAILY_LIMIT_REACHED
				|| englishResult.status == BiographyImprovementStatus.DAILY_LIMIT_REACHED
				|| portugueseResult.status == BiographyImprovementStatus.DAILY_LIMIT_REACHED;

		if (!anyProcessedText) {
			getSession().error(getString("textAssistanceEmptyTextError"));
			return;
		}

		if (spanishResult.status == BiographyImprovementStatus.APPLIED) {
			bioSpanish = spanishResult.text;
		}
		if (englishResult.status == BiographyImprovementStatus.APPLIED) {
			bioEnglish = englishResult.text;
		}
		if (portugueseResult.status == BiographyImprovementStatus.APPLIED) {
			bioPortuguese = portugueseResult.text;
		}

		if (anyDailyLimitReached) {
			getSession().error(getString("textAssistanceDailyLimitReached"));
			return;
		}

		if (anyProcessingError) {
			getSession().error(getString("textAssistanceProcessingError"));
			return;
		}

		if (anyApplied) {
			getSession().info(getString("textAssistanceSpellingApplied"));
			return;
		}

		getSession().info(getString("textAssistanceSpellingNoChanges"));
	}

	private void applySpanishBiographyTranslation(CandidateTextImprovementInstruction instruction) {
		BiographyImprovementResult translationResult = improveBiographyText(bioSpanish, instruction);
		if (translationResult.status == BiographyImprovementStatus.EMPTY) {
			getSession().error(getString("textAssistanceEmptyTextError"));
			return;
		}

		if (translationResult.status == BiographyImprovementStatus.DAILY_LIMIT_REACHED) {
			getSession().error(getString("textAssistanceDailyLimitReached"));
			return;
		}

		if (translationResult.status == BiographyImprovementStatus.PROCESSING_ERROR) {
			getSession().error(getString("textAssistanceProcessingError"));
			return;
		}

		if (translationResult.status == BiographyImprovementStatus.NO_CHANGES) {
			getSession().info(getString(resolveNoChangesMessageKey(instruction)));
			return;
		}

		if (instruction == CandidateTextImprovementInstruction.TRANSLATE_TO_PORTUGUESE) {
			bioPortuguese = translationResult.text;
		} else if (instruction == CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH) {
			bioEnglish = translationResult.text;
		}

		getSession().info(getString(resolveAppliedMessageKey(instruction)));
	}

	private BiographyImprovementResult improveBiographyText(String originalBiography, CandidateTextImprovementInstruction instruction) {
		String flattenedText = flattenHtmlToPlainText(originalBiography);
		if (!hasText(flattenedText)) {
			return BiographyImprovementResult.empty();
		}

		CandidateTextImprovementResponse improvementResponse;
		try {
			improvementResponse = AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
					null,
					flattenedText,
					instruction,
					null,
					SecurityUtils.getClientIp());
		} catch (Exception ex) {
			appLogger.error("Error improving candidate biography with IA. candidateId={}, instruction={}", candidate.getCandidateId(), instruction, ex);
			return BiographyImprovementResult.processingError();
		}

		if (improvementResponse == null || !improvementResponse.isProcessed()) {
			return improvementResponse != null && improvementResponse.isDailyLimitReached()
					? BiographyImprovementResult.dailyLimitReached()
					: BiographyImprovementResult.processingError();
		}

		String improvedText = trimToNull(improvementResponse.getImprovedText());
		if (improvedText == null) {
			return BiographyImprovementResult.processingError();
		}

		if (sameText(flattenedText, improvedText)) {
			return BiographyImprovementResult.noChanges();
		}

		return BiographyImprovementResult.applied(improvedText);
	}

	private void persistBiographyChanges(long electionId) {
		try {
			if (sameValue(candidate.getName(), candidateName)
					&& sameValue(candidate.getMail(), candidateMail)
					&& sameValue(candidate.getLinkedinUrl(), candidateLinkedinUrl)
					&& sameValue(candidate.getBioSpanish(), bioSpanish)
					&& sameValue(candidate.getBioEnglish(), bioEnglish)
					&& sameValue(candidate.getBioPortuguese(), bioPortuguese)) {
				getSession().info(getString("candidateBiographyNoChanges"));
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
				return;
			}

			candidate.setName(candidateName);
			candidate.setMail(candidateMail);
			candidate.setLinkedinUrl(candidateLinkedinUrl);
			candidate.setBioSpanish(bioSpanish);
			candidate.setBioEnglish(bioEnglish);
			candidate.setBioPortuguese(bioPortuguese);
			AppContext.getInstance().getManagerBeanRemote().editCandidate(candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			getSession().info(getString("candidateBiographySaveSuccess"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
		} catch (Exception e) {
			appLogger.error("Error updating candidate biographies for candidateId={}", candidate.getCandidateId(), e);
			getSession().error(getString("candidateBiographySaveError"));
		}
	}

	private boolean sameValue(String left, String right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.equals(right);
	}

	private String valueOrDash(String value) {
		return value != null && !value.trim().isEmpty() ? value : getString("candidateManagemenListNoLink");
	}

	private String resolveAppliedMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingApplied"
				: "textAssistanceTranslationApplied";
	}

	private String resolveNoChangesMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingNoChanges"
				: "textAssistanceTranslationNoChanges";
	}

	private boolean sameText(String left, String right) {
		return trimToEmpty(left).replace("\r\n", "\n").equals(trimToEmpty(right).replace("\r\n", "\n"));
	}

	private String trimToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	private String trimToNull(String value) {
		String trimmed = value == null ? null : value.trim();
		return trimmed == null || trimmed.isEmpty() ? null : trimmed;
	}

	private boolean hasText(String value) {
		return trimToNull(value) != null;
	}

	private String flattenHtmlToPlainText(String html) {
		if (!hasText(html)) {
			return null;
		}

		String normalized = html.replace("\r\n", "\n").replace('\r', '\n');
		normalized = HTML_SCRIPT_STYLE_PATTERN.matcher(normalized).replaceAll(" ");
		normalized = HTML_BREAK_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_LIST_ITEM_PATTERN.matcher(normalized).replaceAll("\n- ");
		normalized = HTML_CLOSING_LI_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_BLOCK_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_TAG_PATTERN.matcher(normalized).replaceAll(" ");
		normalized = decodeHtmlEntities(normalized);
		normalized = normalized.replace('\u00A0', ' ');
		normalized = normalized.replaceAll("[ \\t\\x0B\\f]+", " ");
		normalized = normalized.replaceAll(" *\\n *", "\n");
		normalized = normalized.replaceAll("\\n{3,}", "\n\n");
		return trimToNull(normalized);
	}

	private String decodeHtmlEntities(String value) {
		if (value == null || value.indexOf('&') < 0) {
			return value;
		}

		String normalized = value
				.replace("&nbsp;", " ")
				.replace("&#160;", " ")
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#34;", "\"")
				.replace("&#39;", "'")
				.replace("&apos;", "'")
				.replace("&ndash;", "-")
				.replace("&mdash;", "-")
				.replace("&bull;", "-")
				.replace("&middot;", "-")
				.replace("&hellip;", "...")
				.replace("&copy;", "(c)")
				.replace("&reg;", "(R)")
				.replace("&trade;", "TM");

		Matcher matcher = HTML_NUMERIC_ENTITY_PATTERN.matcher(normalized);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String rawCode = matcher.group(1);
			String replacement = matcher.group(0);
			try {
				int codePoint;
				if (rawCode != null && (rawCode.startsWith("x") || rawCode.startsWith("X"))) {
					codePoint = Integer.parseInt(rawCode.substring(1), 16);
				} else {
					codePoint = Integer.parseInt(rawCode, 10);
				}
				replacement = Character.isValidCodePoint(codePoint) ? new String(Character.toChars(codePoint)) : "";
			} catch (Exception e) {
				replacement = matcher.group(0);
			}
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
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
			  var maxLength = %d;
			  var allowedControlKeys = [
			    'Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown',
			    'Home', 'End', 'PageUp', 'PageDown', 'Tab', 'Escape'
			  ];
			  tinymce.remove();
			  tinymce.init({
			    selector: '#bioSpanishEditor,#bioEnglishEditor,#bioPortugueseEditor',
			    menubar: false,
			    branding: false,
			    statusbar: true,
			    height: 280,
			    plugins: [
			      'charmap preview',
			      'searchreplace visualblocks fullscreen',
			      'insertdatetime table contextmenu paste code'
			    ],
			    toolbar: 'undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify',
			    setup: function (editor) {
			      function syncEditor() {
			        editor.save();
			      }
			      function getCurrentLength() {
			        return editor.getContent({ format: 'html' }).length;
			      }
			      function canAcceptInput() {
			        return getCurrentLength() < maxLength;
			      }
			      function isShortcut(event) {
			        return event.ctrlKey || event.metaKey || event.altKey;
			      }
			      editor.on('init', function () {
			        syncEditor();
			      });
			      editor.on('change keyup undo redo SetContent', syncEditor);
			      editor.on('keydown', function (event) {
			        if (isShortcut(event) || allowedControlKeys.indexOf(event.key) !== -1) {
			          return;
			        }
			        if (!canAcceptInput()) {
			          event.preventDefault();
			          event.stopPropagation();
			        }
			      });
			      editor.on('paste', function (event) {
			        if (canAcceptInput()) {
			          return;
			        }
			        event.preventDefault();
			        event.stopPropagation();
			      });
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
			""".formatted(BIOGRAPHY_MAX_LENGTH, get(BIOGRAPHY_FORM_ID).getMarkupId());
	}
}
