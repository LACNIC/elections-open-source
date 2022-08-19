package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ButtonViewLink extends Panel {

	private static final long serialVersionUID = 3593755033216180134L;

	private final WebMarkupContainer container;
	private final AjaxLink<Void> viewLinkButton;
	private final ExternalLink voteLink;


	public ButtonViewLink(String id, long i, String voteLinkText) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setMarkupId("viewLinkContainer." + i);

		container = new WebMarkupContainer("viewLinkContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setMarkupId("viewLinkContainer.container." + i);
		add(container);

		Label voteLinkTextLabel = new Label("voteLinkText", voteLinkText);
		voteLink = new ExternalLink("voteLink", voteLinkText);
		voteLink.setMarkupId("viewLinkContainer.voteLink." + i);
		voteLink.add(voteLinkTextLabel);
		voteLink.setVisible(false);

		viewLinkButton = new AjaxLink<Void>("viewLinkButton") {
			private static final long serialVersionUID = 6329100614104709360L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(container);
				voteLink.setVisible(true);
				viewLinkButton.setVisible(false);
				if(voteLink.isVisible()) {
					registerActivity();
				}
			}
		};
		viewLinkButton.setMarkupId("viewLinkContainer.viewLinkButton." + i);
		viewLinkButton.setOutputMarkupPlaceholderTag(true);

		container.add(voteLink);
		container.add(viewLinkButton);
	}

	public abstract void registerActivity();

}
