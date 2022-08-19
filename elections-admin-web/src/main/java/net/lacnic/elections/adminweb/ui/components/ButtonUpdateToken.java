package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ButtonUpdateToken extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private final WebMarkupContainer container;
	private final AjaxLink<Void> ask;
	private final Link<Void> confirm;
	private final AjaxLink<Void> cancel;


	public ButtonUpdateToken(String id, long i) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setMarkupId("buttonUpdateToken."+i);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		container.setMarkupId("buttonUpdateToken.container."+i);
		add(container);

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonUpdateToken.this);
			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		ask.setMarkupId("buttonUpdateToken.ask."+i);
		add(ask);

		confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = -7189444424235489327L;

			@Override
			public void onClick() {
				onConfirm();
			}
		};
		confirm.setOutputMarkupPlaceholderTag(true);
		confirm.setMarkupId("buttonUpdateToken.confirm."+i);
		container.add(confirm);

		cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 1141166607185779739L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				ask.setVisible(true);
				target.add(container);
				target.add(ask);
			}
		};
		cancel.setOutputMarkupPlaceholderTag(true);
		cancel.setMarkupId("buttonUpdateToken.cancel."+i);
		container.add(cancel);

	}

	public abstract void onConfirm();

}
