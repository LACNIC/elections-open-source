package net.lacnic.elections.adminweb.ui.token;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.validators.NonBlankStringValidator;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.domain.pre.CandidateTextImprovementResponse;
import net.lacnic.elections.domain.pre.Nomination;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidateCommunityQuestionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ANSWER_LENGTH = 1000;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final String token;
	private final List<CommunityQuestionView> pendingQuestions;
	private final List<CommunityQuestionView> answeredQuestions;

	public CandidateCommunityQuestionsPanel(String id, String token, Nomination nomination) {
		super(id);
		this.token = token;

		List<CandidateQuestion> candidateQuestions = AppContext.getInstance().getPreNominationBeanRemote().getCandidateQuestionsForNominationTasks(token);
		pendingQuestions = new ArrayList<>();
		answeredQuestions = new ArrayList<>();
		for (CandidateQuestion question : candidateQuestions) {
			if (question == null || question.getStatus() == null) {
				continue;
			}
			CommunityQuestionView row = toView(question);
			if (question.getStatus() == CandidateQuestionStatus.QUESTION_READY_FOR_CANDIDATE) {
				pendingQuestions.add(row);
				continue;
			}
			if (question.getStatus() == CandidateQuestionStatus.ANSWER_SUBMITTED_BY_CANDIDATE || question.getStatus() == CandidateQuestionStatus.PUBLISHED) {
				answeredQuestions.add(row);
			}
		}

		pendingQuestions.sort(new Comparator<CommunityQuestionView>() {
			@Override
			public int compare(CommunityQuestionView a, CommunityQuestionView b) {
				int byDate = compareDatesDesc(a.getQuestionDate(), b.getQuestionDate());
				if (byDate != 0) {
					return byDate;
				}
				return Long.compare(b.getQuestionId(), a.getQuestionId());
			}
		});
		answeredQuestions.sort(new Comparator<CommunityQuestionView>() {
			@Override
			public int compare(CommunityQuestionView a, CommunityQuestionView b) {
				int byDate = compareDatesDesc(a.getAnswerDate(), b.getAnswerDate());
				if (byDate != 0) {
					return byDate;
				}
				return Long.compare(b.getQuestionId(), a.getQuestionId());
			}
		});

		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		WebMarkupContainer pendingSection = new WebMarkupContainer("pendingSection");
		pendingSection.setVisible(!pendingQuestions.isEmpty());
		add(pendingSection);
		pendingSection.add(new ListView<CommunityQuestionView>("pendingRows", pendingQuestions) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CommunityQuestionView> item) {
				CommunityQuestionView row = item.getModelObject();
				item.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsPendingRow", row.getQuestionId())));

				Label askedByInitials = new Label("askedByInitials", valueOrDash(row.getAskedByInitials()));
				askedByInitials.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsPendingAskedByInitials", row.getQuestionId())));
				item.add(askedByInitials);

				Label questionText = new Label("questionText", valueOrDash(row.getQuestionText()));
				questionText.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsPendingQuestionText", row.getQuestionId())));
				item.add(questionText);

				Label questionDate = new Label("questionDate", formatDateTime(row.getQuestionDate()));
				questionDate.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsPendingQuestionDate", row.getQuestionId())));
				item.add(questionDate);

				Label statusLabel = new Label("statusLabel", resolveStatusLabel(row.getStatus()));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge text-bg-dark"));
				statusLabel.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsPendingStatusLabel", row.getQuestionId())));
				item.add(statusLabel);

				Form<Void> answerForm = new Form<Void>("answerForm") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSubmit() {
						String normalizedAnswer = trimToNull(row.getAnswerDraft());
						boolean updated = AppContext.getInstance().getPreNominationBeanRemote().submitCandidateQuestionAnswer(
								token,
								row.getQuestionId(),
								normalizedAnswer,
								resolveActor(),
								SecurityUtils.getClientIp());
						if (!updated) {
							error(getString("candidateCommunityQuestionsAnswerSaveError"));
							return;
						}
						getSession().success(getString("candidateCommunityQuestionsAnswerSaved"));
						reloadCurrentPage();
					}
				};
				answerForm.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnswerForm", row.getQuestionId())));

					AiAssistTextAreaPanel answerEditor = new AiAssistTextAreaPanel(
							"answerEditor",
							new PropertyModel<>(row, "answerDraft"),
							null,
							target -> target.add(feedbackPanel),
							(originalText, instruction, styleContext) -> {
								long startMillis = System.currentTimeMillis();
								Long questionId = row.getQuestionId();
								appLogger.info("Community question spellcheck requested. questionId={}, answerLength={}",
										questionId,
										StringUtils.length(StringUtils.defaultString(originalText)));
								appLogger.info("Community question spellcheck started. questionId={}, originalLength={}",
										questionId,
										StringUtils.length(StringUtils.defaultString(originalText)));
								try {
									CandidateTextImprovementResponse improvementResponse = AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
											token,
											originalText,
											CandidateTextImprovementInstruction.SPELLING_REVIEW,
											null,
											SecurityUtils.getClientIp());
									long elapsedMs = System.currentTimeMillis() - startMillis;
									if (improvementResponse == null || !improvementResponse.isProcessed()) {
										if (improvementResponse != null && improvementResponse.isDailyLimitReached()) {
											appLogger.warn("Community question spellcheck daily limit reached. questionId={}, elapsedMs={}", questionId, elapsedMs);
										} else {
											appLogger.warn("Community question spellcheck not processed. questionId={}, responseNull={}, elapsedMs={}",
													questionId,
													improvementResponse == null,
													elapsedMs);
										}
										return improvementResponse;
									}
									String improvedText = improvementResponse.getImprovedText();
									if (StringUtils.isBlank(improvedText)) {
										appLogger.warn("Community question spellcheck returned blank improvedText. questionId={}, elapsedMs={}", questionId, elapsedMs);
										return improvementResponse;
									}
									if (sameText(originalText, improvedText)) {
										appLogger.info("Community question spellcheck completed with no changes. questionId={}, improvedLength={}, elapsedMs={}",
												questionId,
												StringUtils.length(improvedText),
												elapsedMs);
									} else {
										appLogger.info("Community question spellcheck applied. questionId={}, improvedLength={}, elapsedMs={}",
												questionId,
												StringUtils.length(improvedText),
												elapsedMs);
									}
									return improvementResponse;
								} catch (Exception ex) {
									appLogger.error("Community question spellcheck failed on backend call. questionId={}, elapsedMs={}",
											questionId,
											System.currentTimeMillis() - startMillis,
											ex);
									throw ex;
								}
							},
							false,
							true,
							true,
							false,
							false,
							MAX_ANSWER_LENGTH,
							4,
							textArea -> {
								textArea.setConvertEmptyInputStringToNull(true);
								textArea.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnswerTextArea", row.getQuestionId())));
								textArea.add(new NonBlankStringValidator("candidateCommunityQuestionsAnswerRequired"));
							});
				answerEditor.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnswerEditor", row.getQuestionId())));
					answerForm.add(answerEditor);
					Button submitAnswerButton = new Button("submitAnswerButton");
					submitAnswerButton.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsSubmitAnswerButton", row.getQuestionId())));
					answerForm.add(submitAnswerButton);
					item.add(answerForm);
				}
		});

		Label pendingEmpty = new Label("pendingEmpty", getString("candidateCommunityQuestionsPendingEmpty"));
		pendingEmpty.setVisible(pendingQuestions.isEmpty());
		add(pendingEmpty);

		WebMarkupContainer answeredSection = new WebMarkupContainer("answeredSection");
		answeredSection.setVisible(!answeredQuestions.isEmpty());
		add(answeredSection);
		answeredSection.add(new ListView<CommunityQuestionView>("answeredRows", answeredQuestions) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CommunityQuestionView> item) {
				CommunityQuestionView row = item.getModelObject();
				item.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredRow", row.getQuestionId())));

				Label askedByInitials = new Label("askedByInitials", valueOrDash(row.getAskedByInitials()));
				askedByInitials.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredAskedByInitials", row.getQuestionId())));
				item.add(askedByInitials);

				Label questionText = new Label("questionText", valueOrDash(row.getQuestionText()));
				questionText.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredQuestionText", row.getQuestionId())));
				item.add(questionText);

				Label questionDate = new Label("questionDate", formatDateTime(row.getQuestionDate()));
				questionDate.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredQuestionDate", row.getQuestionId())));
				item.add(questionDate);

				Label answerDate = new Label("answerDate", formatDateTime(row.getAnswerDate()));
				answerDate.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredAnswerDate", row.getQuestionId())));
				item.add(answerDate);

				Label statusLabel = new Label("statusLabel", resolveStatusLabel(row.getStatus()));
				statusLabel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge text-bg-dark"));
				statusLabel.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredStatusLabel", row.getQuestionId())));
				item.add(statusLabel);

				MultiLineLabel answerText = new MultiLineLabel("answerText", valueOrDash(row.getAnswerText()));
				answerText.add(AttributeModifier.replace("data-testid", buildQuestionScopedTestId("candidateCommunityQuestionsAnsweredAnswerText", row.getQuestionId())));
				item.add(answerText);
			}
		});

		Label answeredEmpty = new Label("answeredEmpty", getString("candidateCommunityQuestionsAnsweredEmpty"));
		answeredEmpty.setVisible(answeredQuestions.isEmpty());
		add(answeredEmpty);
	}

	private CommunityQuestionView toView(CandidateQuestion question) {
		CommunityQuestionView row = new CommunityQuestionView();
		row.setQuestionId(question.getCandidateQuestionId());
		row.setStatus(question.getStatus());
		row.setAskedByName(question.getAskedByName());
		row.setAskedByInitials(resolveInitials(question.getAskedByName()));
		row.setQuestionDate(resolveQuestionDate(question));
		row.setAnswerDate(resolveAnswerDate(question));
		row.setQuestionText(resolveLocalizedText(
				question.getQuestionSpanish(),
				question.getQuestionEnglish(),
				question.getQuestionPortuguese()));
		row.setAnswerText(resolveLocalizedText(
				question.getAnswerSpanish(),
				question.getAnswerEnglish(),
				question.getAnswerPortuguese()));
		return row;
	}

	private Date resolveQuestionDate(CandidateQuestion question) {
		if (question == null) {
			return null;
		}
		if (question.getCreationDate() != null) {
			return question.getCreationDate();
		}
		return question.getUpdateDate();
	}

	private Date resolveAnswerDate(CandidateQuestion question) {
		if (question == null) {
			return null;
		}
		if (question.getStatus() == CandidateQuestionStatus.PUBLISHED && question.getPublishedDate() != null) {
			return question.getPublishedDate();
		}
		if (question.getUpdateDate() != null) {
			return question.getUpdateDate();
		}
		return question.getCreationDate();
	}

	private int compareDatesDesc(Date left, Date right) {
		if (left == null && right == null) {
			return 0;
		}
		if (left == null) {
			return 1;
		}
		if (right == null) {
			return -1;
		}
		return right.compareTo(left);
	}

	private void reloadCurrentPage() {
		if (getPage() instanceof PublicTokenBasePage) {
			((PublicTokenBasePage) getPage()).reloadCurrentPageWithCurrentParameters();
			return;
		}
		setResponsePage(getPage());
	}

	private String resolveActor() {
		if (hasText(SecurityUtils.getUserAdminId())) {
			return SecurityUtils.getUserAdminId();
		}
		return "CANDIDATE_LINK";
	}

	private String resolveStatusLabel(CandidateQuestionStatus status) {
		if (status == null) {
			return "-";
		}
		return getString("candidateQuestionStatus." + status.name());
	}

	private String buildQuestionScopedTestId(String prefix, long questionId) {
		return prefix + "-" + questionId;
	}

	private String resolveLocalizedText(String spanish, String english, String portuguese) {
		LanguageCode languageCode = SecurityUtils.getLanguageCode();
		switch (languageCode) {
		case EN:
			if (hasText(english)) {
				return english;
			}
			if (hasText(spanish)) {
				return spanish;
			}
			return hasText(portuguese) ? portuguese : null;
		case PT:
			if (hasText(portuguese)) {
				return portuguese;
			}
			if (hasText(spanish)) {
				return spanish;
			}
			return hasText(english) ? english : null;
		case SP:
		default:
			if (hasText(spanish)) {
				return spanish;
			}
			if (hasText(english)) {
				return english;
			}
			return hasText(portuguese) ? portuguese : null;
		}
	}

	private String resolveInitials(String nameValue) {
		if (!hasText(nameValue)) {
			return "?";
		}
		String[] parts = nameValue.trim().split("\\s+");
		Character first = null;
		Character last = null;
		int count = 0;
		for (String part : parts) {
			Character initial = extractInitial(part);
			if (initial == null) {
				continue;
			}
			if (first == null) {
				first = initial;
			}
			last = initial;
			count++;
		}
		if (first == null) {
			return "?";
		}
		if (count <= 1 || last == null) {
			return String.valueOf(first);
		}
		return String.valueOf(first) + last;
	}

	private Character extractInitial(String value) {
		if (!hasText(value)) {
			return null;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				return Character.toUpperCase(c);
			}
		}
		return null;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean sameText(String originalText, String improvedText) {
		return StringUtils.equals(
				StringUtils.trimToEmpty(originalText).replace("\r\n", "\n"),
				StringUtils.trimToEmpty(improvedText).replace("\r\n", "\n"));
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy · HH:mm", resolveUiLocale());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date) + " UTC";
	}

	private Locale resolveUiLocale() {
		Locale locale = getLocale();
		if (locale != null) {
			return locale;
		}
		Locale securityLocale = SecurityUtils.getLocale();
		if (securityLocale != null) {
			return securityLocale;
		}
		return new Locale("es", "ES");
	}

	private static class CommunityQuestionView implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private long questionId;
		private CandidateQuestionStatus status;
		private String askedByName;
		private String askedByInitials;
		private Date questionDate;
		private Date answerDate;
		private String questionText;
		private String answerText;
		private String answerDraft;

		public long getQuestionId() {
			return questionId;
		}

		public void setQuestionId(long questionId) {
			this.questionId = questionId;
		}

		public CandidateQuestionStatus getStatus() {
			return status;
		}

		public void setStatus(CandidateQuestionStatus status) {
			this.status = status;
		}

		public String getAskedByName() {
			return askedByName;
		}

		public void setAskedByName(String askedByName) {
			this.askedByName = askedByName;
		}

		public String getAskedByInitials() {
			return askedByInitials;
		}

		public void setAskedByInitials(String askedByInitials) {
			this.askedByInitials = askedByInitials;
		}

		public Date getQuestionDate() {
			return questionDate;
		}

		public void setQuestionDate(Date questionDate) {
			this.questionDate = questionDate;
		}

		public Date getAnswerDate() {
			return answerDate;
		}

		public void setAnswerDate(Date answerDate) {
			this.answerDate = answerDate;
		}

		public String getQuestionText() {
			return questionText;
		}

		public void setQuestionText(String questionText) {
			this.questionText = questionText;
		}

		public String getAnswerText() {
			return answerText;
		}

		public void setAnswerText(String answerText) {
			this.answerText = answerText;
		}

		public String getAnswerDraft() {
			return answerDraft;
		}

		public void setAnswerDraft(String answerDraft) {
			this.answerDraft = answerDraft;
		}
	}
}
