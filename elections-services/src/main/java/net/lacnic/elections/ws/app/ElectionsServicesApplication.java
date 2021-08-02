package net.lacnic.elections.ws.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import net.lacnic.elections.ws.services.ElectionsService;

@ApplicationPath("/")
public class ElectionsServicesApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new ElectionsService());

		return singletons;
	}

}
