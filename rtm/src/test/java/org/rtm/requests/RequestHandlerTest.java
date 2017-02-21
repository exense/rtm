package org.rtm.requests;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.StreamedSessionId;
import org.rtm.stream.StreamedSessionManager;
import org.rtm.time.LongTimeInterval;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RequestHandlerTest {
	
	@Test
	public void basicTest() throws JsonProcessingException{
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		
		AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildSimpleSelectorList(), null);
		StreamedSessionManager ssm = new StreamedSessionManager();
		RequestHandler rh = new RequestHandler(ssm);
		
		AbstractResponse response = rh.handle(ar);
		
		System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(response));
		
		// -- NETWORK ROUND TRIP --
		
		StreamedSessionId sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamedSessionId.class);
		
		System.out.println(sId.getStreamedSessionId() + " : " +ssm.getStream(sId));
	}

}
