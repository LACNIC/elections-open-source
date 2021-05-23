package net.lacnic.elections.admin.panel.user;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.UsuarioPadron;

public class DetalleVoteEleccionSimplePanel extends Panel {

	private static final long serialVersionUID = 1L;

	public DetalleVoteEleccionSimplePanel(String id, Eleccion eleccion, UsuarioPadron upd) {
		super(id);

		add(new Label("titulo", eleccion.getTitulo(getIdioma(upd))));
		add(new Label("votante", upd.getInformacionDelVotante()));
		add(new Label("cantidadVotos", upd.getCantVotos()));

		add(new Label("maximo", String.valueOf(eleccion.getMaxCandidatos())));
		Label desc = new Label("descripcion", eleccion.getDescripcion(getIdioma(upd)));
		desc.setEscapeModelStrings(false);
		add(desc);
	}

	public String getIdioma(UsuarioPadron upd) {
		if (upd != null)
			return upd.getIdioma();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}
}
