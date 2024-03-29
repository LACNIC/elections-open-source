package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ButtonResendVoteEmail extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private final WebMarkupContainer container;
	private final AjaxLink<Void> ask;
	private final Link<Void> confirm;
	private final AjaxLink<Void> cancel;


	public ButtonResendVoteEmail(String id, long i) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setMarkupId("buttonResendVoteEmail."+i);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		container.setMarkupId("buttonResendVoteEmail.container."+i);
		add(container);

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonResendVoteEmail.this);
			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		ask.setMarkupId("buttonResendVoteEmail.ask."+i);
		add(ask);

		confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = -9032798913616414943L;

			@Override
			public void onClick() {
				onConfirm();
			}
		};
		confirm.setOutputMarkupPlaceholderTag(true);
		confirm.setMarkupId("buttonResendVoteEmail.confirm."+i);
		container.add(confirm);

		cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = -4235368147783120549L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				ask.setVisible(true);
				target.add(container);
				target.add(ask);
			}
		};
		cancel.setOutputMarkupPlaceholderTag(true);
		cancel.setMarkupId("buttonResendVoteEmail.cancel."+i);
		container.add(cancel);

	}

	public abstract void onConfirm();

}