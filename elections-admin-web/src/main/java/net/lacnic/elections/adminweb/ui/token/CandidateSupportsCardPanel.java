package net.lacnic.elections.adminweb.ui.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class CandidateSupportsCardPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CandidateSupportsCardPanel(String id, String badgeText, String badgeCssClass, List<? extends SupportCardRowView> rows, String emptyMessage) {
		super(id);
		final List<SupportCardRowView> safeRows = rows != null ? new ArrayList<>(rows) : new ArrayList<SupportCardRowView>();

		Label completionBadge = new Label("completionBadge", valueOrDash(badgeText));
		completionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(badgeCssClass)));
		add(completionBadge);

		add(new ListView<SupportCardRowView>("supportRows", safeRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SupportCardRowView> item) {
				SupportCardRowView row = item.getModelObject();
				item.add(new Label("supportTitle", valueOrDash(row.getDisplayTitle())));
				Label subtitle = new Label("supportSubtitle", valueOrDash(row.getSubtitle()));
				subtitle.setVisible(hasText(row.getSubtitle()));
				item.add(subtitle);
				Label status = new Label("supportStatusBadge", valueOrDash(row.getStatusBadgeText()));
				status.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(row.getStatusBadgeClass())));
				item.add(status);
				Label supportDate = new Label("supportDate", valueOrDash(row.getSupportDateText()));
				supportDate.setVisible(hasText(row.getSupportDateText()));
				item.add(supportDate);
			}
		});

		add(new Label("supportEmptyMessage", valueOrDash(emptyMessage)).setVisible(safeRows.isEmpty()));
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
