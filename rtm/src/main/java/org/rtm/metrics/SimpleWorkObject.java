package org.rtm.metrics;

public class SimpleWorkObject implements WorkObject{

	private Object payload;
	
	@Override
	public Object getPayload() {
		return payload;
	}

	@Override
	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public String toString(){
		return payload.toString();
	}
}
