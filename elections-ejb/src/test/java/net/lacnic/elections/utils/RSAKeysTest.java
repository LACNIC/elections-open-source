package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.Test;

class RSAKeysTest {

	@Test
	void gettersAndSettersPreserveAssignedValues() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(512);
		KeyPair keyPair = generator.generateKeyPair();

		RSAKeys keys = new RSAKeys();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		keys.setPrivateKey(privateKey);
		keys.setPublicKey(publicKey);
		keys.setExponent("010001");
		keys.setModulus("ABCDEF");

		assertSame(privateKey, keys.getPrivateKey());
		assertSame(publicKey, keys.getPublicKey());
		assertEquals("010001", keys.getExponent());
		assertEquals("ABCDEF", keys.getModulus());
	}
}
