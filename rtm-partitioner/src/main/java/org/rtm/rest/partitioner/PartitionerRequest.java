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
package org.rtm.rest.partitioner;

import java.util.List;
import java.util.Properties;

import org.rtm.selection.Selector;

/**
 * @author doriancransac
 *
 */
public class PartitionerRequest {

	private List<Selector> sel;
	private Properties prop;
	private long subPartitioning;
	private int subPoolSize;
	private int timeoutSecs;
	private long start;
	private long end;
	private long increment;
	private long optimalSize;
	
	public List<Selector> getSel() {
		return sel;
	}
	public void setSel(List<Selector> sel) {
		this.sel = sel;
	}
	public Properties getProp() {
		return prop;
	}
	public void setProp(Properties prop) {
		this.prop = prop;
	}
	public long getSubPartitioning() {
		return subPartitioning;
	}
	public void setSubPartitioning(long subPartitioning) {
		this.subPartitioning = subPartitioning;
	}
	public int getSubPoolSize() {
		return subPoolSize;
	}
	public void setSubPoolSize(int subPoolSize) {
		this.subPoolSize = subPoolSize;
	}
	public int getTimeoutSecs() {
		return timeoutSecs;
	}
	public void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public long getIncrement() {
		return increment;
	}
	public void setIncrement(long increment) {
		this.increment = increment;
	}
	public long getOptimalSize() {
		return optimalSize;
	}
	public void setOptimalSize(long optimalSize) {
		this.optimalSize = optimalSize;
	}
}
