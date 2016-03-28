package org.rtm.core;

public class LongTimeInterval {
	
	private Long begin;
	private Long end;
	
	public LongTimeInterval(Long pBegin, long pGranularity) {
		this.begin = pBegin;
		this.end = new Long(pBegin + pGranularity);
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

	public boolean belongs(Long d){
		return ((d >= this.begin) && (d < this.end));
	}
	
	public LongTimeInterval getNext(long granularity){
		Long nextBegin = this.begin + granularity;
		return new LongTimeInterval(nextBegin, granularity);
	}
	
	public String toString(){return "BEG="+this.begin+"|END="+this.end;}
}
