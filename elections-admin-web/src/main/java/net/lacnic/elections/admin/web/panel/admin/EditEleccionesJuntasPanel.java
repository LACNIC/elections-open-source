package net.lacnic.elections.admin.web.panel.admin;

import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.SupraEleccion;

public class EditEleccionesJuntasPanel extends Panel {

	private static final long serialVersionUID = -7732839509000652567L;

	private List<String> elecciones = AppContext.getInstance().getManagerBeanRemote().obtenerEleccionesIdDesc();
	private String selectedA;
	private String selectedB;

	public EditEleccionesJuntasPanel(String id) {
		super(id);

		Form<Void> form = new Form<>("formEditTemplate");
		add(form);

		DropDownChoice<String> eleccionA = new DropDownChoice<>("eleccion1", new PropertyModel<>(this, "selectedA"), elecciones);
		form.add(eleccionA);
		eleccionA.setRequired(true);

		DropDownChoice<String> eleccionB = new DropDownChoice<>("eleccion2", new PropertyModel<>(this, "selectedB"), elecciones);
		form.add(eleccionB);
		eleccionB.setRequired(true);

		form.add(new Button("agregar") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				long idEleccionA = Long.parseLong(selectedA.split("-")[0]);
				long idEleccionB = Long.parseLong(selectedB.split("-")[0]);
				
				Eleccion e1 = AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(idEleccionA); 
				Eleccion e2 = AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(idEleccionB);
				
				Date dtIniE1 = e1.getFechaInicio();
				Date dtIniE2 = e2.getFechaInicio();
				
				
				if (selectedA.equals(selectedB)) {
					error(getString("uniteElecEditError1"));
				} else if (AppContext.getInstance().getManagerBeanRemote().isSupraEleccion(idEleccionA)) {
					error(getString("uniteElecEditError2") + selectedA);
				} else if (AppContext.getInstance().getManagerBeanRemote().isSupraEleccion(idEleccionB)) {
					error(getString("uniteElecEditError2") + selectedB);
				} else if (dtIniE1.compareTo(dtIniE2) != 0) {
					error(getString("uniteElecEditError3"));
				} else {
					SupraEleccion supra = new SupraEleccion();
					supra.setIdEleccionA(idEleccionA);
					supra.setIdEleccionB(idEleccionB);
					AppContext.getInstance().getManagerBeanRemote().actualizarSupraEleccion(supra);
					setResponsePage(new DashboardEleccionesJuntas());
				}
			}
		});

	}

	public String getSelectedA() {
		return this.selectedA;
	}

	public String getSelectedB() {
		return this.selectedB;
	}

}
