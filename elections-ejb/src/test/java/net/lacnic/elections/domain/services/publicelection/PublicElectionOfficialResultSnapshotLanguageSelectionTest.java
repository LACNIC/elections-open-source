package net.lacnic.elections.domain.services.publicelection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.LanguageCode;

class PublicElectionOfficialResultSnapshotLanguageSelectionTest extends TestCase {

	static Test suite() {
		return new TestSuite(PublicElectionOfficialResultSnapshotLanguageSelectionTest.class);
	}

	@org.junit.jupiter.api.Test
	void testGetResultLetterDataReturnsRequestedLanguageWhenAvailable() {
		PublicElectionOfficialResultSnapshot snapshot = new PublicElectionOfficialResultSnapshot();
		snapshot.setSpanishLetter(letter(LanguageCode.SP, new byte[] { 1 }, "es"));
		snapshot.setEnglishLetter(letter(LanguageCode.EN, new byte[] { 2 }, "en"));
		snapshot.setPortugueseLetter(letter(LanguageCode.PT, new byte[] { 3 }, "pt"));

		assertSame(snapshot.getSpanishLetter(), snapshot.getResultLetterData(LanguageCode.SP));
		assertSame(snapshot.getEnglishLetter(), snapshot.getResultLetterData(LanguageCode.EN));
		assertSame(snapshot.getPortugueseLetter(), snapshot.getResultLetterData(LanguageCode.PT));
	}

	@org.junit.jupiter.api.Test
	void testGetResultReturnsRequestedLanguageWhenAvailable() {
		PublicElectionOfficialResultSnapshot snapshot = new PublicElectionOfficialResultSnapshot();
		PublicElectionCoreSnapshot.LocalizedTextData result = new PublicElectionCoreSnapshot.LocalizedTextData();
		result.setSpanish("es");
		result.setEnglish("en");
		result.setPortuguese("pt");
		snapshot.setResult(result);

		assertEquals("es", snapshot.getResult(LanguageCode.SP));
		assertEquals("en", snapshot.getResult(LanguageCode.EN));
		assertEquals("pt", snapshot.getResult(LanguageCode.PT));
	}

	private PublicElectionOfficialResultSnapshot.OfficialResultLetterData letter(LanguageCode languageCode, byte[] content, String url) {
		PublicElectionOfficialResultSnapshot.OfficialResultLetterData data =
				new PublicElectionOfficialResultSnapshot.OfficialResultLetterData();
		data.setLanguageCode(languageCode);
		data.setContent(content);
		data.setUrl(url);
		return data;
	}
}
