package org.rtm.requests;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.Test;
import org.rtm.core.DateTimeInterval;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.StreamedSessionManager;
import org.rtm.utils.DateUtils;

public class RequestHandlerTest {
	
	@Test
	public void basicTest(){
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(2, ChronoUnit.WEEKS);
		DateTimeInterval dti = new DateTimeInterval(DateUtils.asDate(twoWeeksAgo), DateUtils.asDate(today));
		
		AggregationRequest ar = new AggregationRequest(dti, TestSelectorBuilder.buildSimpleSelectorList(), null);
		RequestHandler rh = new RequestHandler(new StreamedSessionManager());
		
		AbstractResponse response = rh.handle(ar);
		System.out.println(response);
	}

}
