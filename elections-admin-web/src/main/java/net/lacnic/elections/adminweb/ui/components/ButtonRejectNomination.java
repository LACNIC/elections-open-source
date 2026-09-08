package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;

public abstract class ButtonRejectNomination extends Panel {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_ASK_RESOURCE_KEY = "acceptNominationRejectAskButton";
	private static final String DEFAULT_IRREVERSIBLE_RESOURCE_KEY = "acceptNominationRejectIrreversible";
	private static final String DEFAULT_CONFIRM_RESOURCE_KEY = "acceptNominationRejectConfirmButton";
	private static final String DEFAULT_CANCEL_RESOURCE_KEY = "acceptNominationRejectCancelButton";
	private static final String DEFAULT_ASK_BUTTON_CSS_CLASS = "btn btn-light";
	private static final String DEFAULT_CONFIRM_BUTTON_CSS_CLASS = "btn btn-danger";
	private static final String DEFAULT_CANCEL_BUTTON_CSS_CLASS = "btn btn-secondary";

	private final WebMarkupContainer container;
	private final AjaxLink<Void> ask;

	public ButtonRejectNomination(String id) {
		this(
				id,
				DEFAULT_ASK_RESOURCE_KEY,
				DEFAULT_IRREVERSIBLE_RESOURCE_KEY,
				DEFAULT_CONFIRM_RESOURCE_KEY,
				DEFAULT_CANCEL_RESOURCE_KEY,
				DEFAULT_ASK_BUTTON_CSS_CLASS,
				DEFAULT_CONFIRM_BUTTON_CSS_CLASS,
				DEFAULT_CANCEL_BUTTON_CSS_CLASS);
	}

	public ButtonRejectNomination(
			String id,
			String askResourceKey,
			String irreversibleResourceKey,
			String confirmResourceKey,
			String cancelResourceKey,
			String askButtonCssClass,
			String confirmButtonCssClass,
			String cancelButtonCssClass) {
		super(id);
		setOutputMarkupPlaceholderTag(true);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		add(container);
		container.add(new Label("irreversibleLabel", getString(irreversibleResourceKey)));

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonRejectNomination.this);
			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		ask.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, askButtonCssClass));
		ask.add(new Label("askLabel", getString(askResourceKey)));
		add(ask);

		Link<Void> confirm = new Link<Void>("confirm") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				onConfirm();
			}
		};
		confirm.setOutputMarkupPlaceholderTag(true);
		confirm.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, confirmButtonCssClass));
		confirm.add(new Label("confirmLabel", getString(confirmResourceKey)));
		container.add(confirm);

		AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				ask.setVisible(true);
				target.add(container);
				target.add(ask);
			}
		};
		cancel.setOutputMarkupPlaceholderTag(true);
		cancel.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, cancelButtonCssClass));
		cancel.add(new Label("cancelLabel", getString(cancelResourceKey)));
		container.add(cancel);
	}

	public abstract void onConfirm();
}
