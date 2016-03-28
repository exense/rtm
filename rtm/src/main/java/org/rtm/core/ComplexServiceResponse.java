package org.rtm.core;

import java.util.List;
import java.util.Map;

import org.rtm.commons.Measurement;

public class ComplexServiceResponse {

	public static enum Status{
		NORMAL,
		WARNING
	}
	
	private Map<String,List<Measurement>> payload;
	private Status returnStatus;
	private String message = ";";
	
	public Map<String,List<Measurement>> getPayload() {
		return payload;
	}
	public void setPayload(Map<String,List<Measurement>> payload) {
		this.payload = payload;
	}
	public Status getReturnStatus() {
		return returnStatus;
	}
	public void setReturnStatus(Status returnStatus) {
		this.returnStatus = returnStatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
