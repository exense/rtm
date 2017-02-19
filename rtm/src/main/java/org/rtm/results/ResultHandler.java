package org.rtm.results;

import org.rtm.buckets.RangeBucket;
import org.rtm.struct.Dimension;
import org.rtm.struct.Stream;

public class ResultHandler {

	private final Stream stream;
	//private final RangeBucket<Long> bucket;
	
	//public ResultHandler(Stream stream, RangeBucket<Long> bucket){
	public ResultHandler(Stream stream){
		this.stream = stream;
		//this.bucket = bucket;
	}

	public void attachResult(Dimension r) {
		//stream.put
		System.out.println("How do I know to which TimeValue I'm supposed to attach this Dimension?\n"+
							"PB: Buckets not known yet at the time of ResultHandler creation\n"+
							"Sol1: Have the callables create TimeValue's directly instead of Dimensions -> what does that mean for //?\n"+
							"Sol2: Figure out a way to recompute the bucket information from this side\n"+
							"Sol3: attach the bucket info to the Dimension somehow (as metadata or in the payload?)\n\n");
	}

	public Stream getStream() {
		return stream;
	}

	
}
