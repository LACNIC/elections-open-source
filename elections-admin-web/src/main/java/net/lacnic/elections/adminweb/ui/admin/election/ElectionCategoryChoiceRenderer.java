package net.lacnic.elections.adminweb.ui.admin.election;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

import net.lacnic.elections.domain.ElectionCategory;

public class ElectionCategoryChoiceRenderer implements IChoiceRenderer<ElectionCategory> {

	private static final long serialVersionUID = 1L;
	private static final String MODERATORS_LABEL = "NO ESTATUTARIAS";

	private final Component component;

	public ElectionCategoryChoiceRenderer(Component component) {
		this.component = component;
	}

	@Override
	public Object getDisplayValue(ElectionCategory object) {
		if (object == null) {
			return "";
		}
		if (ElectionCategory.MODERATORS == object) {
			return component.getString("electionCategorySelect.MODERATORS", null, MODERATORS_LABEL);
		}
		String label = component.getString("publicElectionsCategory." + object.name(), null, object.name());
		return label != null && !label.trim().isEmpty() ? label : object.name();
	}

}
