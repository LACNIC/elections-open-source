package net.lacnic.elections.adminweb.ui.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidateAnswersTaskCardPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CandidateAnswersTaskCardPanel(
			String id,
			String title,
			String badgeText,
			String badgeCssClass,
			List<? extends AnswerCardRowView> rows,
			boolean showAttentionBadge,
			boolean declarationMode) {
		super(id);
		final List<AnswerCardRowView> safeRows = rows != null ? new ArrayList<>(rows) : new ArrayList<AnswerCardRowView>();

		add(new Label("cardTitle", valueOrDash(title)));
		Label completionBadge = new Label("completionBadge", valueOrDash(badgeText));
		completionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(badgeCssClass)));
		add(completionBadge);

		add(new ListView<AnswerCardRowView>("answerRows", safeRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AnswerCardRowView> item) {
				AnswerCardRowView row = item.getModelObject();
				String itemClass = item.getIndex() == safeRows.size() - 1 ? "answer-item" : "answer-item mb-3";
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  itemClass));
				item.add(new Label("questionLabel", valueOrDash(row != null ? row.getQuestion() : null)));

				MultiLineLabel answerText = new MultiLineLabel("answerText", valueOrDash(row != null ? row.getAnswer() : null));
				answerText.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mb-0 text-muted"));
				answerText.setVisible(!declarationMode);
				item.add(answerText);

				Label answerBadge = new Label("answerBadge", "Atención");
				answerBadge.setVisible(!declarationMode && showAttentionBadge && row != null && row.isAnswered() && !row.isAccepted());
				answerBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge bg-warning-subtle text-warning"));
				item.add(answerBadge);

				Label answerDescription = new Label("answerDescription", valueOrDash(row != null ? row.getDescription() : null));
				answerDescription.setEscapeModelStrings(false);
				answerDescription.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mt-1 mb-0 small text-muted"));
				answerDescription.setVisible(!declarationMode && row != null && hasText(row.getDescription()));
				item.add(answerDescription);

				MultiLineLabel declarationText = new MultiLineLabel("declarationText", valueOrDash(row != null ? row.getDescription() : null));
				declarationText.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mt-2 mb-0 text-body"));
				declarationText.setVisible(declarationMode);
				item.add(declarationText);
			}
		});
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
