package net.lacnic.elections.adminweb.ui.token.panel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class PublicElectionRecoverLinkPanelTest {

	private static final List<String> RECOVER_LINK_RESOURCE_KEYS = Arrays.asList(
			"recoverLinkTitleParticipation",
			"recoverLinkModeHelpTitle",
			"recoverLinkModeHelpOnlyBr",
			"recoverLinkPanelIntro",
			"recoverLinkPanelHelp",
			"recoverLinkOnlyBrMilacnicNotice",
			"recoverLinkEmailLabel",
			"recoverLinkButton",
			"recoverLinkInvalidEmail",
			"recoverLinkRequestAccepted",
			"recoverLinkSendError");

	@Test
	void recoverLinkTextsExistInAllSupportedApplicationBundles() throws Exception {
		for (String resourceName : Arrays.asList(
				"net/lacnic/elections/adminweb/app/ElectionsManagerApp.properties.xml",
				"net/lacnic/elections/adminweb/app/ElectionsManagerApp_en.properties.xml",
				"net/lacnic/elections/adminweb/app/ElectionsManagerApp_pt.properties.xml")) {
			Properties bundle = loadXmlResource(resourceName);
			for (String key : RECOVER_LINK_RESOURCE_KEYS) {
				assertTrue(hasNonBlankEntry(bundle, key), () -> resourceName + " missing key " + key);
			}
		}
	}

	@Test
	void recoverLinkPanelMarkupUsesWicketComponentsForVisibleLocalizedTexts() throws Exception {
		String markup = Files.readString(Path.of(
				"src/main/java/net/lacnic/elections/adminweb/ui/token/panel/PublicElectionRecoverLinkPanel.html"),
				StandardCharsets.UTF_8);

		assertTrue(markup.contains("wicket:id=\"recoverLinkPanelIntro\""));
		assertTrue(markup.contains("wicket:id=\"recoverLinkPanelHelp\""));
		assertTrue(markup.contains("wicket:id=\"recoverLinkEmailLabel\""));
		assertFalse(markup.contains("<wicket:message key=\"recoverLinkPanelIntro\""));
		assertFalse(markup.contains("<wicket:message key=\"recoverLinkPanelHelp\""));
		assertFalse(markup.contains("<wicket:message key=\"recoverLinkEmailLabel\""));
		assertFalse(markup.contains("<wicket:message key=\"recoverLinkButton\""));
	}

	private static Properties loadXmlResource(String resourceName) throws Exception {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
			Properties properties = new Properties();
			properties.loadFromXML(inputStream);
			return properties;
		}
	}

	private static boolean hasNonBlankEntry(Properties properties, String key) {
		String value = properties.getProperty(key);
		return value != null && !value.trim().isEmpty();
	}
}
