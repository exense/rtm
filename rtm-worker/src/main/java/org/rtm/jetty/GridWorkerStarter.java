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
import java.util.HashMap;

import org.eclipse.jetty.server.Server;
import org.rtm.commons.Configuration;

import step.grid.agent.Agent;
import step.grid.agent.conf.AgentConf;

/**
 * @author dcransac
 *
 */
public class GridWorkerStarter {

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
			new GridWorkerStarter().start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start() throws Exception {

		AgentConf conf = new AgentConf();
		conf.setAgentHost("localhost");
		conf.setAgentPort(8083);
		conf.setGridHost("http://localhost:8082");

		Agent agent = new Agent(conf);
		agent.addTokens(2, new HashMap<>(), new HashMap<>(), new HashMap<>());
		agent.start();

	}

}
