package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;

import net.lacnic.elections.domain.LanguageCode;

public class PublicElectionOfficialResultSnapshot implements Serializable {

	private static final long serialVersionUID = 5364138766917385530L;

	private PublicElectionSnapshotMetadata metadata;
	private Long electionId;
	private PublicElectionCoreSnapshot.LocalizedTextData result = new PublicElectionCoreSnapshot.LocalizedTextData();
	private OfficialResultLetterData spanishLetter;
	private OfficialResultLetterData englishLetter;
	private OfficialResultLetterData portugueseLetter;

	public PublicElectionSnapshotMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PublicElectionSnapshotMetadata metadata) {
		this.metadata = metadata;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public PublicElectionCoreSnapshot.LocalizedTextData getResult() {
		return result;
	}

	public void setResult(PublicElectionCoreSnapshot.LocalizedTextData result) {
		this.result = result;
	}

	public OfficialResultLetterData getSpanishLetter() {
		return spanishLetter;
	}

	public void setSpanishLetter(OfficialResultLetterData spanishLetter) {
		this.spanishLetter = spanishLetter;
	}

	public OfficialResultLetterData getEnglishLetter() {
		return englishLetter;
	}

	public void setEnglishLetter(OfficialResultLetterData englishLetter) {
		this.englishLetter = englishLetter;
	}

	public OfficialResultLetterData getPortugueseLetter() {
		return portugueseLetter;
	}

	public void setPortugueseLetter(OfficialResultLetterData portugueseLetter) {
		this.portugueseLetter = portugueseLetter;
	}

	public String getResult(LanguageCode languageCode) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			if (hasText(result != null ? result.getEnglish() : null)) {
				return result.getEnglish();
			}
			if (hasText(result != null ? result.getSpanish() : null)) {
				return result.getSpanish();
			}
			return hasText(result != null ? result.getPortuguese() : null) ? result.getPortuguese() : null;
		case PT:
			if (hasText(result != null ? result.getPortuguese() : null)) {
				return result.getPortuguese();
			}
			if (hasText(result != null ? result.getSpanish() : null)) {
				return result.getSpanish();
			}
			return hasText(result != null ? result.getEnglish() : null) ? result.getEnglish() : null;
		case SP:
		default:
			if (hasText(result != null ? result.getSpanish() : null)) {
				return result.getSpanish();
			}
			if (hasText(result != null ? result.getEnglish() : null)) {
				return result.getEnglish();
			}
			return hasText(result != null ? result.getPortuguese() : null) ? result.getPortuguese() : null;
		}
	}

	public OfficialResultLetterData getResultLetterData(LanguageCode languageCode) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			if (hasLetter(englishLetter)) {
				return englishLetter;
			}
			if (hasLetter(spanishLetter)) {
				return spanishLetter;
			}
			return hasLetter(portugueseLetter) ? portugueseLetter : null;
		case PT:
			if (hasLetter(portugueseLetter)) {
				return portugueseLetter;
			}
			if (hasLetter(spanishLetter)) {
				return spanishLetter;
			}
			return hasLetter(englishLetter) ? englishLetter : null;
		case SP:
		default:
			if (hasLetter(spanishLetter)) {
				return spanishLetter;
			}
			if (hasLetter(englishLetter)) {
				return englishLetter;
			}
			return hasLetter(portugueseLetter) ? portugueseLetter : null;
		}
	}

	public byte[] getResultLetter(LanguageCode languageCode) {
		OfficialResultLetterData letterData = getResultLetterData(languageCode);
		return letterData != null ? letterData.getContent() : null;
	}

	public String getResultLetterUrl(LanguageCode languageCode) {
		OfficialResultLetterData letterData = getResultLetterData(languageCode);
		return letterData != null ? letterData.getUrl() : null;
	}

	private boolean hasLetter(OfficialResultLetterData value) {
		return value != null && value.getContent() != null && value.getContent().length > 0;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public static class OfficialResultLetterData implements Serializable {
		private static final long serialVersionUID = 2485269672188797422L;

		private LanguageCode languageCode;
		private String url;
		private byte[] content;

		public LanguageCode getLanguageCode() {
			return languageCode;
		}

		public void setLanguageCode(LanguageCode languageCode) {
			this.languageCode = languageCode;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}
	}
}
