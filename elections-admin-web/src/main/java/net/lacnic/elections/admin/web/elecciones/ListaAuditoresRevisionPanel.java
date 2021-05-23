package net.lacnic.elections.admin.web.elecciones;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.utils.UtilsLinks;

public class ListaAuditoresRevisionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaAuditoresRevisionPanel(String id, long idEleccion) {
		super(id);
		try {
			List<Auditor> auditores = AppContext.getInstance().getManagerBeanRemote().obtenerAuditoresEleccion(idEleccion);
			ListView<Auditor> auditoresDataView = new ListView<Auditor>("auditoresList", auditores) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Auditor> item) {
					final Auditor actual = item.getModelObject();
					item.add(new Label("nombreA", actual.getNombre()));
					item.add(new Label("mailA", actual.getMail()));
					item.add(new Label("revision", (actual.isHabilitaRevision() ? "SI" : "NO")));
					String calcularLinkVotar = UtilsLinks.calcularLinkResultadoAuditor(actual.getTokenResultado());
					Label textoLinkVotar = new Label("textoLink", calcularLinkVotar);
					ExternalLink linkvotar = new ExternalLink("link", calcularLinkVotar);
					linkvotar.add(textoLinkVotar);
					item.add(linkvotar);
					item.setVisible(actual.isComisionado());
				}
			};
			add(auditoresDataView);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}