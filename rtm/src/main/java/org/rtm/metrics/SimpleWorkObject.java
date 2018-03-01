package org.rtm.metrics;

public class SimpleWorkObject implements WorkObject{

	private Object payload;
	
	@Override
	public Object getPayload(String key) {
		return payload;
	}

	@Override
	public void setPayload(String key, Object payload) {
		this.payload = payload;
	}

	public String toString(){
		return payload.toString();
	}
}
