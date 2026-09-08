package net.lacnic.elections.adminweb.validators;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.evra.registro.CategoriasEnum;

public final class OrganizationValidationRules {

	private static final Pattern ORG_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{1,254}$");
	private static final Pattern CNPJ_ALLOWED_PATTERN = Pattern.compile("^[0-9./-]+$");
	private static final Pattern ASN_PATTERN = Pattern.compile("^(?i)(AS)?\\d{1,10}$");
	private static final Pattern CONTACT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{2,255}$");
	private static final Pattern CONTACT_NAME_PATTERN = Pattern.compile("^[\\p{L}0-9][\\p{L}0-9 .,'-]{1,999}$");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
	private static final Set<String> COUNTRY_CODES = buildCountryCodes();

	private OrganizationValidationRules() {
	}

	public static String normalizeOrgId(String orgId) {
		if (orgId == null) {
			return "";
		}
		return orgId.trim().toUpperCase(Locale.ROOT);
	}

	public static boolean isValidOrgId(String orgId) {
		return orgId != null && ORG_ID_PATTERN.matcher(orgId.trim()).matches();
	}

	public static boolean isValidVotes(Integer votes) {
		return votes != null && votes >= 1 && votes <= 11;
	}

	public static boolean isValidCnpj(String cnpj) {
		if (cnpj == null || cnpj.trim().isEmpty()) {
			return true;
		}
		String value = cnpj.trim();
		if (!CNPJ_ALLOWED_PATTERN.matcher(value).matches()) {
			return false;
		}
		String digits = value.replaceAll("\\D", "");
		return isValidCnpjDigits(digits);
	}

	public static boolean isValidAsn(String asn) {
		return asn == null || asn.trim().isEmpty() || ASN_PATTERN.matcher(asn.trim()).matches();
	}

	public static boolean isValidMembershipContactId(String membershipContactId) {
		return membershipContactId != null && CONTACT_ID_PATTERN.matcher(membershipContactId.trim()).matches();
	}

	public static boolean isValidMembershipContactName(String membershipContactName) {
		return membershipContactName != null && CONTACT_NAME_PATTERN.matcher(membershipContactName.trim()).matches();
	}

	public static boolean isValidMembershipContactEmail(String membershipContactEmail) {
		return membershipContactEmail != null && EMAIL_PATTERN.matcher(membershipContactEmail.trim()).matches();
	}

	public static boolean isValidCountry(String countryCode) {
		if (countryCode == null || countryCode.trim().isEmpty()) {
			return false;
		}
		return COUNTRY_CODES.contains(countryCode.trim().toUpperCase(Locale.ROOT));
	}

	public static boolean isValidCategory(String category) {
		return CategoriasEnum.fromValue(category) != null;
	}

	public static boolean isValidMembershipLanguage(String language) {
		return LanguageCode.fromValue(language) != null;
	}

	private static Set<String> buildCountryCodes() {
		Set<String> result = new HashSet<>();
		for (String countryCode : new CountryUtils().getIdsListExcludingDefault()) {
			if (countryCode != null && !countryCode.trim().isEmpty()) {
				result.add(countryCode.trim().toUpperCase(Locale.ROOT));
			}
		}
		return result;
	}

	private static boolean isValidCnpjDigits(String digits) {
		if (digits == null || !digits.matches("\\d{14}")) {
			return false;
		}
		if (digits.chars().distinct().count() == 1) {
			return false;
		}

		int d1 = calculateCnpjDigit(digits.substring(0, 12), new int[] { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
		int d2 = calculateCnpjDigit(digits.substring(0, 12) + d1, new int[] { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 });
		return digits.equals(digits.substring(0, 12) + d1 + d2);
	}

	private static int calculateCnpjDigit(String value, int[] weights) {
		int sum = 0;
		for (int i = 0; i < weights.length; i++) {
			sum += Character.getNumericValue(value.charAt(i)) * weights[i];
		}
		int mod = sum % 11;
		return mod < 2 ? 0 : 11 - mod;
	}
}
