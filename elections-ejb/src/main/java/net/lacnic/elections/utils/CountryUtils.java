package net.lacnic.elections.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CountryUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String COUNTRIES_CHAR = "AA:Seleccione País;AF:Afghanistan;AL:Albania;DZ:Algeria;AS:American Samoa;AD:Andorra;AO:Angola;AI:Anguilla;AQ:Antarctica;AG:Antigua and Barbuda;AR:Argentina;AM:Armenia;AW:Aruba;AU:Australia;AT:Austria;AZ:Azerbaijan;AP:Azores;BS:Bahamas;BH:Bahrain;BD:Bangladesh;BB:Barbados;BY:Belarus;BE:Belgium;BZ:Belize;BJ:Benin;BM:Bermuda;BT:Bhutan;BO:Bolivia;BA:Bosnia And Herzegowina;XB:Bosnia-Herzegovina;BW:Botswana;BV:Bouvet Island;BR:Brazil;IO:British Indian Ocean Territory;VG:British Virgin Islands;BN:Brunei Darussalam;BG:Bulgaria;BF:Burkina Faso;BI:Burundi;KH:Cambodia;CM:Cameroon;CA:Canada;CV:Cape Verde;KY:Cayman Islands;CF:Central African Republic;TD:Chad;CL:Chile;CN:China;CX:Christmas Island;CC:Cocos (Keeling) Islands;CO:Colombia;KM:Comoros;CG:Congo;CD:Congo, The Democratic Republic O;CK:Cook Islands;XE:Corsica;CR:Costa Rica;CI:Cote d` Ivoire (Ivory Coast);HR:Croatia;CU:Cuba;CY:Cyprus;CZ:Czech Republic;DK:Denmark;DJ:Djibouti;DM:Dominica;DO:Dominican Republic;TP:East Timor;EC:Ecuador;EG:Egypt;SV:El Salvador;GQ:Equatorial Guinea;ER:Eritrea;EE:Estonia;ET:Ethiopia;FK:Falkland Islands (Malvinas);FO:Faroe Islands;FJ:Fiji;FI:Finland;FR:France (Includes Monaco);FX:France, Metropolitan;GF:French Guiana;PF:French Polynesia;TA:French Polynesia (Tahiti);TF:French Southern Territories;GA:Gabon;GM:Gambia;GE:Georgia;DE:Germany;GH:Ghana;GI:Gibraltar;GR:Greece;GL:Greenland;GD:Grenada;GP:Guadeloupe;GU:Guam;GT:Guatemala;GN:Guinea;GW:Guinea-Bissau;GY:Guyana;HT:Haiti;HM:Heard And Mc Donald Islands;VA:Holy See (Vatican City State);HN:Honduras;HK:Hong Kong;HU:Hungary;IS:Iceland;IN:India;ID:Indonesia;IR:Iran;IQ:Iraq;IE:Ireland;EI:Ireland (Eire);IL:Israel;IT:Italy;JM:Jamaica;JP:Japan;JO:Jordan;KZ:Kazakhstan;KE:Kenya;KI:Kiribati;KP:Korea, Democratic People\'S Repub;KW:Kuwait;KG:Kyrgyzstan;LA:Laos;LV:Latvia;LB:Lebanon;LS:Lesotho;LR:Liberia;LY:Libya;LI:Liechtenstein;LT:Lithuania;LU:Luxembourg;MO:Macao;MK:Macedonia;MG:Madagascar;ME:Madeira Islands;MW:Malawi;MY:Malaysia;MV:Maldives;ML:Mali;MT:Malta;MH:Marshall Islands;MQ:Martinique;MR:Mauritania;MU:Mauritius;YT:Mayotte;MX:Mexico;FM:Micronesia, Federated States Of;MD:Moldova, Republic Of;MC:Monaco;MN:Mongolia;MS:Montserrat;MA:Morocco;MZ:Mozambique;MM:Myanmar (Burma);NA:Namibia;NR:Nauru;NP:Nepal;NL:Netherlands;AN:Netherlands Antilles;NC:New Caledonia;NZ:New Zealand;NI:Nicaragua;NE:Niger;NG:Nigeria;NU:Niue;NF:Norfolk Island;MP:Northern Mariana Islands;NO:Norway;OM:Oman;PK:Pakistan;PW:Palau;PS:Palestinian Territory, Occupied;PA:Panama;PG:Papua New Guinea;PY:Paraguay;PE:Peru;PH:Philippines;PN:Pitcairn;PL:Poland;PT:Portugal;PR:Puerto Rico;QA:Qatar;RE:Reunion;RO:Romania;RU:Russian Federation;RW:Rwanda;KN:Saint Kitts And Nevis;SM:San Marino;ST:Sao Tome and Principe;SA:Saudi Arabia;SN:Senegal;XS:Serbia-Montenegro;SC:Seychelles;SL:Sierra Leone;SG:Singapore;SK:Slovak Republic;SI:Slovenia;SB:Solomon Islands;SO:Somalia;ZA:South Africa;GS:South Georgia And The South Sand;KR:South Korea;ES:Spain;LK:Sri Lanka;NV:St. Christopher and Nevis;SH:St. Helena;LC:St. Lucia;PM:St. Pierre and Miquelon;VC:St. Vincent and the Grenadines;SD:Sudan;SR:Suriname;SJ:Svalbard And Jan Mayen Islands;SZ:Swaziland;SE:Sweden;CH:Switzerland;SY:Syrian Arab Republic;TW:Taiwan;TJ:Tajikistan;TZ:Tanzania;TH:Thailand;TG:Togo;TK:Tokelau;TO:Tonga;TT:Trinidad and Tobago;XU:Tristan da Cunha;TN:Tunisia;TR:Turkey;TM:Turkmenistan;TC:Turks and Caicos Islands;TV:Tuvalu;UG:Uganda;UA:Ukraine;AE:United Arab Emirates;UK:United Kingdom;GB:Great Britain;US:United States;UM:United States Minor Outlying Isl;UY:Uruguay;UZ:Uzbekistan;VU:Vanuatu;XV:Vatican City;VE:Venezuela;VN:Vietnam;VI:Virgin Islands (U.S.);WF:Wallis and Furuna Islands;EH:Western Sahara;WS:Western Samoa;YE:Yemen;YU:Yugoslavia;ZR:Zaire;ZM:Zambia;ZW:Zimbabwe;BQ:Bonaire, San Eustaquio, Saba;CW:Curazao;SX:San Martín;AX:Aland Islands;BL:Saint Barthelemy;GG:Guernsey;IM:Isle of Man;JE:Jersey;MF:Saint Martin (French Part);RS:Serbia;SS:South Sudan;TL:Timor-Leste";
	private static final String DEFAULT_COUNTRY_ID = "AA";
	private static final Locale LOCALE_ENGLISH = Locale.ENGLISH;
	private static final Locale LOCALE_SPANISH = new Locale("es");
	private static final Locale LOCALE_PORTUGUESE = new Locale("pt");
	private static final Set<String> ISO_COUNTRY_CODES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Locale.getISOCountries())));
	private static final Map<String, Map<String, String>> CUSTOM_LOCALIZED_COUNTRY_NAMES = buildCustomLocalizedCountryNames();
	private static final Map<String, String> COUNTRY_CODE_ALIASES_FOR_LOCALE_LOOKUP = buildCountryCodeAliasesForLocaleLookup();
	private static final List<String> LACNIC_COVERAGE_IDS = Collections.unmodifiableList(Arrays.asList(
			"AR", "AW", "BZ", "BO", "BQ", "BR", "CL", "CO", "CR", "CU", "CW", "EC", "SV", "GT", "GY", "GF",
			"HT", "HN", "FK", "MX", "NI", "PA", "PY", "PE", "DO", "SX", "GS", "SR", "TT", "UY", "VE"));
	private static final Set<String> LACNIC_COVERAGE_IDS_SET = new LinkedHashSet<>(LACNIC_COVERAGE_IDS);
	private static final List<String> namesList = new ArrayList<>();
	private static final List<String> idsList = new ArrayList<>();
	private static final Map<String, String> nameById = new HashMap<>();
	private static final Map<String, String> idToNameMapExcludingDefault = new HashMap<>();

	static {
		String[] countriesArray = COUNTRIES_CHAR.split(";");

		for (String country : countriesArray) {
			String[] countrySplit = country.split(":", 2);
			if (countrySplit.length < 2) {
				continue;
			}
			String id = countrySplit[0].trim();
			String name = countrySplit[1].trim();
			if (id.isEmpty() || name.isEmpty()) {
				continue;
			}
			namesList.add(name);
			idsList.add(id);
			nameById.put(id, name);
			if (!DEFAULT_COUNTRY_ID.equals(id)) {
				idToNameMapExcludingDefault.put(id, name);
			}
		}
	}

	public CountryUtils() {
		// Default public constructor
	}

	public List<String> getNamesList() {
		return new ArrayList<>(namesList);
	}

	public List<String> getIdsList() {
		return new ArrayList<>(idsList);
	}

	public List<String> getIdsListExcludingDefault() {
		List<String> ids = new ArrayList<>();
		for (String id : idsList) {
			if (!DEFAULT_COUNTRY_ID.equals(id)) {
				ids.add(id);
			}
		}
		return ids;
	}

	public List<String> getIdsListLacnicFirst(boolean includePlaceholder) {
		List<String> source = includePlaceholder ? getIdsList() : getIdsListExcludingDefault();
		return orderCountryIdsLacnicFirst(source, includePlaceholder);
	}

	public List<String> getLacnicCoverageIds() {
		List<String> result = new ArrayList<>();
		for (String countryId : LACNIC_COVERAGE_IDS) {
			if (idToNameMapExcludingDefault.containsKey(countryId)) {
				result.add(countryId);
			}
		}
		return result;
	}

	public boolean isLacnicCoverageCountryCode(String countryCode) {
		String normalizedCode = normalizeCountryCode(countryCode);
		return normalizedCode != null && LACNIC_COVERAGE_IDS_SET.contains(normalizedCode);
	}

	public String normalizeCountryCode(String countryCode) {
		if (countryCode == null) {
			return null;
		}
		String trimmed = countryCode.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.toUpperCase(Locale.ROOT);
	}

	public String getNameById(String id) {
		String normalizedCode = normalizeCountryCode(id);
		return normalizedCode == null ? null : nameById.get(normalizedCode);
	}

	public String getNameById(String id, Locale locale) {
		String normalizedCode = normalizeCountryCode(id);
		if (normalizedCode == null) {
			return null;
		}
		if (!nameById.containsKey(normalizedCode)) {
			return null;
		}
		if (DEFAULT_COUNTRY_ID.equals(normalizedCode)) {
			return resolveDefaultCountryLabel(locale);
		}

		String localizedCountryName = resolveLocalizedCountryName(normalizedCode, locale);
		if (localizedCountryName != null) {
			return localizedCountryName;
		}
		return nameById.get(normalizedCode);
	}

	public Map<String, String> getIdToNameMapExcludingDefault() {
		return new HashMap<>(idToNameMapExcludingDefault);
	}

	public Map<String, String> getIdToNameMapExcludingDefault(Locale locale) {
		Map<String, String> localizedMap = new LinkedHashMap<>();
		for (String countryId : idsList) {
			if (DEFAULT_COUNTRY_ID.equals(countryId)) {
				continue;
			}
			String countryName = getNameById(countryId, locale);
			if (countryName != null && !countryName.trim().isEmpty()) {
				localizedMap.put(countryId, countryName.trim());
			}
		}
		return localizedMap;
	}

	public String getDisplayLabel(String countryCode, Locale locale, boolean appendCodeForNonLacnicCountries) {
		String normalizedCode = normalizeCountryCode(countryCode);
		if (normalizedCode == null) {
			return "";
		}

		String localizedName = getNameById(normalizedCode, locale);
		String baseLabel = localizedName == null || localizedName.trim().isEmpty() ? normalizedCode : localizedName.trim();
		if (!appendCodeForNonLacnicCountries || DEFAULT_COUNTRY_ID.equals(normalizedCode)) {
			return baseLabel;
		}
		String normalizedLabel = baseLabel.toUpperCase(Locale.ROOT);
		String expectedSuffix = " (" + normalizedCode + ")";
		if (normalizedLabel.endsWith(expectedSuffix)) {
			return baseLabel;
		}
		return normalizedCode.equalsIgnoreCase(baseLabel) ? normalizedCode : baseLabel + " (" + normalizedCode + ")";
	}

	private static Map<String, String> buildCountryCodeAliasesForLocaleLookup() {
		Map<String, String> aliases = new HashMap<>();
		aliases.put("UK", "GB");
		aliases.put("EI", "IE");
		aliases.put("TP", "TL");
		aliases.put("XV", "VA");
		aliases.put("NV", "KN");
		aliases.put("XB", "BA");
		aliases.put("XS", "RS");
		aliases.put("YU", "RS");
		aliases.put("ZR", "CD");
		return Collections.unmodifiableMap(aliases);
	}

	private static Map<String, Map<String, String>> buildCustomLocalizedCountryNames() {
		Map<String, Map<String, String>> customNames = new HashMap<>();

		Map<String, String> caribbeanNetherlandsNames = new HashMap<>();
		caribbeanNetherlandsNames.put("en", "Bonaire / Sint Eustatius / Saba (BQ)");
		caribbeanNetherlandsNames.put("es", "Bonaire / San Eustacio / Saba (BQ)");
		caribbeanNetherlandsNames.put("pt", "Bonaire / Santo Eustáquio / Saba (BQ)");
		customNames.put("BQ", Collections.unmodifiableMap(caribbeanNetherlandsNames));

		return Collections.unmodifiableMap(customNames);
	}

	private String resolveLocalizedCountryName(String countryCode, Locale locale) {
		Locale targetLocale = resolveSupportedLocale(locale);
		String language = targetLocale.getLanguage();

		Map<String, String> customNamesByLanguage = CUSTOM_LOCALIZED_COUNTRY_NAMES.get(countryCode);
		if (customNamesByLanguage != null) {
			String customLocalizedName = customNamesByLanguage.get(language);
			if (customLocalizedName != null && !customLocalizedName.trim().isEmpty()) {
				return customLocalizedName.trim();
			}
		}

		String displayCode = COUNTRY_CODE_ALIASES_FOR_LOCALE_LOOKUP.getOrDefault(countryCode, countryCode);
		if (!ISO_COUNTRY_CODES.contains(displayCode)) {
			return null;
		}
		String localizedName = new Locale("", displayCode).getDisplayCountry(targetLocale);
		if (localizedName == null) {
			return null;
		}

		String normalizedLocalizedName = localizedName.trim();
		if (normalizedLocalizedName.isEmpty() || displayCode.equalsIgnoreCase(normalizedLocalizedName)) {
			return null;
		}
		return normalizedLocalizedName;
	}

	private Locale resolveSupportedLocale(Locale locale) {
		if (locale == null) {
			return LOCALE_ENGLISH;
		}
		String language = locale.getLanguage();
		if ("es".equalsIgnoreCase(language)) {
			return LOCALE_SPANISH;
		}
		if ("pt".equalsIgnoreCase(language)) {
			return LOCALE_PORTUGUESE;
		}
		return LOCALE_ENGLISH;
	}

	private String resolveDefaultCountryLabel(Locale locale) {
		Locale targetLocale = resolveSupportedLocale(locale);
		if (LOCALE_SPANISH.getLanguage().equals(targetLocale.getLanguage())) {
			return "Seleccione país";
		}
		if (LOCALE_PORTUGUESE.getLanguage().equals(targetLocale.getLanguage())) {
			return "Selecione o país";
		}
		return "Select country";
	}

	private List<String> orderCountryIdsLacnicFirst(List<String> sourceCountryIds, boolean includePlaceholder) {
		LinkedHashSet<String> orderedInputIds = new LinkedHashSet<>();
		boolean hasPlaceholder = false;
		for (String countryId : sourceCountryIds) {
			String normalizedCountryId = normalizeCountryCode(countryId);
			if (normalizedCountryId == null) {
				continue;
			}
			if (DEFAULT_COUNTRY_ID.equals(normalizedCountryId)) {
				hasPlaceholder = true;
				continue;
			}
			orderedInputIds.add(normalizedCountryId);
		}

		List<String> orderedOutput = new ArrayList<>();
		if (includePlaceholder && hasPlaceholder) {
			orderedOutput.add(DEFAULT_COUNTRY_ID);
		}

		for (String lacnicCountryId : LACNIC_COVERAGE_IDS) {
			if (orderedInputIds.remove(lacnicCountryId)) {
				orderedOutput.add(lacnicCountryId);
			}
		}

		orderedOutput.addAll(orderedInputIds);
		return orderedOutput;
	}

}
