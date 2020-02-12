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
import java.io.IOException;

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
import org.rtm.commons.Configuration;
import org.rtm.rest.aggregation.AggregationServlet;
import org.rtm.rest.conf.ConfigurationServlet;
import org.rtm.rest.ingestion.IngestionServlet;
import org.rtm.rest.measurement.MeasurementServlet;
import org.rtm.rest.security.AuthenticationFilter;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.exense.commons.app.ArgumentParser;
import ch.exense.viz.persistence.accessors.GenericVizAccessor;
import ch.exense.viz.rest.VizServlet;
import step.grid.GridImpl;

/**
 * @author dcransac
 *
 */
public class JettyFrontendStarter {
	
	private Server server;	
	
	public static void main(String[] args) throws IOException{
		
		AggregationServlet.partitionerGrid = new GridImpl(8015);
		AggregationServlet.workerGrid = new GridImpl(8016);

		try {
			System.out.println("[MAIN] Starting partitioner grid..");
			AggregationServlet.partitionerGrid.start();
			System.out.println("[MAIN] Starting worker grid..");
			AggregationServlet.workerGrid.start();
			System.out.println("[MAIN] Grids initialized.");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ArgumentParser arguments = new ArgumentParser(args);
		
		String agentConfStr = arguments.getOption("config");
		
		if(agentConfStr == null){
			Exception e = new Exception("Missing -config option. Please set path to config file.");
			e.printStackTrace();
			System.exit(0);
		}
		
		Configuration.initSingleton(new File(agentConfStr));
		
		try {
			new JettyFrontendStarter().start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start() throws Exception {
		
		System.out.println("--- Starting Frontend Server ---");
		
		//handlers = new ContextHandlerCollection();
		int rtmPort = Integer.parseInt(Configuration.getInstance().getProperty("rtm.port"));
		server = new Server(rtmPort);
		
		ContextHandlerCollection hcoll = new ContextHandlerCollection();
		
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(AggregationServlet.class.getPackage().getName());
		resourceConfig.register(JacksonJaxbJsonProvider.class);
		resourceConfig.registerClasses(AggregationServlet.class);
		resourceConfig.registerClasses(MeasurementServlet.class);
		resourceConfig.registerClasses(ConfigurationServlet.class);
		resourceConfig.registerClasses(IngestionServlet.class);
		
		resourceConfig.registerClasses(AuthenticationFilter.class);
		
		resourceConfig.registerClasses(VizServlet.class);
		ch.exense.commons.app.Configuration config = convertConfig(Configuration.getInstance());
		GenericVizAccessor accessor = new GenericVizAccessor(new ch.exense.viz.persistence.mongodb.MongoClientSession(config));
		resourceConfig.register(new AbstractBinder() {	
			@Override
			protected void configure() {
				bind(accessor).to(GenericVizAccessor.class);
			}
		});

		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);
		sh.setInitParameter("cacheControl","max-age=0,public"); 

		ServletContextHandler serviceHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		serviceHandler.setContextPath("/rtm/rest");
		serviceHandler.addServlet(sh, "/*");
		serviceHandler.setInitParameter("cacheControl","max-age=0,public"); 

		ContextHandler webAppHandler = new ContextHandler("/rtm");
		ResourceHandler bb = new ResourceHandler();
		bb.setResourceBase(Resource.newClassPathResource("webapp").getURI().toString());
		webAppHandler.setHandler(bb);
		
		hcoll.addHandler(serviceHandler);
		hcoll.addHandler(webAppHandler);
		
		server.setHandler(hcoll);
		server.start();
		server.join();
		
	}
	
	private ch.exense.commons.app.Configuration convertConfig(Configuration instance) throws Exception {
		ch.exense.commons.app.Configuration config = new ch.exense.commons.app.Configuration();
		config.putProperty("db.host", Configuration.getInstance().getProperty("ds.host"));
		config.putProperty("ds.port", Configuration.getInstance().getProperty("ds.port"));
		config.putProperty("db.dbname", Configuration.getInstance().getProperty("ds.dbname"));
		return config;
	}

}
