package net.lacnic.elections.domain.pre;

public enum SupportStatus {

	/**
	 * La organización propone al candidato. El candidato todavía no tomó una
	 * decisión.
	 */
	PROPOSED,

	/**
	 * El candidato acepta esta nominación y la elige como su candidatura activa.
	 */
	ACCEPTED,

	/**
	 * El candidato rechaza esta nominación. No continúa el proceso con esta
	 * propuesta.
	 */
	REJECTED,

	/**
	 * La nominación fue invalidada por el sistema o por la comisión electoral
	 * (datos incorrectos, apoyos inválidos, fuera de plazo, etc.).
	 */
	INVALID,

	/**
	 * La nominación fue validada completamente (apoyos correctos, etapas completas)
	 * y aprobada por la comisión electoral.
	 */
	APPROVED
}