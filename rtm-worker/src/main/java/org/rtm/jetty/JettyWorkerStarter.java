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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rtm.commons.Configuration;
import org.rtm.rest.worker.WorkerServlet;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * @author dcransac
 *
 */
public class JettyWorkerStarter {

	private Server server;
	//private ContextHandlerCollection handlers;


	public static void main(String[] args){
		ArgumentParser arguments = new ArgumentParser(args);

		String agentConfStr = arguments.getOption("config");

		if(agentConfStr == null){
			Exception e = new Exception("Missing -config option. Please set path to config file.");
			e.printStackTrace();
			System.exit(0);
		}

		Configuration.initSingleton(new File(agentConfStr));

		try {
			new JettyWorkerStarter().start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start() throws Exception {

		System.out.println("--- Starting Worker Server ---");
		
		//handlers = new ContextHandlerCollection();
		int rtmPort = Integer.parseInt(Configuration.getInstance().getProperty("rtm.port"));
		server = new Server(rtmPort);

		ContextHandlerCollection hcoll = new ContextHandlerCollection();

		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(WorkerServlet.class.getPackage().getName());
		resourceConfig.register(JacksonJaxbJsonProvider.class);
		resourceConfig.registerClasses(WorkerServlet.class);
		
		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);
		sh.setInitParameter("cacheControl","max-age=0,public");

		ServletContextHandler serviceHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		serviceHandler.setContextPath("/rtm/rest");
		serviceHandler.addServlet(sh, "/*");
		serviceHandler.setInitParameter("cacheControl","max-age=0,public");

		hcoll.addHandler(serviceHandler);

		server.setHandler(hcoll);
		server.start();
		server.join();

	}

}
