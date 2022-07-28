package org.rtm.rest.security;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.rtm.rest.AbstractServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Provider
@Priority(Priorities.AUTHENTICATION)
@Tag(name = "RTM Authentication")
@Hidden
public class AuthenticationFilter extends AbstractServlet implements ContainerRequestFilter, ClientResponseFilter {
	
	private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
	UnsecureAbstractClient client;
	boolean useSSO;
	
	@PostConstruct
	public void init() throws Exception {
		client = new UnsecureAbstractClient(context.getConfiguration().getProperty("sso.server.url", "http://localhost:8080"));
		useSSO = Boolean.parseBoolean(context.getConfiguration().getProperty("sso.active", "false"));
	}

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
							context.getConfiguration().getProperty(
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