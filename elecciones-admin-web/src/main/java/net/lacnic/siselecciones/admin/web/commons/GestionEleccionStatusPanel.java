package net.lacnic.siselecciones.admin.web.commons;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;

public class GestionEleccionStatusPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public GestionEleccionStatusPanel(String id, Eleccion eleccion) {
		super(id);

		Link<Void> detalle = new Link<Void>("detalle") {
			private static final long serialVersionUID = 1791144645448735702L;

			@Override
			public void onClick() {
				setResponsePage(DashboardGestionEleccion.class, UtilsParameters.getId(eleccion.getIdEleccion()));
			}
		};
		detalle.setEnabled(eleccion.getIdEleccion() != 0);
		add(detalle);

		Link<Void> padron = new Link<Void>("padron") {
			private static final long serialVersionUID = -846692535588349478L;

			@Override
			public void onClick() {
				setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdEleccion()));
			}
		};
		padron.setEnabled(eleccion.getIdEleccion() != 0);
		add(padron);

		Link<Void> candidatos = new Link<Void>("candidatos") {
			private static final long serialVersionUID = 8317962582176759530L;

			@Override
			public void onClick() {
				setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
			}
		};
		candidatos.setEnabled(eleccion.getIdEleccion() != 0);
		add(candidatos);

		Link<Void> auditores = new Link<Void>("auditores") {
			private static final long serialVersionUID = 5621792633991539504L;

			@Override
			public void onClick() {
				setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getIdEleccion()));
			}
		};
		auditores.setEnabled(eleccion.getIdEleccion() != 0);
		add(auditores);

		if (id.equalsIgnoreCase("tabDetalle"))
			detalle.add(new AttributeAppender("class", " current"));
		else
			detalle.add(new AttributeAppender("class", " done"));

		if (id.equalsIgnoreCase("tabPadron"))
			padron.add(new AttributeAppender("class", " current"));
		else if (eleccion.isPadronSeteado())
			padron.add(new AttributeAppender("class", " done"));
		else
			padron.add(new AttributeAppender("class", " disabled"));

		if (id.equalsIgnoreCase("tabCandidatos"))
			candidatos.add(new AttributeAppender("class", " current"));
		else if (eleccion.isCandidatosSeteado())
			candidatos.add(new AttributeAppender("class", " done"));
		else
			candidatos.add(new AttributeAppender("class", " disabled"));

		if (id.equalsIgnoreCase("tabAuditores"))
			auditores.add(new AttributeAppender("class", " current"));
		else if (eleccion.isAuditoresSeteado())
			auditores.add(new AttributeAppender("class", " done"));
		else
			auditores.add(new AttributeAppender("class", " disabled"));

	}

}
