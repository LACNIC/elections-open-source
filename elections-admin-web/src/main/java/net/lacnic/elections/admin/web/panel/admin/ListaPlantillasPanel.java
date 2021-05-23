package net.lacnic.elections.admin.web.panel.admin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarPlantilla;
import net.lacnic.elections.admin.dashboard.admin.DashboardEnviarEmailPaso1;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.TemplateEleccion;
import net.lacnic.elections.utils.Constants;

public class ListaPlantillasPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaPlantillasPanel(String id, long idEleccion) {
		super(id);
		List<TemplateEleccion> listPlantillasBase = AppContext.getInstance().getManagerBeanRemote().obtenerTemplatesEleccion(idEleccion);
		Collections.sort(listPlantillasBase, new Comparator<TemplateEleccion>() {

			@Override
			public int compare(TemplateEleccion o1, TemplateEleccion o2) {
				return o1.getTipoTemplate().equals(Constants.TemplateTypeNEW) ? -1 : o2.getTipoTemplate().equals(Constants.TemplateTypeNEW) ? 1 : o1.getTipoTemplate().compareTo(o2.getTipoTemplate());
			}
		});
		init(listPlantillasBase, idEleccion);
	}

	private void init(List<TemplateEleccion> plantillasBase, long idEleccion) {
		try {
			final ListView<TemplateEleccion> dataViewTemplates = new ListView<TemplateEleccion>("listaPlantillas", plantillasBase) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<TemplateEleccion> item) {
					final TemplateEleccion actual = item.getModelObject();
					try {
						item.add(new Label("asuntoEN", actual.getAsuntoEN()));
						item.add(new MultiLineLabel("cuerpoEN", actual.getCuerpoEN()));
						item.add(new Label("asuntoES", actual.getAsuntoES()));
						item.add(new MultiLineLabel("cuerpoES", actual.getCuerpoES()));
						item.add(new Label("asuntoPT", actual.getAsuntoPT()));
						item.add(new MultiLineLabel("cuerpoPT", actual.getCuerpoPT()));
						item.add(new Label("tipo", actual.getTipoTemplate()));

						Link<Void> enviarEmail = new Link<Void>("enviarEmail") {

							private static final long serialVersionUID = 7217163464200407226L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new DashboardEnviarEmailPaso1(actual.getTipoTemplate(), UtilsParameters.getId(idEleccion)));
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						enviarEmail.setVisible(botonEnviarVisible(actual) && idEleccion != 0);
						item.add(enviarEmail);

						Link<Void> editarPlantilla = new Link<Void>("editarPlantilla") {

							private static final long serialVersionUID = -995928488655867689L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new DashboardEditarPlantilla(actual.getTipoTemplate(), UtilsParameters.getId(idEleccion)));
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(editarPlantilla);

					} catch (Exception e) {
						error(e.getMessage());
					}
				}

				private boolean botonEnviarVisible(TemplateEleccion actual) {
					return (!(actual.getTipoTemplate().equals(Constants.TemplateTypeAUDITOR_AGREEMENT) || actual.getTipoTemplate().equals(Constants.TemplateTypeVOTE_CODES) || actual.getTipoTemplate().equals(Constants.TemplateTypeAUDITOR_REVISION)));
				}
			};
			add(dataViewTemplates);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}
}