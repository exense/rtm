package org.rtm.requests;

import org.json.JSONObject;

public class AbstractResponse {
	
	private ResponseStatus status;
	private String metaMessage;
	private JSONObject payload;
	
	public enum ResponseStatus{
		SUCCESS("SUCCESS"), WARNING("WARNING"), ERROR("ERROR");
		
		String name;
		ResponseStatus(String s) {
			name = s;
		}
		String getName() {
			return name;
		} 
		
	}

	public String getMetaMessage() {
		return metaMessage;
	}

	public void setMetaMessage(String metaMessage) {
		this.metaMessage = metaMessage;
	}

	public JSONObject getPayload() {
		return payload;
	}

	public void setPayload(JSONObject payload) {
		this.payload = payload;
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public void setStatus(ResponseStatus status) {
		this.status = status;
	}

	public String toString(){
		return "status=" + this.getStatus() + "; meta=" + this.getMetaMessage()+ "; payload=" + this.getPayload();
	}
	
}
