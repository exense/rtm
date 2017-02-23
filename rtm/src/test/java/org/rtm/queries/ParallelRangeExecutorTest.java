package org.rtm.queries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.query.ParallelRangeExecutor;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.Stream;
import org.rtm.time.LongTimeInterval;
import org.rtm.utils.DateUtils;

public class ParallelRangeExecutorTest {

	@Test
	public void e2eTestWithMongo() {
		ParallelRangeExecutor tpe = new ParallelRangeExecutor();
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		
		Stream<Long> stream = null;
		try {
			stream = tpe.getResponseStream(TestSelectorBuilder.buildSimpleSelectorList(), lti, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(stream);
	}
}