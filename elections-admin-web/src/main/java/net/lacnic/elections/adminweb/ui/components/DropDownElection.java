package net.lacnic.elections.adminweb.ui.components;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;


public class DropDownElection extends DropDownChoice<Election> {

	private static final long serialVersionUID = 4766509011662393037L;


	public DropDownElection(String id, IModel<Election> model) {
		super(id);
		List<Election> elections = AppContext.getInstance().getManagerBeanRemote().getElectionsLightThisYear();
		Election election = new Election(0);
		elections.add(election);
		Collections.sort(elections, new Comparator<Election>() {
			@Override
			public int compare(Election o1, Election o2) {
				return o1.getElectionId() == 0 ? -1 : 1;
			}
		});
		setChoices(elections);
		setModel(model);

		setChoiceRenderer(new IChoiceRenderer<Election>() {
			private static final long serialVersionUID = 57388878999120541L;

			@Override
			public Object getDisplayValue(Election election) {
				return election.getTitleSpanish();
			}

			@Override
			public String getIdValue(Election election, int index) {
				return String.valueOf(election.getElectionId());
			}

			@Override
			public Election getObject(String id, IModel<? extends List<? extends Election>> list) {
				Optional<List<? extends Election>> optionalList = Optional.ofNullable(list.getObject());
				if(optionalList.isPresent()) {
					return optionalList.get()
							.stream()
							.filter(current -> String.valueOf(current.getElectionId()).equals(id))
							.findFirst()
							.orElse(null);
				} else {
					return null;
				}
			}
		});
		setRequired(true);
	}

	@Override
	protected String getNullKeyDisplayValue() {
		return "Select...";
	}

}
