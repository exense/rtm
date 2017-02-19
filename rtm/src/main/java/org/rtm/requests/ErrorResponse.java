package org.rtm.requests;

public class ErrorResponse extends AbstractResponse {

	public ErrorResponse(String message){
		super();
		setStatus(ResponseStatus.ERROR);
		setMetaMessage(message);
	}
	
}
