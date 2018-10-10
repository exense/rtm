package org.rtm.rest.security;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.rtm.commons.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, ClientResponseFilter {
	
	private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
	UnsecureAbstractClient client = new UnsecureAbstractClient(Configuration.getInstance().getPropertyWithDefault("sso.server.url", "http://localhost:8080"));
	boolean useSSO = Boolean.parseBoolean(Configuration.getInstance().getPropertyWithDefault("sso.active", "false"));
	
	@Inject
	private ExtendedUriInfo extendendUriInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(useSSO) {
			Cookie sessionCookie = requestContext.getCookies().get("sessionid");
			if(sessionCookie!=null) {
				String token = sessionCookie.getValue();
				try {
					validateToken(token);
					
				} catch (TokenValidationException e) {
					//only a warning, due to refresh calls in clients after expiration -> TODO: stop refresh after X attempts on client side?
					logger.warn("Incorrect session token or the token could not be validated.", e);
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				} catch (Exception e) {
					logger.error("An exception was thrown while checking user rights.", e);
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				}
			} else {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}
	}

	private void validateToken(String token) throws TokenValidationException {
		if(!client.requestBuilder(
							Configuration.getInstance()
							.getPropertyWithDefault(
								"step.server.tokenContext",
								"/rest/access/checkToken?token=")
							+token)
					.get(Boolean.class)){
			throw new TokenValidationException();
		}
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		
	}
}