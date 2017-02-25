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

import java.util.Date;

public class DateTimeInterval {
	
	private Date begin;
	private Date end;
	
	public DateTimeInterval(Date pBegin, Date pEnd){
		this.begin = pBegin;
		this.end = pEnd;
	}
	
	public DateTimeInterval(Date pBegin, long pGranularity) {
		this.begin = pBegin;
		this.end = new Date(pBegin.getTime() + pGranularity);
	}

	public Date getBegin() {
		return begin;
	}
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}	

	public boolean belongs(Date d){
		return ((d.after(this.begin) || d.equals(this.begin)) && d.before(this.end));
	}
	
	public boolean belongs(long timestamp){
		return ((timestamp >= this.begin.getTime()) && (timestamp < this.end.getTime()));
	}
	
	public DateTimeInterval getNext(long granularity){
		Date nextBegin = new Date(this.begin.getTime() + granularity);
		Date nextEnd = new Date(this.end.getTime() + granularity);
		
		return new DateTimeInterval(nextBegin, nextEnd);
	}
	
	public String toString(){return "BEG="+this.begin+"|END="+this.end;}
	
	public LongTimeInterval toLongTime(){
		return new LongTimeInterval(this.begin.getTime(), this.end.getTime());
	}
}
