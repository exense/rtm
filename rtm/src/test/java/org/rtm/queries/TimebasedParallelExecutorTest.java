package org.rtm.queries;

import java.util.Date;

import org.junit.Test;
import org.rtm.core.LongTimeInterval;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamResultHandler;

public class TimebasedParallelExecutorTest {

	@Test
	public void e2eTestWithMongo() {
		StreamResultHandler rh = new StreamResultHandler(new Stream<>());
		
		// Simulate real request inputs
		long begin = 0L;
		long end = new Date().getTime();
		long interval = end - begin; // single bucket
		
		TimebasedParallelExecutor tpe = new TimebasedParallelExecutor(new LongTimeInterval(begin, end, 0L), interval);
		
		try {
			tpe.processMongoQueryParallel(
							3, // nbThreads
							60L, // timeoutSecs
							rh,
							TestSelectorBuilder.buildSimpleSelectorList(),
							null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(rh.getStream());
	}
}