/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.jetty;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import ch.exense.commons.app.Configuration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rtm.commons.RtmContext;
import org.rtm.rest.aggregation.AggregationServlet;
import org.rtm.rest.conf.ConfigurationServlet;
import org.rtm.rest.ingestion.IngestionServlet;
import org.rtm.rest.measurement.MeasurementServlet;
import org.rtm.rest.security.AuthenticationFilter;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * @author dcransac
 *
 */
public class JettyStarter {
	
	private Server server;
	private RtmContext context;

	public JettyStarter(Configuration configuration) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		context = new RtmContext(configuration);
	}
	//private ContextHandlerCollection handlers;


	public RtmContext getContext() {
		return context;
	}

	public static void main(String[] args){
		ArgumentParser arguments = new ArgumentParser(args);
		
		String agentConfStr = arguments.getOption("config");
		
		if(agentConfStr == null){
			Exception e = new Exception("Missing -config option. Please set path to config file.");
			e.printStackTrace();
			System.exit(0);
		}
		
		
		
		try {
			Configuration configuration = new Configuration(new File(agentConfStr));
			new JettyStarter(configuration).start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start() throws Exception {
		
		//handlers = new ContextHandlerCollection();
		int rtmPort = context.getConfiguration().getPropertyAsInteger("rtm.port");
		server = new Server(rtmPort);

		
		
		ContextHandlerCollection hcoll = new ContextHandlerCollection();

		ServletContextHandler serviceHandler = getServletContextHandler();

		ContextHandler webAppHandler = new ContextHandler("/rtm");
		ResourceHandler bb = new ResourceHandler();
		bb.setResourceBase(Resource.newClassPathResource("webappRtm").getURI().toString());
		webAppHandler.setHandler(bb);
		
		hcoll.addHandler(serviceHandler);
		hcoll.addHandler(webAppHandler);
		
		server.setHandler(hcoll);
		server.start();
		server.join();
		
	}

	public ServletContextHandler getServletContextHandler() {
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(AggregationServlet.class.getPackage().getName());
		resourceConfig.register(JacksonJaxbJsonProvider.class);
		resourceConfig.registerClasses(AggregationServlet.class);
		resourceConfig.registerClasses(MeasurementServlet.class);
		resourceConfig.registerClasses(ConfigurationServlet.class);
		resourceConfig.registerClasses(IngestionServlet.class);

		resourceConfig.registerClasses(AuthenticationFilter.class);

		resourceConfig.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(context).to(RtmContext.class);
			}
		});

		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);
		sh.setInitParameter("cacheControl","max-age=0,public");

		ServletContextHandler serviceHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		serviceHandler.setContextPath("/rtm/rest");
		serviceHandler.addServlet(sh, "/*");
		serviceHandler.setInitParameter("cacheControl","max-age=0,public");
		return serviceHandler;
	}

}
