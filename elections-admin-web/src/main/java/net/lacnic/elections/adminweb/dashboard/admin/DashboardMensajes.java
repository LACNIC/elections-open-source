package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.components.DropDownElection;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.admin.ListaMensajesPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

@AuthorizeInstantiation("elections-only-one")
public class DashboardMensajes extends DashboardAdminBasePage {

	private static final long serialVersionUID = -9012477457154755011L;

	private Election eleccionFiltrar;

	public DashboardMensajes(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Form<Void> form = new Form<>("form");
		add(form);
		DropDownElection dropDownElecciones = new DropDownElection("eleccion", new PropertyModel<>(DashboardMensajes.this, "eleccionFiltrar"));
		form.add(dropDownElecciones);

		form.add(new Button("filtrar") {

			private static final long serialVersionUID = -5319374745120063191L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				params.remove(UtilsParameters.getIdText());
				params.add(UtilsParameters.getIdText(), getEleccionFiltrar().getElectionId());
				setResponsePage(DashboardMensajes.class, params);
			}

		});

		add(new ListaMensajesPanel("listMensajes", UtilsParameters.getIdAsLong(params), UtilsParameters.isAll(params)));
	}

	public Election getEleccionFiltrar() {
		return eleccionFiltrar;
	}

	public void setEleccionFiltrar(Election eleccionFiltrar) {
		this.eleccionFiltrar = eleccionFiltrar;
	}
}
