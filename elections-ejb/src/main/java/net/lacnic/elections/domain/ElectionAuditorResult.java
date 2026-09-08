package net.lacnic.elections.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditor_results")
public class ElectionAuditorResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditor_results_seq")
	@SequenceGenerator(name = "auditor_results_seq", sequenceName = "auditor_results_seq", allocationSize = 1)
	@Column(name = "auditor_result_id")
	private long auditorResultId;

	@Column(name = "election_id", nullable = false, unique = true)
	private long electionId;

	@Column(name = "result_spanish", columnDefinition = "TEXT")
	private String resultSpanish;

	@Column(name = "result_english", columnDefinition = "TEXT")
	private String resultEnglish;

	@Column(name = "result_portuguese", columnDefinition = "TEXT")
	private String resultPortuguese;

	@Column(name = "result_letter_spanish")
	private byte[] resultLetterSpanish;

	@Column(name = "result_letter_english")
	private byte[] resultLetterEnglish;

	@Column(name = "result_letter_portuguese")
	private byte[] resultLetterPortuguese;

	public long getAuditorResultId() {
		return auditorResultId;
	}

	public void setAuditorResultId(long auditorResultId) {
		this.auditorResultId = auditorResultId;
	}

	public long getElectionId() {
		return electionId;
	}

	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}

	public String getResultSpanish() {
		return resultSpanish;
	}

	public void setResultSpanish(String resultSpanish) {
		this.resultSpanish = resultSpanish;
	}

	public String getResultEnglish() {
		return resultEnglish;
	}

	public void setResultEnglish(String resultEnglish) {
		this.resultEnglish = resultEnglish;
	}

	public String getResultPortuguese() {
		return resultPortuguese;
	}

	public void setResultPortuguese(String resultPortuguese) {
		this.resultPortuguese = resultPortuguese;
	}

	public byte[] getResultLetterSpanish() {
		return resultLetterSpanish;
	}

	public void setResultLetterSpanish(byte[] resultLetterSpanish) {
		this.resultLetterSpanish = resultLetterSpanish;
	}

	public byte[] getResultLetterEnglish() {
		return resultLetterEnglish;
	}

	public void setResultLetterEnglish(byte[] resultLetterEnglish) {
		this.resultLetterEnglish = resultLetterEnglish;
	}

	public byte[] getResultLetterPortuguese() {
		return resultLetterPortuguese;
	}

	public void setResultLetterPortuguese(byte[] resultLetterPortuguese) {
		this.resultLetterPortuguese = resultLetterPortuguese;
	}

	public String getResult(LanguageCode languageCode) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			if (hasText(resultEnglish)) {
				return resultEnglish;
			}
			if (hasText(resultSpanish)) {
				return resultSpanish;
			}
			return hasText(resultPortuguese) ? resultPortuguese : null;
		case PT:
			if (hasText(resultPortuguese)) {
				return resultPortuguese;
			}
			if (hasText(resultSpanish)) {
				return resultSpanish;
			}
			return hasText(resultEnglish) ? resultEnglish : null;
		case SP:
		default:
			if (hasText(resultSpanish)) {
				return resultSpanish;
			}
			if (hasText(resultEnglish)) {
				return resultEnglish;
			}
			return hasText(resultPortuguese) ? resultPortuguese : null;
		}
	}

	public byte[] getResultLetter(LanguageCode languageCode) {
		LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
		switch (resolvedLanguageCode) {
		case EN:
			if (hasBinary(resultLetterEnglish)) {
				return resultLetterEnglish;
			}
			if (hasBinary(resultLetterSpanish)) {
				return resultLetterSpanish;
			}
			return hasBinary(resultLetterPortuguese) ? resultLetterPortuguese : null;
		case PT:
			if (hasBinary(resultLetterPortuguese)) {
				return resultLetterPortuguese;
			}
			if (hasBinary(resultLetterSpanish)) {
				return resultLetterSpanish;
			}
			return hasBinary(resultLetterEnglish) ? resultLetterEnglish : null;
		case SP:
		default:
			if (hasBinary(resultLetterSpanish)) {
				return resultLetterSpanish;
			}
			if (hasBinary(resultLetterEnglish)) {
				return resultLetterEnglish;
			}
			return hasBinary(resultLetterPortuguese) ? resultLetterPortuguese : null;
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean hasBinary(byte[] value) {
		return value != null && value.length > 0;
	}
}
