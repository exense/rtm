package org.rtm.requests;

public class AbstractResponse {
	
	private ResponseType t;
	private String metaMessage;
	
	public enum ResponseType{
		SUCCESS, WARNING, ERROR
	}
	
	AbstractResponse(){}

	public void setType(ResponseType t){
		this.t = t;
	}

	public String getMetaMessage() {
		return metaMessage;
	}

	public void setMetaMessage(String metaMessage) {
		this.metaMessage = metaMessage;
	}
}
