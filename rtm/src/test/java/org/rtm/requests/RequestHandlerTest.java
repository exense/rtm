package org.rtm.requests;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.RequestHandler;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.StreamId;
import org.rtm.stream.StreamBroker;
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
		StreamBroker ssm = new StreamBroker();
		RequestHandler rh = new RequestHandler(ssm);
		
		AbstractResponse response = rh.handle(ar);
		
		System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(response));
		
		// -- NETWORK ROUND TRIP --
		
		StreamId sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamId.class);
		
		System.out.println(sId.getStreamedSessionId() + " : " +ssm.getStream(sId));
	}

}
