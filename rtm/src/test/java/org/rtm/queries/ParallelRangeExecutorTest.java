package org.rtm.queries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.db.DBClient;
import org.rtm.pipeline.RangeSplittingPipeline;
import org.rtm.pipeline.RangeSplittingPipeline.ExecutionLevel;
import org.rtm.pipeline.RangeSplittingPipeline.ExecutionType;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.Stream;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;
import org.rtm.utils.DateUtils;

public class ParallelRangeExecutorTest {

	@Test
	public void e2eTestWithMongo() {
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		
		long optimalSize = DBClient.computeOptimalIntervalSize(lti.getSpan(), 20);
		
		Stream<Long> stream = new Stream<Long>();
		ResultHandler<Long> rh = new StreamResultHandler(stream);

		RangeSplittingPipeline tpe = new RangeSplittingPipeline("e2etest", lti, optimalSize, 5,
				10L, rh, TestSelectorBuilder.buildSimpleSelectorList(),
				ExecutionLevel.DOUBLE, ExecutionType.NON_BLOCKING,
				null);
		
		try {
			tpe.processRange();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(stream);
	}
}