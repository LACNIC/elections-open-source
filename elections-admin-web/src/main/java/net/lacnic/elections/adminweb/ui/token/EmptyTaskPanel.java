package net.lacnic.elections.adminweb.ui.token;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class EmptyTaskPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public EmptyTaskPanel(String id) {
		super(id);
		add(new Label("message", new ResourceModel("acceptNominationTaskEmptyPanel")));
	}
}
