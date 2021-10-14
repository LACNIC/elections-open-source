package net.lacnic.elections.ws.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;


@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
@Produces({MediaType.APPLICATION_JSON, "text/json"})
public class JacksonConfigurationProvider extends ResteasyJackson2Provider {

	public JacksonConfigurationProvider() {
		super();
		JacksonObjectMapper mapper = JacksonObjectMapper.get();
		setMapper(mapper);
	}

}
