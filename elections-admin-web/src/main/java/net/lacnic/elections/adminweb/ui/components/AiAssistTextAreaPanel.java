package net.lacnic.elections.adminweb.ui.components;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.adminweb.ui.components.behavior.AjaxLoadingButtonBehavior;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.domain.pre.CandidateTextImprovementResponse;
import net.lacnic.elections.utils.Constants;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AiAssistTextAreaPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final String AI_CHANGE_ANIMATION_CLASSES = " animate__animated animate__zoomIn";
	private static final long AI_TEXT_IMPROVEMENT_CONFIG_CACHE_TTL_MS = 60_000L;
	private static final String KEY_TEXT_ASSISTANCE_PROCESSING_ERROR = "textAssistanceProcessingError";
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static volatile Boolean aiTextImprovementConfigured;
	private static volatile long aiTextImprovementConfigCheckedAt;
	private static final Object aiTextImprovementConfigLock = new Object();

	@FunctionalInterface
	public interface FeedbackUpdater extends Serializable {
		void update(AjaxRequestTarget target);
	}

	@FunctionalInterface
	public interface ImprovementExecutor extends Serializable {
		CandidateTextImprovementResponse improve(String originalText, CandidateTextImprovementInstruction instruction, String styleContext);
	}

	@FunctionalInterface
	public interface TextAreaConfigurer extends Serializable {
		void configure(TextArea<String> textArea);
	}

	private final IModel<String> valueModel;
	private final IModel<String> styleContextModel;
	private final FeedbackUpdater feedbackUpdater;
	private final ImprovementExecutor improvementExecutor;
	private final boolean counterEnabled;
	private final boolean spellcheckEnabled;
	private final boolean styleReviewEnabled;
	private final boolean discardEnabled;
	private final boolean translateEnabled;
	private final boolean readOnly;
	private final int maxLength;
	private final int rows;
	private final TextAreaConfigurer textAreaConfigurer;
	private final boolean aiTextImprovementEnabled;

	private String valueBeforeAiChanges;
	private boolean aiChangesPending;
	private boolean aiAnimationEnabled;

	public AiAssistTextAreaPanel(
			String id,
			IModel<String> valueModel,
			IModel<String> styleContextModel,
			FeedbackUpdater feedbackUpdater,
			ImprovementExecutor improvementExecutor,
			boolean readOnly,
			boolean counterEnabled,
				boolean spellcheckEnabled,
				boolean styleReviewEnabled,
				boolean discardEnabled,
				int maxLength,
				int rows) {
		this(id, valueModel, styleContextModel, feedbackUpdater, improvementExecutor, readOnly, counterEnabled, spellcheckEnabled, styleReviewEnabled, discardEnabled, maxLength, rows, false, null);
	}

	public AiAssistTextAreaPanel(
			String id,
			IModel<String> valueModel,
			IModel<String> styleContextModel,
			FeedbackUpdater feedbackUpdater,
			ImprovementExecutor improvementExecutor,
			boolean readOnly,
			boolean counterEnabled,
			boolean spellcheckEnabled,
			boolean styleReviewEnabled,
			boolean discardEnabled,
			int maxLength,
			int rows,
			boolean translateEnabled) {
		this(id, valueModel, styleContextModel, feedbackUpdater, improvementExecutor, readOnly, counterEnabled, spellcheckEnabled, styleReviewEnabled, discardEnabled, maxLength, rows, translateEnabled, null);
	}

	public AiAssistTextAreaPanel(
			String id,
			IModel<String> valueModel,
			IModel<String> styleContextModel,
			FeedbackUpdater feedbackUpdater,
			ImprovementExecutor improvementExecutor,
			boolean readOnly,
			boolean counterEnabled,
				boolean spellcheckEnabled,
				boolean styleReviewEnabled,
				boolean discardEnabled,
				int maxLength,
				int rows,
				TextAreaConfigurer textAreaConfigurer) {
		this(id, valueModel, styleContextModel, feedbackUpdater, improvementExecutor, readOnly, counterEnabled, spellcheckEnabled, styleReviewEnabled, discardEnabled, maxLength, rows, false, textAreaConfigurer);
	}

	public AiAssistTextAreaPanel(
			String id,
			IModel<String> valueModel,
			IModel<String> styleContextModel,
			FeedbackUpdater feedbackUpdater,
			ImprovementExecutor improvementExecutor,
			boolean readOnly,
			boolean counterEnabled,
			boolean spellcheckEnabled,
			boolean styleReviewEnabled,
			boolean discardEnabled,
			int maxLength,
			int rows,
			boolean translateEnabled,
			TextAreaConfigurer textAreaConfigurer) {
		super(id, valueModel);
		if (maxLength <= 0) {
			throw new IllegalArgumentException("maxLength must be greater than zero");
		}
		this.valueModel = valueModel;
		this.styleContextModel = styleContextModel;
		this.feedbackUpdater = feedbackUpdater;
		this.improvementExecutor = improvementExecutor;
		this.counterEnabled = counterEnabled;
		this.spellcheckEnabled = spellcheckEnabled;
		this.styleReviewEnabled = styleReviewEnabled;
		this.discardEnabled = discardEnabled;
		this.translateEnabled = translateEnabled;
		this.readOnly = readOnly;
		this.maxLength = maxLength;
		this.rows = rows;
		this.textAreaConfigurer = textAreaConfigurer;
		this.aiTextImprovementEnabled = isAiTextImprovementConfigured();
		add(new AjaxLoadingButtonBehavior());

		TextArea<String> textArea = new TextArea<>("textArea", this.valueModel);
		textArea.setEnabled(!this.readOnly);
		textArea.setOutputMarkupId(true);
		if (this.maxLength > 0) {
			textArea.add(AttributeModifier.replace("maxlength", String.valueOf(this.maxLength)));
			textArea.add(StringValidator.maximumLength(this.maxLength));
		}
		if (this.rows > 0) {
			textArea.add(AttributeModifier.replace("rows", String.valueOf(this.rows)));
		}
		textArea.add(new AttributeAppender(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return aiAnimationEnabled ? AI_CHANGE_ANIMATION_CLASSES : "";
			}
		}));
		if (this.textAreaConfigurer != null) {
			this.textAreaConfigurer.configure(textArea);
		}
		add(textArea);

		Label counter = new Label("counter", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return buildCharactersCounter(valueModel.getObject(), maxLength);
			}
		});
		counter.setOutputMarkupPlaceholderTag(true);
		counter.setOutputMarkupId(true);
		counter.setVisible(this.counterEnabled);
		add(counter);

		if (this.counterEnabled) {
			textArea.add(new AjaxFormComponentUpdatingBehavior("input") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(counter);
				}
			});
		}

		WebMarkupContainer actionsContainer = new WebMarkupContainer("actionsContainer");
		actionsContainer.setVisible(!this.readOnly && this.aiTextImprovementEnabled && (this.spellcheckEnabled || this.styleReviewEnabled || this.discardEnabled || this.translateEnabled));
		add(actionsContainer);

		AjaxButton discardButton = new AjaxButton("discardButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				discardTextImprovementChanges(textArea, counter, this, target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				updateExternalFeedback(target);
			}
		};
		discardButton.setDefaultFormProcessing(false);
		discardButton.setOutputMarkupPlaceholderTag(true);
		discardButton.setVisible(false);
		actionsContainer.add(discardButton);

		AjaxButton spellcheckButton = buildTextImprovementButton(
				"spellcheckButton",
				CandidateTextImprovementInstruction.SPELLING_REVIEW,
				textArea,
				counter,
				discardButton);
		actionsContainer.add(spellcheckButton);

		AjaxButton styleReviewButton = buildTextImprovementButton(
				"styleReviewButton",
				CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW,
				textArea,
				counter,
				discardButton);
		actionsContainer.add(styleReviewButton);

		AjaxButton translateToSpanishButton = buildTextImprovementButton(
				"translateToSpanishButton",
				CandidateTextImprovementInstruction.TRANSLATE_TO_SPANISH,
				textArea,
				counter,
				discardButton);
		actionsContainer.add(translateToSpanishButton);

		AjaxButton translateToPortugueseButton = buildTextImprovementButton(
				"translateToPortugueseButton",
				CandidateTextImprovementInstruction.TRANSLATE_TO_PORTUGUESE,
				textArea,
				counter,
				discardButton);
		actionsContainer.add(translateToPortugueseButton);

		AjaxButton translateToEnglishButton = buildTextImprovementButton(
				"translateToEnglishButton",
				CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH,
				textArea,
				counter,
				discardButton);
		actionsContainer.add(translateToEnglishButton);

		spellcheckButton.setVisible(!this.readOnly && this.spellcheckEnabled);
		styleReviewButton.setVisible(!this.readOnly && this.styleReviewEnabled);
		translateToSpanishButton.setVisible(!this.readOnly && this.translateEnabled);
		translateToPortugueseButton.setVisible(!this.readOnly && this.translateEnabled);
		translateToEnglishButton.setVisible(!this.readOnly && this.translateEnabled);
	}

	private static boolean isAiTextImprovementConfigured() {
		long now = System.currentTimeMillis();
		if (aiTextImprovementConfigured != null && now - aiTextImprovementConfigCheckedAt < AI_TEXT_IMPROVEMENT_CONFIG_CACHE_TTL_MS) {
			return aiTextImprovementConfigured;
		}

		synchronized (aiTextImprovementConfigLock) {
			now = System.currentTimeMillis();
			if (aiTextImprovementConfigured != null && now - aiTextImprovementConfigCheckedAt < AI_TEXT_IMPROVEMENT_CONFIG_CACHE_TTL_MS) {
				return aiTextImprovementConfigured;
			}

			boolean configured = resolveAiTextImprovementConfiguration();
			aiTextImprovementConfigured = configured;
			aiTextImprovementConfigCheckedAt = now;
			return configured;
		}
	}

	public static boolean isAiTextImprovementAvailable() {
		return isAiTextImprovementConfigured();
	}

	private static boolean resolveAiTextImprovementConfiguration() {
		try {
			ElectionsManagerEJB managerBean = AppContext.getInstance().getManagerBeanRemote();
			if (managerBean == null) {
				return false;
			}

			return isAiTextImprovementConfigured(managerBean.getParametersAll());
		} catch (Exception ex) {
			appLogger.warn("Unable to resolve AI text improvement configuration. Feature disabled as fallback.");
			return false;
		}
	}

	static boolean isAiTextImprovementConfigured(List<Parameter> parameters) {
		if (parameters == null) {
			return false;
		}

		String openAiUrl = null;
		String openAiApiKey = null;
		boolean featureEnabled = true;
		for (Parameter parameter : parameters) {
			if (parameter == null || StringUtils.isBlank(parameter.getKey())) {
				continue;
			}
			if (Constants.OPENAI_URL.equals(parameter.getKey())) {
				openAiUrl = parameter.getValue();
			} else if (Constants.OPENAI_API_KEY.equals(parameter.getKey())) {
				openAiApiKey = parameter.getValue();
			} else if (Constants.AI_TEXT_IMPROVEMENT_ENABLED.equals(parameter.getKey())) {
				featureEnabled = Boolean.parseBoolean(StringUtils.trimToEmpty(parameter.getValue()));
			}
		}
		return featureEnabled && StringUtils.isNotBlank(openAiUrl) && StringUtils.isNotBlank(openAiApiKey);
	}

	private AjaxButton buildTextImprovementButton(
			String id,
			CandidateTextImprovementInstruction instruction,
			TextArea<String> textArea,
			Label counter,
			AjaxButton discardButton) {
		AjaxButton button = new AjaxButton(id) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				AjaxLoadingButtonBehavior.configureAttributes(attributes, getMarkupId());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				applyTextImprovement(instruction, textArea, counter, discardButton, target);
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				updateExternalFeedback(target);
			}
		};
		button.setDefaultFormProcessing(false);
		button.setOutputMarkupId(true);
		return button;
	}

	private void applyTextImprovement(
			CandidateTextImprovementInstruction instruction,
			TextArea<String> textArea,
			Label counter,
			AjaxButton discardButton,
			AjaxRequestTarget target) {
		String originalValue = StringUtils.trimToNull(valueModel.getObject());
		if (originalValue == null) {
			SecurityUtils.error(getString("textAssistanceEmptyTextError"));
			updateExternalFeedback(target);
			return;
		}

		CandidateTextImprovementResponse improvementResponse;
		try {
			improvementResponse = improvementExecutor.improve(originalValue, instruction, resolveStyleContext(instruction));
		} catch (Exception ex) {
			appLogger.error("Error improving text with IA", ex);
			SecurityUtils.error(getString(KEY_TEXT_ASSISTANCE_PROCESSING_ERROR));
			updateExternalFeedback(target);
			return;
		}

		if (improvementResponse == null || !improvementResponse.isProcessed()) {
			if (improvementResponse != null && improvementResponse.isDailyLimitReached()) {
				SecurityUtils.error(getString("textAssistanceDailyLimitReached"));
			} else {
				SecurityUtils.error(getString(KEY_TEXT_ASSISTANCE_PROCESSING_ERROR));
			}
			updateExternalFeedback(target);
			return;
		}

		String improvedText = normalizeImprovedText(improvementResponse.getImprovedText());
		if (StringUtils.isBlank(improvedText)) {
			SecurityUtils.error(getString(KEY_TEXT_ASSISTANCE_PROCESSING_ERROR));
			updateExternalFeedback(target);
			return;
		}

		if (sameText(originalValue, improvedText)) {
			SecurityUtils.info(getString(resolveNoChangesMessageKey(instruction)));
			if (discardEnabled) {
				discardButton.setVisible(aiChangesPending);
				target.add(discardButton);
			}
			updateExternalFeedback(target);
			return;
		}

		if (discardEnabled && !aiChangesPending) {
			valueBeforeAiChanges = originalValue;
		}
		aiChangesPending = discardEnabled;
		aiAnimationEnabled = true;
		setCurrentValue(improvedText);
		syncTextAreaValue(textArea, improvedText);
		if (discardEnabled) {
			discardButton.setVisible(true);
		}
		SecurityUtils.info(getString(resolveAppliedMessageKey(instruction)));
		target.add(textArea);
		target.add(counter);
		target.add(discardButton);
		updateExternalFeedback(target);
	}

	private void discardTextImprovementChanges(
			TextArea<String> textArea,
			Label counter,
			AjaxButton discardButton,
			AjaxRequestTarget target) {
		if (!discardEnabled) {
			discardButton.setVisible(false);
			target.add(discardButton);
			return;
		}

		if (!aiChangesPending) {
			discardButton.setVisible(false);
			target.add(discardButton);
			return;
		}

		String originalValue = StringUtils.defaultString(valueBeforeAiChanges);
		setCurrentValue(originalValue);
		syncTextAreaValue(textArea, originalValue);
		resetAiChangesState();
		aiAnimationEnabled = true;
		discardButton.setVisible(false);
		SecurityUtils.info(getString("textAssistanceDiscarded"));
		target.add(textArea);
		target.add(counter);
		target.add(discardButton);
		updateExternalFeedback(target);
	}

	private void resetAiChangesState() {
		valueBeforeAiChanges = null;
		aiChangesPending = false;
		aiAnimationEnabled = false;
	}

	private String resolveStyleContext(CandidateTextImprovementInstruction instruction) {
		if (instruction != CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW || styleContextModel == null) {
			return null;
		}
		return StringUtils.trimToNull(styleContextModel.getObject());
	}

	private void setCurrentValue(String value) {
		valueModel.setObject(value);
	}

	private String resolveAppliedMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingApplied"
				: instruction == CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW
						? "textAssistanceStyleApplied"
						: "textAssistanceTranslationApplied";
	}

	private String resolveNoChangesMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingNoChanges"
				: instruction == CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW
						? "textAssistanceStyleNoChanges"
						: "textAssistanceTranslationNoChanges";
	}

	private boolean sameText(String originalText, String improvedText) {
		return StringUtils.equals(
				StringUtils.trimToEmpty(originalText).replace("\r\n", "\n"),
				StringUtils.trimToEmpty(improvedText).replace("\r\n", "\n"));
	}

	static String normalizeImprovedText(String value) {
		if (value == null) {
			return null;
		}
		return Parser.unescapeEntities(value, false);
	}

	private String buildCharactersCounter(String value, int maxAllowedLength) {
		int currentLength = StringUtils.length(StringUtils.defaultString(value));
		return currentLength + "/" + maxAllowedLength;
	}

	private void syncTextAreaValue(TextArea<String> textArea, String value) {
		textArea.setModelObject(value);
		textArea.clearInput();
		textArea.modelChanged();
	}

	private void updateExternalFeedback(AjaxRequestTarget target) {
		if (feedbackUpdater != null) {
			feedbackUpdater.update(target);
		}
	}
}
