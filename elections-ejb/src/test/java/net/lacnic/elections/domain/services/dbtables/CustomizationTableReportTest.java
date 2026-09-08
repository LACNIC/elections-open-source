package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Customization;

 class CustomizationTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorEncodesContainedImagesOrReturnsEmpty() {
		Customization customization = new Customization();
		customization.setCustomizationId(22L);
		customization.setPicSmallLogo("small-logo.png");
		customization.setPicBigLogo("big-logo.png");
		customization.setPicSymbol("symbol");
		customization.setContPicSmallLogo(new byte[] { 10, 20, 30 });
		customization.setContPicBigLogo(null);
		customization.setContPicSymbol(new byte[] {});
		customization.setSiteTitle("Site Title");
		customization.setLoginTitle("Login Title");
		customization.setShowHome(true);
		customization.setHomeHtml("<html/>");

		CustomizationTableReport report = new CustomizationTableReport(customization);

		assertEquals(22L, report.getCustomizationId().longValue());
		assertEquals("ChQe", report.getContPicSmallLogo());
		assertEquals("", report.getContPicBigLogo());
		assertEquals("", report.getContPicSymbol());
		assertEquals("small-logo.png", report.getPicSmallLogo());
		assertEquals("big-logo.png", report.getPicBigLogo());
		assertEquals("symbol", report.getPicSymbol());
		assertEquals("Site Title", report.getSiteTitle());
		assertEquals("Login Title", report.getLoginTitle());
		assertEquals(Boolean.TRUE, report.isShowHome());
		assertEquals("<html/>", report.getHomeHtml());
	}
}
