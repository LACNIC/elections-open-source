package net.lacnic.siselecciones.utils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
/**
 * Helper utilizado para encriptar/desencriptar con RSA
 * @author Antonymous
 *
 */
public final class RSAKeys {
	/**
	 * RSAPrivateKey esta en java.security.interfaces
	 */
	private RSAPrivateKey privateKey;
	private String exponente;
	private String modulus;
	private RSAPublicKey publicKey;

	// sus metodos get y set
	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public String getExponente() {
		return exponente;
	}

	public void setExponente(String exponente) {
		this.exponente = exponente;
	}

	public String getModulus() {
		return modulus;
	}

	public void setModulus(String modulus) {
		this.modulus = modulus;
	}

	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}
}