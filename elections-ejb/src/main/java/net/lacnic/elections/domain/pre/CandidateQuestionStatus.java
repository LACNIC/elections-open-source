package net.lacnic.elections.domain.pre;

import net.lacnic.elections.domain.LanguageCode;

public enum CandidateQuestionStatus {
	QUESTION_RECEIVED_LACNIC,
	QUESTION_READY_FOR_CANDIDATE,
	ANSWER_SUBMITTED_BY_CANDIDATE,
	PUBLISHED,
	REJECTED;

	public CandidateQuestionOwner resolveOwner() {
		switch (this) {
		case QUESTION_READY_FOR_CANDIDATE:
			return CandidateQuestionOwner.CANDIDATE;
		case PUBLISHED:
		case REJECTED:
			return CandidateQuestionOwner.NONE;
		case ANSWER_SUBMITTED_BY_CANDIDATE:
		case QUESTION_RECEIVED_LACNIC:
		default:
			return CandidateQuestionOwner.LACNIC;
		}
	}

	public String resolveLabel(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		switch (resolvedLanguage) {
		case EN:
			return resolveLabelEnglish();
		case PT:
			return resolveLabelPortuguese();
		case SP:
		default:
			return resolveLabelSpanish();
		}
	}

	public String resolveActionRequired(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		switch (resolvedLanguage) {
		case EN:
			return resolveActionEnglish();
		case PT:
			return resolveActionPortuguese();
		case SP:
		default:
			return resolveActionSpanish();
		}
	}

	private String resolveLabelSpanish() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Pregunta recibida por la organización";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Pregunta lista para candidato";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Respuesta enviada por candidato";
		case PUBLISHED:
			return "Publicada";
		case REJECTED:
		default:
			return "Rechazada";
		}
	}

	private String resolveLabelEnglish() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Question received by the organization";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Question ready for candidate";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Answer submitted by candidate";
		case PUBLISHED:
			return "Published";
		case REJECTED:
		default:
			return "Rejected";
		}
	}

	private String resolveLabelPortuguese() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Pergunta recebida pela organização";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Pergunta pronta para o candidato";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Resposta enviada pelo candidato";
		case PUBLISHED:
			return "Publicada";
		case REJECTED:
		default:
			return "Rejeitada";
		}
	}

	private String resolveActionSpanish() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Revisar la pregunta (traducirla) y definir si es una pregunta válida para enviar al candidato o rechazarla.";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Responder la pregunta enviada por un integrante de la comunidad de la organización. (la pregunta será marcada como pendiente de respuesta en la interfaz pública de la elección)";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Revisar la respuesta, traducirla y decidir su publicación.";
		case PUBLISHED:
			return "Verificar la publicación en el portal público.";
		case REJECTED:
		default:
			return "Verificar la NO publicación en el portal público.";
		}
	}

	private String resolveActionEnglish() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Review the question (translate it) and determine whether it is valid to send to the candidate or reject it.";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Answer the question submitted by a member of the organization's community. (the question will be marked as pending response in the public election interface)";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Review the answer, translate it, and decide whether to publish it.";
		case PUBLISHED:
			return "Verify the publication in the public portal.";
		case REJECTED:
		default:
			return "Verify the non-publication in the public portal.";
		}
	}

	private String resolveActionPortuguese() {
		switch (this) {
		case QUESTION_RECEIVED_LACNIC:
			return "Revisar a pergunta (traduzi-la) e definir se é uma pergunta válida para enviar ao candidato ou rejeitá-la.";
		case QUESTION_READY_FOR_CANDIDATE:
			return "Responder à pergunta enviada por um integrante da comunidade da organização. (a pergunta será marcada como pendente de resposta na interface pública da eleição)";
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			return "Revisar a resposta, traduzi-la e decidir sua publicação.";
		case PUBLISHED:
			return "Verificar a publicação no portal público.";
		case REJECTED:
		default:
			return "Verificar a NÃO publicação no portal público.";
		}
	}
}
