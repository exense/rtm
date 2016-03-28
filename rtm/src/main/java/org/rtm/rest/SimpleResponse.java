package org.rtm.rest;

public class SimpleResponse {
	
	public static enum STATUS{
		FAILED,
		SUCCESS
	}
	
	private STATUS status;
	private String message;
	public STATUS getStatus() {
		return status;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toString(){
		return "[Response]status="+status+";message="+message;
	}
}
