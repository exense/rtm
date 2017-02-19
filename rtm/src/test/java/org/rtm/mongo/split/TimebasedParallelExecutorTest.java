package org.rtm.mongo.split;

import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.junit.Test;
import org.rtm.backend.queries.TimebasedParallelExecutor;
import org.rtm.core.LongTimeInterval;

public class TimebasedParallelExecutorTest {

	@Test
	public void basicTest(){
		try {
			new TimebasedParallelExecutor(new LongTimeInterval(14000L, 1000L), 99L, new Document()).processParallel(3, 1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
