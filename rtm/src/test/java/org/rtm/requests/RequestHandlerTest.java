package org.rtm.requests;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.core.DateTimeInterval;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.StreamedSessionId;
import org.rtm.stream.StreamedSessionManager;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RequestHandlerTest {
	
	@Test
	public void basicTest() throws JsonProcessingException{
		
		LocalDate today = LocalDate.now();
		LocalDate twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		DateTimeInterval dti = new DateTimeInterval(DateUtils.asDate(twoWeeksAgo), DateUtils.asDate(today));
		
		AggregationRequest ar = new AggregationRequest(dti, TestSelectorBuilder.buildSimpleSelectorList(), null);
		StreamedSessionManager ssm = new StreamedSessionManager();
		RequestHandler rh = new RequestHandler(ssm);
		
		AbstractResponse response = rh.handle(ar);
		
		System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(response));
		
		// -- NETWORK ROUND TRIP --
		
		StreamedSessionId sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamedSessionId.class);
		
		System.out.println(sId.getStreamedSessionId() + " : " +ssm.getStream(sId));
	}

}
