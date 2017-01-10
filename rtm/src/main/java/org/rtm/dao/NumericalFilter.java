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
package org.rtm.dao;

public class NumericalFilter {

	private String key;
	private Long minValue;
	private Long maxValue;

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Long getMinValue() {
		return minValue;
	}
	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}
	public boolean hasMinValue() {
		if(minValue == null)
			return false;
		return true;
	}
	public Long getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}
	public boolean hasMaxValue() {
		if(maxValue == null)
			return false;
		return true;
	}
	public String toString(){
		return "["+this.getKey()+":"+this.getMaxValue()+"," + this.getMinValue()+"]";
	}
}
