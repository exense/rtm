package org.rtm.queries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.db.DBClient;
import org.rtm.query.ParallelRangeExecutor;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.ResultHandler;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamResultHandler;
import org.rtm.time.LongTimeInterval;
import org.rtm.utils.DateUtils;

public class ParallelRangeExecutorTest {

	@Test
	public void e2eTestWithMongo() {
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		
		long optimalSize = DBClient.computeOptimalIntervalSize(lti.getSpan(), 20);
		
		ParallelRangeExecutor tpe = new ParallelRangeExecutor("e2etest", lti, optimalSize);
		
		Stream<Long> stream = new Stream<Long>();
		ResultHandler<Long> rh = new StreamResultHandler(stream);
		try {
			tpe.processRangeDoubleLevelBlocking(rh, TestSelectorBuilder.buildSimpleSelectorList(),
					null, 5, 10L);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(stream);
	}
}