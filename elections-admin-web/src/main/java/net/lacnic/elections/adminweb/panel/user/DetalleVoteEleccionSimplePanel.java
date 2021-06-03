package net.lacnic.elections.adminweb.panel.user;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;

public class DetalleVoteEleccionSimplePanel extends Panel {

	private static final long serialVersionUID = 1L;

	public DetalleVoteEleccionSimplePanel(String id, Election eleccion, UserVoter upd) {
		super(id);

		add(new Label("titulo", eleccion.getTitle(getIdioma(upd))));
		add(new Label("votante", upd.getVoterInformation()));
		add(new Label("cantidadVotos", upd.getVoteAmount()));

		add(new Label("maximo", String.valueOf(eleccion.getMaxCandidates())));
		Label desc = new Label("descripcion", eleccion.getDescription(getIdioma(upd)));
		desc.setEscapeModelStrings(false);
		add(desc);
	}

	public String getIdioma(UserVoter upd) {
		if (upd != null)
			return upd.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}
}
