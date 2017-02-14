package org.rtm.commons;

import org.slf4j.Logger;

public class ExceptionHandling {

	public static String buildCustomMessage(Exception e){
		return "Exception thrown: type=" + e.getClass() + "; msg=" + e.getMessage();
	}
	
	public static String processExceptionAndReturnMessage(Logger l, Exception e){
		String message = buildCustomMessage(e);
		l.error(message);
		return message;
	}
	
	public static void processException(Logger l, Exception e){
		l.error(buildCustomMessage(e));
	}
	
}
