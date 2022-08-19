package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class ButtonDeleteWithConfirmation extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private AjaxLink<Void> ask;
	private WebMarkupContainer container;
	private Link<Void> confirm;
	private AjaxLink<Void> cancel;


	public ButtonDeleteWithConfirmation(String id, long i) {
		super(id);
		setMarkupId("buttonDeleteWithConfirmation." + i);

		setOutputMarkupPlaceholderTag(true);

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonDeleteWithConfirmation.this);
			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		add(ask);
		ask.setMarkupId("buttonDeleteWithConfirmation.ask" + i);

		confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = -5497166159069241885L;

			@Override
			public void onClick() {
				onConfirm();
			}
		};
		confirm.setMarkupId("buttonDeleteWithConfirmation.confirmRemove" + i);

		cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 5860568364324870195L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				ask.setVisible(true);
				target.add(container);
				target.add(ask);
			}
		};
		cancel.setMarkupId("buttonDeleteWithConfirmation.cancelRemove" + i);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		container.add(confirm);
		container.add(cancel);

		add(container);
	}

	public abstract void onConfirm();


	public AjaxLink<Void> getAsk() {
		return ask;
	}

	public void setAsk(AjaxLink<Void> ask) {
		this.ask = ask;
	}

	public WebMarkupContainer getContainer() {
		return container;
	}

	public void setContainer(WebMarkupContainer container) {
		this.container = container;
	}

	public Link<Void> getConfirm() {
		return confirm;
	}

	public void setConfirm(Link<Void> confirm) {
		this.confirm = confirm;
	}

	public AjaxLink<Void> getCancel() {
		return cancel;
	}

	public void setCancel(AjaxLink<Void> cancel) {
		this.cancel = cancel;
	}

}
