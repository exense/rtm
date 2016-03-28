package org.rtm.core;

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
	
}
