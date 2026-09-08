package net.lacnic.elections.adminweb.ui.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicCalendarTimelinePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final String STATUS_CURRENT = "current";

	public PublicCalendarTimelinePanel(String id, List<TimelineEntryView> entries, boolean showMetaBadges) {
		super(id);

		List<TimelineEntryView> normalizedEntries = normalize(entries);
		applyTimelineStatus(normalizedEntries);

		add(new ListView<TimelineEntryView>("timelineItems", normalizedEntries) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<TimelineEntryView> item) {
				TimelineEntryView row = item.getModelObject();
				item.add(AttributeModifier.replace("data-status", row.getStatus()));
				item.add(new Label("timelineSchedule", row.getSchedule()));
				item.add(new Label("timelineTitle", row.getTitle()));

				WebMarkupContainer timelineMeta = new WebMarkupContainer("timelineMeta");
				boolean showMeta = showMetaBadges && hasText(row.getPhase());
				timelineMeta.setVisible(showMeta);

				Label phase = new Label("timelinePhase", hasText(row.getPhase()) ? row.getPhase() : "-");
				phase.setVisible(hasText(row.getPhase()));
				timelineMeta.add(phase);

				item.add(timelineMeta);

				WebMarkupContainer dot = new WebMarkupContainer("timelineDot");
				dot.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getDotCssClass()));
				WebMarkupContainer icon = new WebMarkupContainer("timelineIcon");
				icon.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getIconCssClass()));
				dot.add(icon);
				item.add(dot);
			}
		});
	}

	private List<TimelineEntryView> normalize(List<TimelineEntryView> entries) {
		List<TimelineEntryView> normalized = new ArrayList<TimelineEntryView>();
		if (entries != null) {
			for (TimelineEntryView row : entries) {
				if (row != null) {
					normalized.add(row);
				}
			}
		}
		Collections.sort(normalized, new Comparator<TimelineEntryView>() {
			@Override
			public int compare(TimelineEntryView a, TimelineEntryView b) {
				Date aStart = a.getStartDate();
				Date bStart = b.getStartDate();
				if (aStart == null && bStart == null) {
					return Integer.compare(a.getSortOrder(), b.getSortOrder());
				}
				if (aStart == null) {
					return 1;
				}
				if (bStart == null) {
					return -1;
				}
				int dateCompare = aStart.compareTo(bStart);
				if (dateCompare != 0) {
					return dateCompare;
				}
				return Integer.compare(a.getSortOrder(), b.getSortOrder());
			}
		});
		return normalized;
	}

	private void applyTimelineStatus(List<TimelineEntryView> items) {
		if (items == null || items.isEmpty()) {
			return;
		}

		Date now = new Date();
		for (TimelineEntryView item : items) {
			Date start = item.getStartDate();
			Date end = item.getEndDate() != null ? item.getEndDate() : item.getStartDate();
			if (start != null && end != null && now.after(end)) {
				item.setStatus("done");
				continue;
			}
			if (start != null && end != null && !now.before(start) && !now.after(end)) {
				item.setStatus(STATUS_CURRENT);
				continue;
			}
			item.setStatus("future");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public static class TimelineEntryView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Date startDate;
		private final Date endDate;
		private final int sortOrder;
		private final String title;
		private final String schedule;
		private final String phase;
		private final String mode;
		private String status;

		public TimelineEntryView(Date startDate, Date endDate, int sortOrder, String title, String schedule, String phase, String mode) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.sortOrder = sortOrder;
			this.title = title;
			this.schedule = schedule;
			this.phase = phase;
			this.mode = mode;
			this.status = "future";
		}

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public int getSortOrder() {
			return sortOrder;
		}

		public String getTitle() {
			return title;
		}

		public String getSchedule() {
			return schedule;
		}

		public String getPhase() {
			return phase;
		}

		public String getMode() {
			return mode;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getDotCssClass() {
			if ("done".equals(status)) {
				return "timeline-dot text-bg-dark";
			}
			if (STATUS_CURRENT.equals(status)) {
				return "timeline-dot text-bg-primary";
			}
			return "timeline-dot text-bg-light";
		}

		public String getIconCssClass() {
			if ("done".equals(status)) {
				return "ti ti-check fs-xl text-white";
			}
			if (STATUS_CURRENT.equals(status)) {
				return "ti ti-clock-hour-4 fs-xl text-white";
			}
			return "ti ti-calendar fs-xl text-dark";
		}
	}
}
