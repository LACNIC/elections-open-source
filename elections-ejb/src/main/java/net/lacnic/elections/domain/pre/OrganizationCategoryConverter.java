package net.lacnic.elections.domain.pre;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.lacnic.evra.registro.CategoriasEnum;

@Converter(autoApply = false)
public class OrganizationCategoryConverter implements AttributeConverter<CategoriasEnum, String> {

	@Override
	public String convertToDatabaseColumn(CategoriasEnum category) {
		return category == null ? null : category.name();
	}

	@Override
	public CategoriasEnum convertToEntityAttribute(String dbValue) {
		return CategoriasEnum.fromValue(dbValue);
	}
}
