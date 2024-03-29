package net.lacnic.elections.ws.auth;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.ws.app.AppContext;
import net.ripe.ipresource.IpAddress;
import net.ripe.ipresource.IpResourceSet;

public class WebServiceAuthentication {

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	/**
	 * Authenticates the client according to the configured authentication method
	 * 
	 * @param request
	 * @return null if authentication OK, error Response if there's a problem
	 */
	public static Response authenticate(HttpServletRequest request) {
		try {
			String wsAuthMethod = AppContext.getInstance().getMonitorBeanRemote().getWsAuthMethod();
			String clientIp = getRemoteAddr(request);
			String clientAuthToken = request.getHeader("Authorization");
			appLogger.info("Authenticating WS call from IP " + clientIp + ", authMethod=" + wsAuthMethod);

			if (wsAuthMethod.equals(Constants.WS_AUTH_TYPE_APP)) {
				// Using Elections app native authentication method:
				// - Header 'Authorization' must match 'WS_AUTH_TOKEN' system parameter and
				// - Client IP must be among authorized IPs (system parameter)

				// Validate token
				String appAuthToken = AppContext.getInstance().getMonitorBeanRemote().getWsAuthToken();
				if (clientAuthToken == null || !clientAuthToken.equals(appAuthToken)) {
					appLogger.warn("Authentication failed for WS call from IP " + clientIp + ", missing or invalid Auth Token");
					return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access, Apikey problem").build();
				}

				// Authenticated OK, now check the client IP
				IpResourceSet authorizedIPsList = AppContext.getInstance().getMonitorBeanRemote().getWsAuthorizedIps();
				if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
					appLogger.warn("Authentication failed for WS call from IP " + clientIp + ", IP not allowed");
					return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access, IP problem").build();
				}
			} else if (wsAuthMethod.equals(Constants.WS_AUTH_TYPE_LACNIC)) {
				// Using LACNIC's authentication framework:
				// - Header 'Authorization' passed on to LACNIC's authentication service
				// - Client IP must be among authorized IPs (returned by service)

				// Create the REST client and configure timeouts
				ClientBuilder clientBuilder = ClientBuilder.newBuilder();
				clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
				clientBuilder.readTimeout(10, TimeUnit.SECONDS);
				Client client = clientBuilder.build();

				// Set URL, media type, authtoken header and call GET service method
				LacnicAuthResponse response = client.target(AppContext.getInstance().getMonitorBeanRemote().getWsLacnicAuthUrl()).request().header("Authorization", clientAuthToken).get(LacnicAuthResponse.class);

				// Check response

				if (response == null || !response.getAuthenticated() || !response.getRoles().contains(Constants.api_elections)) {
					appLogger.warn("Authentication failed for WS call from IP " + clientIp + ", missing or invalid Auth Token");
					return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access, Apikey problem").build();
				}

				// Authenticated OK, now check the client IP
				IpResourceSet authorizedIPsList = IpResourceSet.parse(response.getIpAllowed());
				if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
					appLogger.warn("Authentication failed for WS call from IP " + clientIp + ", IP not allowed");
					return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access, IP problem").build();
				}
			} else {
				// Auth type not properly configured
				appLogger.error("Authentication failed for WS call from IP " + clientIp + ", auth method is wrongly configured");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error during authentication").build();
			}
			appLogger.info("Authentication OK for WS call from IP " + clientIp + ", authMethod=" + wsAuthMethod);
		} catch (Exception e) {
			appLogger.error(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error during authentication").build();
		}
		return null;
	}

	/**
	 * Gets the remote client's IP
	 * 
	 * @param request
	 * @return the client IP
	 */
	public static String getRemoteAddr(final HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}

}
