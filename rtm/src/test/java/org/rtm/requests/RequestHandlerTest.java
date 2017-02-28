package org.rtm.requests;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.RequestHandler;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RequestHandlerTest {
	
	@Test
	public void basicTest() throws JsonProcessingException{
		
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime twoWeeksAgo = today.minus(5, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildSimpleSelectorList(), null);
		StreamBroker ssm = new StreamBroker();
		RequestHandler rh = new RequestHandler(ssm);
		
		AbstractResponse response = rh.handle(ar);
		
		long sleepTime = 500;
		System.out.println("Intentional delay for main thread : " + sleepTime +" ms.");
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(response));
		
		// -- NETWORK ROUND TRIP --
		
		StreamId sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamId.class);

		String result = ssm.getStream(sId).toString();
		System.out.println("result=" + result);
		
		Pattern p = Pattern.compile("count=(.+?),");
		Matcher m = p.matcher(result);
		int countTotal = 0;
		while(m.find()){
			String countVal = m.group(1);
			countTotal += Integer.parseInt(countVal);
		}
		
		Pattern pSum = Pattern.compile("sum=(.+?)}");
		Matcher mSum = pSum.matcher(result);
		int sumCount = 0;
		while(mSum.find()){
			String sumVal = mSum.group(1);
			sumCount += Integer.parseInt(sumVal);
		}
		System.out.println("sum=" +sumCount);
		System.out.println("count=" +countTotal);
		Assert.assertEquals(560124, sumCount);
		Assert.assertEquals(100, countTotal);
	}

}
