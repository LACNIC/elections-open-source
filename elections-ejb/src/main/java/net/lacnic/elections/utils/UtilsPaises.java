package net.lacnic.elections.utils;

import java.util.ArrayList;
import java.util.List;

public class UtilsPaises {

	private String charPais = "AA:Seleccione País;AF:Afghanistan;AL:Albania;DZ:Algeria;AS:American Samoa;AD:Andorra;AO:Angola;AI:Anguilla;AQ:Antarctica;AG:Antigua and Barbuda;AR:Argentina;AM:Armenia;AW:Aruba;AU:Australia;AT:Austria;AZ:Azerbaijan;AP:Azores;BS:Bahamas;BH:Bahrain;BD:Bangladesh;BB:Barbados;BY:Belarus;BE:Belgium;BZ:Belize;BJ:Benin;BM:Bermuda;BT:Bhutan;BO:Bolivia;BA:Bosnia And Herzegowina;XB:Bosnia-Herzegovina;BW:Botswana;BV:Bouvet Island;BR:Brazil;IO:British Indian Ocean Territory;VG:British Virgin Islands;BN:Brunei Darussalam;BG:Bulgaria;BF:Burkina Faso;BI:Burundi;KH:Cambodia;CM:Cameroon;CA:Canada;CV:Cape Verde;KY:Cayman Islands;CF:Central African Republic;TD:Chad;CL:Chile;CN:China;CX:Christmas Island;CC:Cocos (Keeling) Islands;CO:Colombia;KM:Comoros;CG:Congo;CD:Congo, The Democratic Republic O;CK:Cook Islands;XE:Corsica;CR:Costa Rica;CI:Cote d` Ivoire (Ivory Coast);HR:Croatia;CU:Cuba;CY:Cyprus;CZ:Czech Republic;DK:Denmark;DJ:Djibouti;DM:Dominica;DO:Dominican Republic;TP:East Timor;EC:Ecuador;EG:Egypt;SV:El Salvador;GQ:Equatorial Guinea;ER:Eritrea;EE:Estonia;ET:Ethiopia;FK:Falkland Islands (Malvinas);FO:Faroe Islands;FJ:Fiji;FI:Finland;FR:France (Includes Monaco);FX:France, Metropolitan;GF:French Guiana;PF:French Polynesia;TA:French Polynesia (Tahiti);TF:French Southern Territories;GA:Gabon;GM:Gambia;GE:Georgia;DE:Germany;GH:Ghana;GI:Gibraltar;GR:Greece;GL:Greenland;GD:Grenada;GP:Guadeloupe;GU:Guam;GT:Guatemala;GN:Guinea;GW:Guinea-Bissau;GY:Guyana;HT:Haiti;HM:Heard And Mc Donald Islands;VA:Holy See (Vatican City State);HN:Honduras;HK:Hong Kong;HU:Hungary;IS:Iceland;IN:India;ID:Indonesia;IR:Iran;IQ:Iraq;IE:Ireland;EI:Ireland (Eire);IL:Israel;IT:Italy;JM:Jamaica;JP:Japan;JO:Jordan;KZ:Kazakhstan;KE:Kenya;KI:Kiribati;KP:Korea, Democratic People\'S Repub;KW:Kuwait;KG:Kyrgyzstan;LA:Laos;LV:Latvia;LB:Lebanon;LS:Lesotho;LR:Liberia;LY:Libya;LI:Liechtenstein;LT:Lithuania;LU:Luxembourg;MO:Macao;MK:Macedonia;MG:Madagascar;ME:Madeira Islands;MW:Malawi;MY:Malaysia;MV:Maldives;ML:Mali;MT:Malta;MH:Marshall Islands;MQ:Martinique;MR:Mauritania;MU:Mauritius;YT:Mayotte;MX:Mexico;FM:Micronesia, Federated States Of;MD:Moldova, Republic Of;MC:Monaco;MN:Mongolia;MS:Montserrat;MA:Morocco;MZ:Mozambique;MM:Myanmar (Burma);NA:Namibia;NR:Nauru;NP:Nepal;NL:Netherlands;AN:Netherlands Antilles;NC:New Caledonia;NZ:New Zealand;NI:Nicaragua;NE:Niger;NG:Nigeria;NU:Niue;NF:Norfolk Island;MP:Northern Mariana Islands;NO:Norway;OM:Oman;PK:Pakistan;PW:Palau;PS:Palestinian Territory, Occupied;PA:Panama;PG:Papua New Guinea;PY:Paraguay;PE:Peru;PH:Philippines;PN:Pitcairn;PL:Poland;PT:Portugal;PR:Puerto Rico;QA:Qatar;RE:Reunion;RO:Romania;RU:Russian Federation;RW:Rwanda;KN:Saint Kitts And Nevis;SM:San Marino;ST:Sao Tome and Principe;SA:Saudi Arabia;SN:Senegal;XS:Serbia-Montenegro;SC:Seychelles;SL:Sierra Leone;SG:Singapore;SK:Slovak Republic;SI:Slovenia;SB:Solomon Islands;SO:Somalia;ZA:South Africa;GS:South Georgia And The South Sand;KR:South Korea;ES:Spain;LK:Sri Lanka;NV:St. Christopher and Nevis;SH:St. Helena;LC:St. Lucia;PM:St. Pierre and Miquelon;VC:St. Vincent and the Grenadines;SD:Sudan;SR:Suriname;SJ:Svalbard And Jan Mayen Islands;SZ:Swaziland;SE:Sweden;CH:Switzerland;SY:Syrian Arab Republic;TW:Taiwan;TJ:Tajikistan;TZ:Tanzania;TH:Thailand;TG:Togo;TK:Tokelau;TO:Tonga;TT:Trinidad and Tobago;XU:Tristan da Cunha;TN:Tunisia;TR:Turkey;TM:Turkmenistan;TC:Turks and Caicos Islands;TV:Tuvalu;UG:Uganda;UA:Ukraine;AE:United Arab Emirates;UK:United Kingdom;GB:Great Britain;US:United States;UM:United States Minor Outlying Isl;UY:Uruguay;UZ:Uzbekistan;VU:Vanuatu;XV:Vatican City;VE:Venezuela;VN:Vietnam;VI:Virgin Islands (U.S.);WF:Wallis and Furuna Islands;EH:Western Sahara;WS:Western Samoa;YE:Yemen;YU:Yugoslavia;ZR:Zaire;ZM:Zambia;ZW:Zimbabwe;BQ:Bonaire, San Eustaquio y Saba;CW:Curazao;SX:San Martín";
	private static List<Pais> listaPaises;
	private List<String> nombrePaises;
	private List<String> idPaises;

	public UtilsPaises() {
		this.nombrePaises = new ArrayList<>();
		this.idPaises = new ArrayList<>();

		String[] arrayPais = charPais.split(";");

		for (String var : arrayPais) {
			String[] var2 = var.split(":");
			this.nombrePaises.add(var2[1]);
			this.idPaises.add(var2[0]);
		}
	}

	public List<Pais> getListaPaises() {
		return listaPaises;
	}

	public List<String> getNombrePaises() {
		return nombrePaises;
	}

	public List<String> getIdPaises() {
		return idPaises;
	}

	public Pais getPais(String id) {

		for (int j = 0; j < listaPaises.size(); j++) {
			Pais pais = listaPaises.get(j);

			if (pais.getId().compareTo(id.trim()) == 0) {
				return pais;
			}
		}
		return null;
	}

	public static Pais getPaisStatic(String idoNombre) {

		for (int j = 0; j < listaPaises.size(); j++) {
			Pais pais = listaPaises.get(j);

			if (pais.getId().compareToIgnoreCase(idoNombre.trim()) == 0 || pais.getNombre().compareToIgnoreCase(idoNombre.trim()) == 0) {
				return pais;
			}
		}
		return null;
	}

}
