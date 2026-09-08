package net.lacnic.elections.adminweb.ui.token;

import org.apache.wicket.Component;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

public class CandidateProfileInfoCardPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CandidateProfileInfoCardPanel(String id, Component candidatePicture, String candidateName, String candidateMail, String profileLinkedin, String profileBio) {
		super(id);

		add(candidatePicture);
		add(new Label("candidateName", valueOrDash(candidateName)));
		add(new Label("candidateMail", valueOrDash(candidateMail)));

        String profileLinkedinUrl = normalizeExternalUrl(profileLinkedin);
        boolean hasProfileLinkedin = hasText(profileLinkedinUrl);
        ExternalLink profileLinkedinLink = new ExternalLink("profileLinkedinLink", profileLinkedinUrl != null ? profileLinkedinUrl : "#", valueOrDash(profileLinkedin));
        profileLinkedinLink.add(new AttributeModifier("aria-label", valueOrDash(profileLinkedin)));
        profileLinkedinLink.setVisible(hasProfileLinkedin);
        add(profileLinkedinLink);
		add(new Label("profileLinkedinNotAvailable", "-").setVisible(!hasProfileLinkedin));

		Label profileBioLabel = new Label("profileBio", CandidateBiographyUtils.toRenderableMarkup(valueOrDash(profileBio)));
		profileBioLabel.setEscapeModelStrings(false);
		add(profileBioLabel);
	}

	private String normalizeExternalUrl(String value) {
		String trimmedValue = trimToNull(value);
		if (!hasText(trimmedValue)) {
			return null;
		}
		if (trimmedValue.startsWith("http://") || trimmedValue.startsWith("https://")) {
			return trimmedValue;
		}
		return "https://" + trimmedValue;
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
