package net.lacnic.elections.admin.web.panel.elecciones;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.ejb.ManagerEleccionesEJB;
import net.lacnic.elections.utils.UtilsLinks;

public class ListaAuditoresPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private ManagerEleccionesEJB managerEjbBean;

	public ListaAuditoresPanel(String id, long idEleccion) {
		super(id);
		managerEjbBean = AppContext.getInstance().getManagerBeanRemote();
		obtenerAuditores(idEleccion);
	}

	private void obtenerAuditores(long idEleccion) {
		try {
			List<Auditor> listaAuditores = managerEjbBean.obtenerAuditoresEleccion(idEleccion);

			ListaAuditoresPanel.this.setOutputMarkupPlaceholderTag(true);
			final Form<Void> form = new Form<>("form");
			final WebMarkupContainer contenedorListaAuditores = new WebMarkupContainer("contenedorAuditores");
			contenedorListaAuditores.setOutputMarkupPlaceholderTag(true);
			final ListView<Auditor> dataViewAuditores = new ListView<Auditor>("dataViewAuditores", listaAuditores) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
					final Auditor actual = item.getModelObject();

					item.add(new Label("nombre", actual.getName()));
					item.add(new Label("email", actual.getMail()));
					item.add(new Label("conforme", actual.isAgreement() ? "Si" : "NO"));

					final Label urlLinkVotacionUsuario = new Label("urlLinkVotacionUsuario", UtilsLinks.calcularLinkResultadoAuditor(actual.getResulttoke()));
					urlLinkVotacionUsuario.setOutputMarkupPlaceholderTag(true);	
					item.add(urlLinkVotacionUsuario);
				}
			};
			contenedorListaAuditores.add(dataViewAuditores);
			form.add(contenedorListaAuditores);
			add(form);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}