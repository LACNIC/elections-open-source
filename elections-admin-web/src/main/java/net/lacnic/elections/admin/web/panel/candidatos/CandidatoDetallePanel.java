package net.lacnic.elections.admin.web.panel.candidatos;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.wicket.util.ImageResource;
import net.lacnic.elections.domain.Candidato;

public class CandidatoDetallePanel extends Panel {

	private static final long serialVersionUID = -2103342536982357758L;

	public CandidatoDetallePanel(String id, Candidato candidato) {
		super(id);

		add(new Label("nombreCandidato", candidato.getNombre()));
		Label bio = new Label("biografia", candidato.getBioEspanol());
		bio.setEscapeModelStrings(false);
		add(bio);
		add(new NonCachingImage("foto", new ImageResource(candidato.getContenidoFoto(), candidato.getExtensionFoto())));
	}
}
