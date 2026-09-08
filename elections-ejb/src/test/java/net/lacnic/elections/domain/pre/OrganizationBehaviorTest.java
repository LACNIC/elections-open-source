package net.lacnic.elections.domain.pre;

import java.lang.reflect.Method;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.evra.registro.CategoriasEnum;

 class OrganizationBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(OrganizationBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testMemberAndCategoryDefaults() {
		Organization organization = new Organization();
		assertTrue(organization.isMember());
		assertNull(organization.getCategory());
		assertNull(organization.getCategoryEnum());
		assertNull(organization.getMembershipContactLanguage());
		assertNull(organization.getMembershipContactLanguageEnum());
	}

	@org.junit.jupiter.api.Test

	 void testCategoryAndLanguageSettersNormalizeAndFallback() {
		Organization organization = new Organization();

		organization.setCategory("none");
		assertEquals("none", organization.getCategory());
		assertEquals(CategoriasEnum.NONE, organization.getCategoryEnum());

		organization.setCategory("  ND ");
		assertEquals(CategoriasEnum.ND.getNombreTabla(), organization.getCategory());
		organization.setCategoryEnum(CategoriasEnum.ASN_ONLY);
		assertEquals("asn-only", organization.getCategory());

		organization.setMembershipContactLanguage("es-ar");
		assertEquals("SP", organization.getMembershipContactLanguage());
		organization.setMembershipContactLanguage("   ");
		assertNull(organization.getMembershipContactLanguage());

		organization.setMembershipContactLanguageEnum(LanguageCode.PT);
		assertEquals("PT", organization.getMembershipContactLanguage());
		assertEquals(LanguageCode.PT, organization.getMembershipContactLanguageEnum());
	}

	@org.junit.jupiter.api.Test

	 void testLifecycleCallbacksPopulateTimestamps() throws Exception {
		Organization organization = new Organization();
		organization.setElection(new Election());

		Method onCreate = Organization.class.getDeclaredMethod("onCreateTimestamps");
		onCreate.setAccessible(true);
		onCreate.invoke(organization);

		Date createdAt = organization.getCreatedAt();
		assertNotNull(createdAt);
		assertNotNull(organization.getUpdatedAt());

		Thread.sleep(5L);
		Method onUpdate = Organization.class.getDeclaredMethod("onUpdateTimestamp");
		onUpdate.setAccessible(true);
		onUpdate.invoke(organization);

		assertNotNull(organization.getUpdatedAt());
		assertFalse(organization.getUpdatedAt().before(createdAt));
	}
}
