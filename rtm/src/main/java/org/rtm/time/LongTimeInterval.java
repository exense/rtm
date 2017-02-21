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
package org.rtm.time;

public class LongTimeInterval {

	private Long begin;
	private Long end;

	public LongTimeInterval(Long pBegin, long duration) {
		this.begin = pBegin;
		this.end = new Long(pBegin + duration);
	}

	public LongTimeInterval(Long pBegin, Long pEnd, long notAmbiguous) {
		this.begin = pBegin;
		this.end = pEnd;
	}

	public Long getBegin() {
		return begin;
	}
	public void setBegin(Long begin) {
		this.begin = begin;
	}
	public Long getEnd() {
		return end;
	}
	public void setEnd(Long end) {
		this.end = end;
	}	

	public Long getSpan() {
		return end - begin;
	}

	public boolean belongs(Long d){
		return ((d >= this.begin) && (d < this.end));
	}

	public LongTimeInterval getNext(long granularity){
		Long nextBegin = this.begin + granularity;
		return new LongTimeInterval(nextBegin, granularity);
	}

	public String toString(){return "BEG="+this.begin+"|END="+this.end;}
}
