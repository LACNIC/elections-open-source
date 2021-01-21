package net.lacnic.ws.elecciones.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import net.lacnic.ws.elecciones.EleccionesService;

public class EleccionesServicesApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new EleccionesService());

		return singletons;
	}

}
