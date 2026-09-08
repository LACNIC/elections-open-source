package net.lacnic.elections.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.lacnic.elections.domain.LanguageCode;

public enum PublicLinkRecoveryType {

	VOTER_VOTE_LINK("recoverLinkType.VOTER_VOTE_LINK", "recoverLinkTypeHelp.VOTER_VOTE_LINK", "Enlace de votacion", "Voting link", "Link de votacao"),
	NOMINATION_LINK("recoverLinkType.NOMINATION_LINK", "recoverLinkTypeHelp.NOMINATION_LINK", "Enlace para realizar nominacion", "Do nomination link",
			"Link para realizar nomeacao"),
	SUPPORT_LINK("recoverLinkType.SUPPORT_LINK", "recoverLinkTypeHelp.SUPPORT_LINK", "Enlace de confirmacion de apoyo", "Support confirmation link", "Link de confirmacao de apoio");

	private static final List<PublicLinkRecoveryType> PUBLIC_VALUES = Collections.unmodifiableList(Arrays.asList(values()));

	private final String displayResourceKey;
	private final String helpResourceKey;
	private final String spanishLabel;
	private final String englishLabel;
	private final String portugueseLabel;

	PublicLinkRecoveryType(String displayResourceKey, String helpResourceKey, String spanishLabel, String englishLabel, String portugueseLabel) {
		this.displayResourceKey = displayResourceKey;
		this.helpResourceKey = helpResourceKey;
		this.spanishLabel = spanishLabel;
		this.englishLabel = englishLabel;
		this.portugueseLabel = portugueseLabel;
	}

	public String getKey() {
		return name();
	}

	public String getDisplayResourceKey() {
		return displayResourceKey;
	}

	public String getHelpResourceKey() {
		return helpResourceKey;
	}

	public String resolveLabel(LanguageCode language) {
		LanguageCode safeLanguage = language != null ? language : LanguageCode.SP;
		switch (safeLanguage) {
		case EN:
			return englishLabel;
		case PT:
			return portugueseLabel;
		case SP:
		default:
			return spanishLabel;
		}
	}

	public static List<PublicLinkRecoveryType> getPublicValues() {
		return PUBLIC_VALUES;
	}

	public static PublicLinkRecoveryType fromKey(String key) {
		if (key == null || key.trim().isEmpty()) {
			return null;
		}
		String normalizedKey = key.trim().toUpperCase(Locale.ROOT);
		for (PublicLinkRecoveryType type : values()) {
			if (type.name().equals(normalizedKey)) {
				return type;
			}
		}
		return null;
	}
}
