package org.rtm.requests;

public class ErrorResponse extends AbstractResponse {

	public ErrorResponse(String message){
		super();
		setType(ResponseType.ERROR);
		setMetaMessage(message);
	}
	
}
