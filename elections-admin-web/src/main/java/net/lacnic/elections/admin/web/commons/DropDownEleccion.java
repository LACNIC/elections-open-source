package net.lacnic.elections.admin.web.commons;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Election;


public class DropDownEleccion extends DropDownChoice<Election> {

	private static final long serialVersionUID = 4766509011662393037L;

	public DropDownEleccion(String id, IModel<Election> model) {
		super(id);
		List<Election> elecciones = AppContext.getInstance().getManagerBeanRemote().obtenerEleccionesLightEsteAnio();
		Election e = new Election(0);
		elecciones.add(e);
		Collections.sort(elecciones, new Comparator<Election>() {
			@Override
			public int compare(Election o1, Election o2) {
				return o1.getIdElection() == 0 ? -1 : 1;
			}
		});
		setChoices(elecciones);
		setModel(model);
		setChoiceRenderer(new IChoiceRenderer<Election>() {

			private static final long serialVersionUID = 57388878999120541L;

			@Override
			public Object getDisplayValue(Election e) {
				return e.getTitleSpanish();
			}

			@Override
			public String getIdValue(Election e, int index) {
				return String.valueOf(e.getIdElection());
			}

			@Override
			public Election getObject(String id, IModel<? extends List<? extends Election>> lista) {
				Optional<List<? extends Election>> listaOpt = Optional.ofNullable(lista.getObject());
				if(listaOpt.isPresent()) {
					return listaOpt.get()
							.stream()
							.filter(actual -> String.valueOf(actual.getIdElection()).equals(id))
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
		return "Seleccione...";
	}

}