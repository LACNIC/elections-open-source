package net.lacnic.siselecciones.admin.dashboard.user;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.dashboard.error.Error404;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.dominio.UsuarioPadron;

public class DashboardYaVoto extends DashboardPublicBasePage {

	private static final long serialVersionUID = -7536199173314577391L;
	
	private UsuarioPadron upd;

	public DashboardYaVoto(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedbackPanel"));
		getSession().info(getString("titulo_yv"));

		add(new Label("titulo", upd.getEleccion().getTitulo(getIdioma())));
		add(new Label("votante", upd.getInformacionDelVotante()));
		add(new Label("maximo", String.valueOf(upd.getEleccion().getMaxCandidatos())));
		add(new Label("cantidadVotos", upd.getCantVotos()));

		Label desc = new Label("descripcion", upd.getEleccion().getDescripcion(getIdioma()));
		desc.setEscapeModelStrings(false);
		add(desc);

		List<Object[]> elegidos = Contexto.getInstance().getVotanteBeanRemote().obtenerCandidatosVotacion(upd.getIdUsuarioPadron(), upd.getEleccion().getIdEleccion());
		final ListView<Object[]> seleccionados = new ListView<Object[]>("codigos", elegidos) {
			private static final long serialVersionUID = 1786359392545666490L;			
			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] actual = item.getModelObject();				
				int nroFila = item.getIndex() + 1;				
				item.add(new Label("numero", String.valueOf(nroFila)));
				Label codigo = new Label("codigo", (String) actual[1]);
				codigo.setMarkupId("codigo" + item.getIndex());
				item.add(codigo);
				
			}
		};
		add(seleccionados);
	}

	@Override
	public Class validarToken(PageParameters params) {
		upd = Contexto.getInstance().getVotanteBeanRemote().verificarAccesoUP(getToken());
		if (upd == null) {
			Contexto.getInstance().getVotanteBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {
			setEleccion(upd.getEleccion());
		}
		return null;
	}

}
