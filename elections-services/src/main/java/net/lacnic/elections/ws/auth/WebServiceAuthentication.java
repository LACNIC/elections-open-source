package net.lacnic.elections.ws.auth;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.ws.app.AppContext;
import net.ripe.ipresource.IpAddress;
import net.ripe.ipresource.IpResourceSet;

public class WebServiceAuthentication {

	private static final Logger appLogger = LoggerFactory.getLogger("servicesAppLogger");
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String LOG_AUTHENTICATING_PREFIX = "Authenticating WS call from IP ";
	private static final String LOG_AUTH_OK_PREFIX = "Authentication OK for WS call from IP ";
	private static final String LOG_AUTH_FAILED_PREFIX = "Authentication failed for WS call from IP ";
	private static final String LOG_AUTH_METHOD_SUFFIX = ", authMethod=";
	private static final String LOG_MISSING_OR_INVALID_AUTH_TOKEN_SUFFIX = ", missing or invalid Auth Token";
	private static final String LOG_IP_NOT_ALLOWED_SUFFIX = ", IP not allowed";
	private static final String LOG_WRONGLY_CONFIGURED_AUTH_METHOD_SUFFIX = ", auth method is wrongly configured";
	private static final String UNAUTHORIZED_APIKEY_ENTITY = "Unauthorized access, Apikey problem";
	private static final String UNAUTHORIZED_IP_ENTITY = "Unauthorized access, IP problem";
	private static final String INTERNAL_SERVER_ERROR_AUTH_ENTITY = "Internal Server Error during authentication";

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
			String clientAuthHeader = request.getHeader(AUTHORIZATION_HEADER);
			String clientAuthToken = extractBearerToken(clientAuthHeader);
			appLogger.debug(LOG_AUTHENTICATING_PREFIX + clientIp + LOG_AUTH_METHOD_SUFFIX + wsAuthMethod);

			if (Constants.WS_AUTH_TYPE_APP.equals(wsAuthMethod)) {
				// Using Elections app native authentication method:
				// - Header 'Authorization' must match the WS_AUTH_TOKEN property
				// - Client IP must be among the WS_AUTHORIZED_IPS property

				// Validate token
				String appAuthToken = AppContext.getInstance().getMonitorBeanRemote().getWsAuthToken();
				if (clientAuthToken == null || !clientAuthToken.equals(appAuthToken)) {
					appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_MISSING_OR_INVALID_AUTH_TOKEN_SUFFIX);
					return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_APIKEY_ENTITY).build();
				}

				// Authenticated OK, now check the client IP
				IpResourceSet authorizedIPsList = AppContext.getInstance().getMonitorBeanRemote().getWsAuthorizedIps();
				if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
					appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_IP_NOT_ALLOWED_SUFFIX);
					return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_IP_ENTITY).build();
				}
			} else if (wsAuthMethod.equals(Constants.WS_AUTH_TYPE_LACNIC)) {
				// Using LACNIC's authentication framework:
				// - Header 'Authorization' passed on to LACNIC's authentication service
				// - Client IP must be among authorized IPs (returned by service)

				// Create the REST client and configure timeouts
				ClientBuilder clientBuilder = ClientBuilder.newBuilder();
				clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
				clientBuilder.readTimeout(10, TimeUnit.SECONDS);

				try (Client client = clientBuilder.build()) {
					// Set URL, media type, authtoken header and call GET service method
					LacnicAuthResponse response = client.target(AppContext.getInstance().getMonitorBeanRemote().getWsLacnicAuthUrl()).request().header(AUTHORIZATION_HEADER, clientAuthHeader).get(LacnicAuthResponse.class);

					// Check response

					if (response == null || !response.getAuthenticated() || !response.getRoles().contains(Constants.api_elections)) {
						appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_MISSING_OR_INVALID_AUTH_TOKEN_SUFFIX);
						return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_APIKEY_ENTITY).build();
					}

					// Authenticated OK, now check the client IP
					IpResourceSet authorizedIPsList = IpResourceSet.parse(response.getIpAllowed());
					if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
						appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_IP_NOT_ALLOWED_SUFFIX);
						return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_IP_ENTITY).build();
					}
				}
			} else {
				// Auth type not properly configured
				appLogger.error(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_WRONGLY_CONFIGURED_AUTH_METHOD_SUFFIX);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(INTERNAL_SERVER_ERROR_AUTH_ENTITY).build();
			}
			appLogger.debug(LOG_AUTH_OK_PREFIX + clientIp + LOG_AUTH_METHOD_SUFFIX + wsAuthMethod);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(INTERNAL_SERVER_ERROR_AUTH_ENTITY).build();
		}
		return null;
	}

	/**
	 * Authenticates the client according to the configured authentication method
	 *
	 * @param request
	 * @return null if authentication OK, error Response if there's a problem
	 */
	public static Response authenticatePublicInformation(HttpServletRequest request) {
		try {
			String wsAuthMethod = AppContext.getInstance().getMonitorBeanRemote().getWsAuthMethod();
			String clientIp = getRemoteAddr(request);
			String clientAuthHeader = request.getHeader(AUTHORIZATION_HEADER);
			String clientAuthToken = extractBearerToken(clientAuthHeader);
			appLogger.debug(LOG_AUTHENTICATING_PREFIX + clientIp + LOG_AUTH_METHOD_SUFFIX + wsAuthMethod);

			if (Constants.WS_AUTH_TYPE_APP.equals(wsAuthMethod)) {
				// Using Elections app native authentication method:
				// - Header 'Authorization' must match the WS_AUTH_TOKEN property
				// - Client IP must be among the WS_AUTHORIZED_IPS property

				// Validate token
				String appAuthToken = AppContext.getInstance().getMonitorBeanRemote().getWsAuthToken();
				if (clientAuthToken == null || !clientAuthToken.equals(appAuthToken)) {
					appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_MISSING_OR_INVALID_AUTH_TOKEN_SUFFIX);
					return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_APIKEY_ENTITY).build();
				}

				// Authenticated OK, now check the client IP
				IpResourceSet authorizedIPsList = AppContext.getInstance().getMonitorBeanRemote().getWsAuthorizedIps();
				if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
					appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_IP_NOT_ALLOWED_SUFFIX);
					return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_IP_ENTITY).build();
				}
			} else if (wsAuthMethod.equals(Constants.WS_AUTH_TYPE_LACNIC)) {
				// Using LACNIC's authentication framework:
				// - Header 'Authorization' passed on to LACNIC's authentication service
				// - Client IP must be among authorized IPs (returned by service)

				// Create the REST client and configure timeouts
				ClientBuilder clientBuilder = ClientBuilder.newBuilder();
				clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
				clientBuilder.readTimeout(10, TimeUnit.SECONDS);

				try (Client client = clientBuilder.build()) {
					// Set URL, media type, authtoken header and call GET service method
					LacnicAuthResponse response = client.target(AppContext.getInstance().getMonitorBeanRemote().getWsLacnicAuthUrl()).request().header(AUTHORIZATION_HEADER, clientAuthHeader).get(LacnicAuthResponse.class);

					// Check response

					if (response == null || !response.getAuthenticated() || !response.getRoles().contains(Constants.api_electionsPublicInformation)) {
						appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_MISSING_OR_INVALID_AUTH_TOKEN_SUFFIX);
						return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_APIKEY_ENTITY).build();
					}

					// Authenticated OK, now check the client IP
					IpResourceSet authorizedIPsList = IpResourceSet.parse(response.getIpAllowed());
					if (!authorizedIPsList.contains(IpAddress.parse(clientIp))) {
						appLogger.warn(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_IP_NOT_ALLOWED_SUFFIX);
						return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_IP_ENTITY).build();
					}
				}
			} else {
				// Auth type not properly configured
				appLogger.error(LOG_AUTH_FAILED_PREFIX + clientIp + LOG_WRONGLY_CONFIGURED_AUTH_METHOD_SUFFIX);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(INTERNAL_SERVER_ERROR_AUTH_ENTITY).build();
			}
			appLogger.debug(LOG_AUTH_OK_PREFIX + clientIp + LOG_AUTH_METHOD_SUFFIX + wsAuthMethod);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(INTERNAL_SERVER_ERROR_AUTH_ENTITY).build();
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

	private static String extractBearerToken(String authorizationHeader) {
		if (authorizationHeader == null) {
			return null;
		}
		String prefix = "Bearer ";
		if (!authorizationHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
			return null;
		}
		String token = authorizationHeader.substring(prefix.length()).trim();
		return token.isEmpty() ? null : token;
	}

}
