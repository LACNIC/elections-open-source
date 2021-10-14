package net.lacnic.elections.adminweb.ui.admin.election.view;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.domain.Candidate;


public class ViewCandidatePanel extends Panel {

	private static final long serialVersionUID = -2103342536982357758L;


	public ViewCandidatePanel(String id, Candidate candidate) {
		super(id);

		add(new Label("name", candidate.getName()));
		Label bio = new Label("bio", candidate.getBio(SecurityUtils.getLocale().getLanguage()));
		bio.setEscapeModelStrings(false);
		add(bio);
		add(new NonCachingImage("picture", new ImageResource(candidate.getPictureInfo(), candidate.getPictureExtension())));
	}

}
