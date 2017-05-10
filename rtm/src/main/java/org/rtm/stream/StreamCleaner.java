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
package org.rtm.stream;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */

@SuppressWarnings("rawtypes")
public class StreamCleaner implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(StreamCleaner.class);
	
	private StreamBroker sb;
	private long sleepTime;
	private long defaultTimeToEviction;
	
	public StreamCleaner(StreamBroker sb, long streamEvictionSeconds, long sleepTimeSeconds){
		this.sb = sb;
		this.sleepTime = sleepTimeSeconds * 1000;
		this.defaultTimeToEviction = streamEvictionSeconds * 1000;
	}

	@Override
	public void run() {
		for(;;){
			collectGarbage();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void collectGarbage() {
		
		Map<String, Stream> registry = this.sb.getStreamRegistry();
		
		if(this.sb == null || this.sb.getStreamRegistry() == null)
			return;
		
		registry.entrySet().stream().forEach(e -> {
			Stream s = e.getValue();
			String id = e.getKey();
			
			if(s.isRefreshedSinceCompletion() || isEvictionTimeReached(s)){
				Stream thisStream = registry.get(id);
				thisStream.closeStream();
				registry.remove(id);
				logger.info("Evicted stream " + id + " - Duration: "+s.getDurationMs()+" ms. Reason: consumed="+s.isRefreshedSinceCompletion() + ", evictionTimeReached=" + isEvictionTimeReached(s));
			}
		});
	}

	private boolean isEvictionTimeReached(Stream s) {
		Long customTimeout = s.getTimeoutDurationSecs();
		long elapsed = System.currentTimeMillis() - s.getTimeCreated();
		if(customTimeout != null && customTimeout > 0)
			return elapsed > (customTimeout * 1000);
		else
			return elapsed > this.defaultTimeToEviction;
	}
	
}
