package org.rtm.mongo.split;

import org.bson.Document;
import org.junit.Test;
import org.rtm.backend.queries.TimebasedParallelExecutor;
import org.rtm.backend.results.ResultHandler;
import org.rtm.core.LongTimeInterval;

public class TimebasedParallelExecutorTest {

	@Test
	public void e2eTestWithMongo(){
		try {
			new TimebasedParallelExecutor(new LongTimeInterval(14000L, 1000L), 99L).processParallel(3, 60L, new ResultHandler(), new Document(), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}