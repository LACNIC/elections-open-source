package net.lacnic.siselecciones.admin.web.commons;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Eleccion;


public class DropDownEleccion extends DropDownChoice<Eleccion> {

	private static final long serialVersionUID = 4766509011662393037L;

	public DropDownEleccion(String id, IModel<Eleccion> model) {
		super(id);
		List<Eleccion> elecciones = Contexto.getInstance().getManagerBeanRemote().obtenerEleccionesLightEsteAnio();
		Eleccion e = new Eleccion(0);
		elecciones.add(e);
		Collections.sort(elecciones, new Comparator<Eleccion>() {
			@Override
			public int compare(Eleccion o1, Eleccion o2) {
				return o1.getIdEleccion() == 0 ? -1 : 1;
			}
		});
		setChoices(elecciones);
		setModel(model);
		setChoiceRenderer(new IChoiceRenderer<Eleccion>() {

			private static final long serialVersionUID = 57388878999120541L;

			@Override
			public Object getDisplayValue(Eleccion e) {
				return e.getTituloEspanol();
			}

			@Override
			public String getIdValue(Eleccion e, int index) {
				return String.valueOf(e.getIdEleccion());
			}

			@Override
			public Eleccion getObject(String id, IModel<? extends List<? extends Eleccion>> lista) {
				Optional<List<? extends Eleccion>> listaOpt = Optional.ofNullable(lista.getObject());
				if(listaOpt.isPresent()) {
					return listaOpt.get()
							.stream()
							.filter(actual -> String.valueOf(actual.getIdEleccion()).equals(id))
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