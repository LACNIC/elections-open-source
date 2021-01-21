package net.lacnic.siselecciones.admin.web.panel.avanzadas;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEditarParametrosPage;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardParametros;
import net.lacnic.siselecciones.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Parametro;

public class ListadoParametrosPanel extends Panel {

	private static final long serialVersionUID = -8554113800494186242L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<Parametro> listadoParametros;

	public ListadoParametrosPanel(String id) {
		super("listadoParametro");

		listadoParametros = Contexto.getInstance().getManagerBeanRemote().obtenerListadoParamteros();

		final ListView<Parametro> listadoParams = new ListView<Parametro>("listadoParametros", listadoParametros) {

			private static final long serialVersionUID = 2146073776795909288L;

			@Override
			protected void populateItem(final ListItem<Parametro> item) {
				try {
					final Parametro p = item.getModelObject();

					item.add(new Label("claveParametro", p.getClave()));
					item.add(new Label("valorParametro", p.getValor()));

					BookmarkablePageLink<Void> editarParametro = new BookmarkablePageLink<>("editarParametro", DashboardEditarParametrosPage.class, UtilsParameters.getClaveId(p.getClave()));
					editarParametro.setMarkupId("editarParametro" + item.getIndex());
					item.add(editarParametro);

					BotonConConfirmacionEliminar borrar = new BotonConConfirmacionEliminar("borrar", item.getIndex()) {

						private static final long serialVersionUID = -7529344711667630816L;

						@Override
						public void onConfirmar() {					
							Contexto.getInstance().getManagerBeanRemote().borrarParametro(p.getClave(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							getSession().info(getString("advParameterExito"));
							setResponsePage(DashboardParametros.class); 
						}
					};
					item.add(borrar);					

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(listadoParams);
	}

}
