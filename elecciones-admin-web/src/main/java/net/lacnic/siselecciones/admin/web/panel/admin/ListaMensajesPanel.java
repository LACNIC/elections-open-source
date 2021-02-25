package net.lacnic.siselecciones.admin.web.panel.admin;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Email;

public class ListaMensajesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;
	private List<Email> listaMails;

	public ListaMensajesPanel(String id, Long idEleccion, boolean all) {
		super(id);
		if (idEleccion == 0) {
			if (all)
				listaMails = Contexto.getInstance().getManagerBeanRemote().obtenerEmailsAll();
			else
				listaMails = Contexto.getInstance().getManagerBeanRemote().obtenerMailsPorEnviar();
		} else {
			if (all)
				listaMails = Contexto.getInstance().getManagerBeanRemote().obtenerMailsDeEleccion(idEleccion);
			else
				listaMails = Contexto.getInstance().getManagerBeanRemote().obtenerMailsPorEnviarDeEleccion(idEleccion);
		}

		init(listaMails);
	}

	private void init(List<Email> mensajes) {
		try {
			final ListView<Email> dataViewMensajes = new ListView<Email>("listaMensajes", mensajes) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Email> item) {
					final Email actual = item.getModelObject();
					try {
						item.add(new Label("enviado", Boolean.TRUE.equals(actual.getEnviado()) ? "Enviado" : "Por enviar"));
						item.add(new Label("instante", actual.getTipoMail()));
						item.add(new Label("tipo", actual.getTipoMail()));
						item.add(new Label("destinatario", actual.getDestinatarios()));
						item.add(new MultiLineLabel("cuerpo", "No Disponible"));
						item.add(new Label("asunto", actual.getAsunto()));

					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(dataViewMensajes);
			
		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	public List<Email> getListaMails() {
		return listaMails;
	}

	public void setListaMails(List<Email> listaMails) {
		this.listaMails = listaMails;
	}

}