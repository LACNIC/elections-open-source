package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ButtonAuditorApproval extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private WebMarkupContainer container;
	private AjaxLink<Void> ask;
	private Link<Void> confirm;
	private AjaxLink<Void> cancel;


	public ButtonAuditorApproval(String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		add(container);

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonAuditorApproval.this);
			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		add(ask);

		confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = 4253669343392802353L;

			@Override
			public void onClick() {
				onConfirm();
			}
		};
		confirm.setOutputMarkupPlaceholderTag(true);
		container.add(confirm);

		cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 3080230554321781158L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				ask.setVisible(true);
				target.add(container);
				target.add(ask);
			}
		};
		cancel.setOutputMarkupPlaceholderTag(true);
		container.add(cancel);

	}

	public abstract void onConfirm();

}
