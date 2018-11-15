package org.rtm.request;

public class ErrorResponse extends AbstractResponse {

	public ErrorResponse(String message){
		super();
		setStatus(ResponseStatus.ERROR);
		setMetaMessage(message);
	}
	
}
