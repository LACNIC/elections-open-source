package net.lacnic.siselecciones.utils;

import java.util.ArrayList;
import java.util.List;

public class UtilsIdiomas {

	private String charIdiomas = "SP:Español;PT:Português;EN:English";
	private List<Idioma> listaIdiomas;
	private List<String> nombreIdiomas;
	private List<String> idIdiomas;

	public UtilsIdiomas() {
		this.nombreIdiomas = new ArrayList<>();
		this.idIdiomas = new ArrayList<>();
		this.listaIdiomas = new ArrayList<>();

		String[] arrayIdiomas = charIdiomas.split(";");

		for (String var : arrayIdiomas) {
			String[] var2 = var.split(":");
			this.nombreIdiomas.add(var2[1]);
			this.idIdiomas.add(var2[0]);
			Idioma i = new Idioma(var2[0], var2[1]);
			this.listaIdiomas.add(i);
		}
	}

	public List<Idioma> getListalistaIdiomas() {
		return listaIdiomas;
	}

	public List<String> getNombrelistaIdiomas() {
		return nombreIdiomas;
	}

	public List<String> getIdlistaIdiomas() {
		return idIdiomas;
	}

	public Idioma getlistaIdiomas(String idoNombre) {

		for (int j = 0; j < listaIdiomas.size(); j++) {
			Idioma idiomas = listaIdiomas.get(j);

			if (idiomas.getId().compareToIgnoreCase(idoNombre.trim()) == 0 || idiomas.getNombre().compareToIgnoreCase(idoNombre.trim()) == 0) {
				return idiomas;
			}
		}
		return null;
	}

}
