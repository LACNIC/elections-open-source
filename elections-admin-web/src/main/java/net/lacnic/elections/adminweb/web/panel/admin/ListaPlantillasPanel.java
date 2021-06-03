package net.lacnic.elections.adminweb.web.panel.admin;

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

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardEditarPlantilla;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardEnviarEmailPaso1;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.utils.Constants;

public class ListaPlantillasPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaPlantillasPanel(String id, long idEleccion) {
		super(id);
		List<ElectionEmailTemplate> listPlantillasBase = AppContext.getInstance().getManagerBeanRemote().getElectionEmailTemplates(idEleccion);
		Collections.sort(listPlantillasBase, new Comparator<ElectionEmailTemplate>() {

			@Override
			public int compare(ElectionEmailTemplate o1, ElectionEmailTemplate o2) {
				return o1.getTemplateType().equals(Constants.TemplateTypeNEW) ? -1 : o2.getTemplateType().equals(Constants.TemplateTypeNEW) ? 1 : o1.getTemplateType().compareTo(o2.getTemplateType());
			}
		});
		init(listPlantillasBase, idEleccion);
	}

	private void init(List<ElectionEmailTemplate> plantillasBase, long idEleccion) {
		try {
			final ListView<ElectionEmailTemplate> dataViewTemplates = new ListView<ElectionEmailTemplate>("listaPlantillas", plantillasBase) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ElectionEmailTemplate> item) {
					final ElectionEmailTemplate actual = item.getModelObject();
					try {
						item.add(new Label("asuntoEN", actual.getSubjectEN()));
						item.add(new MultiLineLabel("cuerpoEN", actual.getBodyEN()));
						item.add(new Label("asuntoES", actual.getSubjectSP()));
						item.add(new MultiLineLabel("cuerpoES", actual.getBodySP()));
						item.add(new Label("asuntoPT", actual.getSubjectPT()));
						item.add(new MultiLineLabel("cuerpoPT", actual.getBodyPT()));
						item.add(new Label("tipo", actual.getTemplateType()));

						Link<Void> enviarEmail = new Link<Void>("enviarEmail") {

							private static final long serialVersionUID = 7217163464200407226L;

							@Override
							public void onClick() {
								try {
									setResponsePage(new DashboardEnviarEmailPaso1(actual.getTemplateType(), UtilsParameters.getId(idEleccion)));
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
									setResponsePage(new DashboardEditarPlantilla(actual.getTemplateType(), UtilsParameters.getId(idEleccion)));
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

				private boolean botonEnviarVisible(ElectionEmailTemplate actual) {
					return (!(actual.getTemplateType().equals(Constants.TemplateTypeAUDITOR_AGREEMENT) || actual.getTemplateType().equals(Constants.TemplateTypeVOTE_CODES) || actual.getTemplateType().equals(Constants.TemplateTypeAUDITOR_REVISION)));
				}
			};
			add(dataViewTemplates);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}
}