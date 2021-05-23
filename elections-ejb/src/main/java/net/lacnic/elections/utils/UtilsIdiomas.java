package net.lacnic.elections.utils;

import java.util.ArrayList;
import java.util.List;

public class UtilsIdiomas {

	private String charIdiomas = "SP:Español;PT:Português;EN:English";
	private List<Language> listaIdiomas;
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
			Language i = new Language(var2[0], var2[1]);
			this.listaIdiomas.add(i);
		}
	}

	public List<Language> getListalistaIdiomas() {
		return listaIdiomas;
	}

	public List<String> getNombrelistaIdiomas() {
		return nombreIdiomas;
	}

	public List<String> getIdlistaIdiomas() {
		return idIdiomas;
	}

	public Language getlistaIdiomas(String idoNombre) {

		for (int j = 0; j < listaIdiomas.size(); j++) {
			Language idiomas = listaIdiomas.get(j);

			if (idiomas.getId().compareToIgnoreCase(idoNombre.trim()) == 0 || idiomas.getName().compareToIgnoreCase(idoNombre.trim()) == 0) {
				return idiomas;
			}
		}
		return null;
	}

}
