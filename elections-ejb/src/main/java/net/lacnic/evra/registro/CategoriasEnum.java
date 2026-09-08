package net.lacnic.evra.registro;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum CategoriasEnum implements Serializable {

	// NUEVAS IPV4
	ND("n/d", false),
	END_USER_A_IPV4("end-user-a-ipv4", true),
	END_USER_B_IPV4("end-user-b-ipv4", true),
	END_USER_C_IPV4("end-user-c-ipv4", true),
	END_USER_D_IPV4("end-user-d-ipv4", true),
	END_USER_E_IPV4("end-user-e-ipv4", true),
	END_USER_F_IPV4("end-user-f-ipv4", true),
	END_USER_G_IPV4("end-user-g-ipv4", true),
	END_USER_H_IPV4("end-user-h-ipv4", true),
	END_USER_I_IPV4("end-user-i-ipv4", true),
	IDP_NANO_IPV4("isp-nano-ipv4", true),
	ISP_MICRO_IPV4("isp-micro-ipv4", true),
	ISP_SMALL_IPV4("isp-small-ipv4", true),
	ISP_MEDIUM_IPV4("isp-medium-ipv4", true),
	ISP_LARGE_IPV4("isp-large-ipv4", true),
	ISP_X_LARGE_IPV4("isp-x-large-ipv4", true),
	ISP_2X_LARGE_A_IPV4("isp-2x-large-a-ipv4", true),
	ISP_2X_LARGE_B_IPV4("isp-2x-large-b-ipv4", true),
	ISP_3X_LARGE_A_IPV4("isp-3x-large-a-ipv4", true),
	ISP_3X_LARGE_B_IPV4("isp-3x-large-b-ipv4", true),
	ISP_4X_LARGE_A_IPV4("isp-4x-large-a-ipv4", true),
	ISP_4X_LARGE_B_IPV4("isp-4x-large-b-ipv4", true),
	ISP_4X_LARGE_C_IPV4("isp-4x-large-c-ipv4", true),
	ISP_4X_LARGE_D_IPV4("isp-4x-large-d-ipv4", true),
	ISP_5X_LARGE_A_IPV4("isp-5x-large-a-ipv4", true),
	ISP_5X_LARGE_B_IPV4("isp-5x-large-b-ipv4", true),
	ISP_5X_LARGE_C_IPV4("isp-5x-large-c-ipv4", true),
	ISP_5X_LARGE_D_IPV4("isp-5x-large-d-ipv4", true),
	ISP_5X_LARGE_E_IPV4("isp-5x-large-e-ipv4", true),
	ISP_5X_LARGE_F_IPV4("isp-5x-large-f-ipv4", true),
	ISP_5X_LARGE_G_IPV4("isp-5x-large-g-ipv4", true),
	ISP_5X_LARGE_H_IPV4("isp-5x-large-h-ipv4", true),
	ISP_6X_LARGE_A_IPV4("isp-6x-large-a-ipv4", true),

	// NUEVAS IPV6
	END_USER_A_IPV6("end-user-a-ipv6", true),
	END_USER_B_IPV6("end-user-b-ipv6", true),
	END_USER_C_IPV6("end-user-c-ipv6", true),
	END_USER_D_IPV6("end-user-d-ipv6", true),
	END_USER_E_IPV6("end-user-e-ipv6", true),
	END_USER_F_IPV6("end-user-f-ipv6", true),
	END_USER_G_IPV6("end-user-g-ipv6", true),
	END_USER_H_IPV6("end-user-h-ipv6", true),
	END_USER_I_IPV6("end-user-i-ipv6", true),
	IDP_NANO_IPV6("isp-nano-ipv6", true),
	ISP_MICRO_IPV6("isp-micro-ipv6", true),
	ISP_SMALL_IPV6("isp-small-ipv6", true),
	ISP_MEDIUM_IPV6("isp-medium-ipv6", true),
	ISP_LARGE_IPV6("isp-large-ipv6", true),
	ISP_X_LARGE_IPV6("isp-x-large-ipv6", true),
	ISP_2X_LARGE_A_IPV6("isp-2x-large-a-ipv6", true),
	ISP_2X_LARGE_B_IPV6("isp-2x-large-b-ipv6", true),
	ISP_3X_LARGE_A_IPV6("isp-3x-large-a-ipv6", true),
	ISP_3X_LARGE_B_IPV6("isp-3x-large-b-ipv6", true),
	ISP_4X_LARGE_A_IPV6("isp-4x-large-a-ipv6", true),
	ISP_4X_LARGE_B_IPV6("isp-4x-large-b-ipv6", true),
	ISP_4X_LARGE_C_IPV6("isp-4x-large-c-ipv6", true),
	ISP_4X_LARGE_D_IPV6("isp-4x-large-d-ipv6", true),
	ISP_5X_LARGE_A_IPV6("isp-5x-large-a-ipv6", true),
	ISP_5X_LARGE_B_IPV6("isp-5x-large-b-ipv6", true),
	ISP_5X_LARGE_C_IPV6("isp-5x-large-c-ipv6", true),
	ISP_5X_LARGE_D_IPV6("isp-5x-large-d-ipv6", true),
	ISP_5X_LARGE_E_IPV6("isp-5x-large-e-ipv6", true),
	ISP_5X_LARGE_F_IPV6("isp-5x-large-f-ipv6", true),
	ISP_5X_LARGE_G_IPV6("isp-5x-large-g-ipv6", true),
	ISP_5X_LARGE_H_IPV6("isp-5x-large-h-ipv6", true),
	ISP_6X_LARGE_A_IPV6("isp-6x-large-a-ipv6", true),

	// GENERALES
	NONE("none", true),
	LEGACY("legacy", true),
	FOUNDING_PARTNER("founding-partner", true),
	ADHERENT_MEMBER("adherent-member", true),
	ASN_ONLY("asn-only", true);

	private final String nombreTabla;
	private final boolean vigente;

	CategoriasEnum(String nombreTabla, boolean vigente) {
		this.nombreTabla = nombreTabla;
		this.vigente = vigente;
	}

	public String getNombreTabla() {
		return nombreTabla;
	}

	public boolean isVigente() {
		return vigente;
	}

	public static List<CategoriasEnum> obtenerVigentes() {
		return Arrays.stream(CategoriasEnum.values()).filter(CategoriasEnum::isVigente).collect(Collectors.toList());
	}

	public static CategoriasEnum fromValue(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return null;
		}
		for (CategoriasEnum categoria : values()) {
			if (categoria.name().equalsIgnoreCase(normalized) || categoria.getNombreTabla().equalsIgnoreCase(value.trim())) {
				return categoria;
			}
		}
		return null;
	}
}
