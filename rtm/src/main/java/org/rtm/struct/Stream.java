package org.rtm.struct;

import java.util.concurrent.ConcurrentSkipListMap;

/*
 * Wraps a CSLM which contains an ordered representation of the entire timeseries data of the stream 
 */
public class Stream extends ConcurrentSkipListMap<Long, TimeValue>{

	private static final long serialVersionUID = 8431902330094695624L;
}
