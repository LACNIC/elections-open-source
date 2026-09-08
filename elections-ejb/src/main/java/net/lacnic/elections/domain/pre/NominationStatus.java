package net.lacnic.elections.domain.pre;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum NominationStatus {

	/**
	 * La organización propone al candidato. El candidato todavía no tomó una
	 * decisión.
	 */
	PROPOSED,

	/**
	 * El candidato acepta esta nominación y la elige como su candidatura activa.
	 */
	ACCEPTED_BY_CANDIDATE,

	/**
	 * El candidato rechaza esta nominación. No continúa el proceso con esta
	 * propuesta.
	 */
	REJECTED_BY_CANDIDATE,

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

	;

	private static final Set<NominationStatus> BLOCKING_FOR_NEW_NOMINATION = Collections.unmodifiableSet(EnumSet.of(
			PROPOSED,
			ACCEPTED_BY_CANDIDATE,
			APPROVED));

	public static Set<NominationStatus> blockingForNewNomination() {
		return BLOCKING_FOR_NEW_NOMINATION;
	}
}
