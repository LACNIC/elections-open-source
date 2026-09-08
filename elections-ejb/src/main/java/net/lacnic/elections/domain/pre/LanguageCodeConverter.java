package net.lacnic.elections.domain.pre;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.lacnic.elections.domain.LanguageCode;

@Converter(autoApply = false)
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, String> {

	@Override
	public String convertToDatabaseColumn(LanguageCode language) {
		return language == null ? null : language.getCode();
	}

	@Override
	public LanguageCode convertToEntityAttribute(String dbValue) {
		return LanguageCode.fromValue(dbValue);
	}
}
