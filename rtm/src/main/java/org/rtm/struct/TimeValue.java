package org.rtm.struct;

import java.util.concurrent.ConcurrentHashMap;

/*
 * Wraps a CHM which contains a representation of the data of a given dimension and time bucket (pointed by the Stream's key) 
 */
public class TimeValue extends ConcurrentHashMap<String, Dimension>{

	private static final long serialVersionUID = -2891193441467345217L;
}
