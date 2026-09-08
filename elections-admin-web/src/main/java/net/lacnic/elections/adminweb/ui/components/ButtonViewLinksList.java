package net.lacnic.elections.adminweb.ui.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class ButtonViewLinksList extends Panel {

	private static final long serialVersionUID = 5506248595921371292L;

	private final WebMarkupContainer container;
	private final WebMarkupContainer linksContainer;
	private final AjaxLink<Void> viewLinkButton;
	private final Label noLinks;

	public ButtonViewLinksList(String id, long itemId, List<ViewLinkItem> links) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setMarkupId("viewLinksListContainer." + itemId);

		List<ViewLinkItem> safeLinks = links == null ? Collections.emptyList() : new ArrayList<>(links);
		boolean hasLinks = !safeLinks.isEmpty();

		container = new WebMarkupContainer("viewLinksContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setMarkupId("viewLinksListContainer.container." + itemId);
		add(container);

		viewLinkButton = new AjaxLink<Void>("viewLinkButton") {
			private static final long serialVersionUID = -1994632717579714312L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				linksContainer.setVisible(true);
				viewLinkButton.setVisible(false);
				target.add(container);
				registerActivity();
			}
		};
		viewLinkButton.setMarkupId("viewLinksListContainer.viewLinkButton." + itemId);
		viewLinkButton.setOutputMarkupPlaceholderTag(true);
		viewLinkButton.setVisible(hasLinks);
		container.add(viewLinkButton);

		noLinks = new Label("noLinks", "-");
		noLinks.setVisible(!hasLinks);
		container.add(noLinks);

		linksContainer = new WebMarkupContainer("linksContainer");
		linksContainer.setOutputMarkupPlaceholderTag(true);
		linksContainer.setVisible(false);
		linksContainer.add(new ListView<ViewLinkItem>("linksList", safeLinks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ViewLinkItem> item) {
				ViewLinkItem viewLinkItem = item.getModelObject();
				ExternalLink voteLink = new ExternalLink("voteLink", viewLinkItem.getLink());
				voteLink.add(new Label("voteLinkText", viewLinkItem.getLabel()));
				item.add(voteLink);
			}
		});
		container.add(linksContainer);
	}

	public abstract void registerActivity();

	public static class ViewLinkItem implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String link;
		private final String label;

		public ViewLinkItem(String link, String label) {
			this.link = link;
			this.label = label;
		}

		public String getLink() {
			return link;
		}

		public String getLabel() {
			return label;
		}
	}
}
