package net.lacnic.elections.ws.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import net.lacnic.elections.ws.json.JacksonConfigurationProvider;
import net.lacnic.elections.ws.services.ElectionsService;
import net.lacnic.elections.ws.services.ElectionsTablesServices;

@ApplicationPath("/")
public class ElectionsServicesApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new ElectionsService());
		singletons.add(new ElectionsTablesServices());
		singletons.add(new JacksonConfigurationProvider());

		return singletons;
	}

}
