package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.text.MessageFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public abstract class AbstractAcceptNominationTaskSituationPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	protected AbstractAcceptNominationTaskSituationPanel(String id, AcceptNominationTaskResolution resolution, String situationName) {
		super(id, resolution);

		add(new Label("panelTitle", MessageFormat.format(getString("acceptNominationTaskPlaceholderTitle"), situationName)));

		Label restrictionMessage = new Label("restrictionMessage", new ResourceModel(getTaskResolution().getRestrictionMessageKey(), ""));
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);
	}
}
