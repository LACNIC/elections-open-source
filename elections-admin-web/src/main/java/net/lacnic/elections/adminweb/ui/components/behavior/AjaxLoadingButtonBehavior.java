package net.lacnic.elections.adminweb.ui.components.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class AjaxLoadingButtonBehavior extends Behavior {

	private static final long serialVersionUID = 1L;
	private static final ResourceReference AJAX_LOADING_BUTTON_JS =
			new PackageResourceReference(AjaxLoadingButtonBehavior.class, "ajax-loading-button.js");

	public static void configureAttributes(AjaxRequestAttributes attributes, String buttonId) {
		AjaxCallListener listener = new AjaxCallListener();
		listener.onBefore(buildStartScript(buttonId));
		listener.onSuccess(buildStopScript(buttonId));
		listener.onComplete(buildStopScript(buttonId));
		listener.onFailure(buildStopScript(buttonId));
		attributes.getAjaxCallListeners().add(listener);
		attributes.setChannel(new AjaxChannel(buttonId, AjaxChannel.Type.DROP));
	}

	private static String buildStartScript(String buttonId) {
		return "window.LacnicAjaxLoadingButton&&window.LacnicAjaxLoadingButton.start('" + buttonId + "');";
	}

	private static String buildStopScript(String buttonId) {
		return "window.LacnicAjaxLoadingButton&&window.LacnicAjaxLoadingButton.stop('" + buttonId + "');";
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(AJAX_LOADING_BUTTON_JS));
	}
}
