package org.rtm.requests;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.RequestHandler;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RequestHandlerTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void basicTest() throws JsonProcessingException{

		LocalDateTime today = LocalDateTime.now();
		LocalDateTime twoWeeksAgo = today.minus(10, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildSimpleSelectorList(), new Properties());

		Integer targetDots = 10;
		ar.getProperties().put("targetChartDots", targetDots.toString());
		StreamBroker ssm = new StreamBroker();
		RequestHandler rh = new RequestHandler(ssm);

		IntStream.rangeClosed(1, 2).forEach(i -> {

			System.out.println("-- iteration " + i + "--");

			long start = System.currentTimeMillis();
			AbstractResponse response = rh.handle(ar);
			Stream stream = ssm.getStream(((StreamId)response.getPayload()));
			
			long waitInterval = 500;

			while(stream.size() < 1){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long firstByte = System.currentTimeMillis();
			System.out.println("TimeToFirstByte=" + (firstByte - start) + " ms.");

			while(stream.size() != targetDots){
				try {
					Thread.sleep(500);
					System.out.println("Size = " + stream.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("Done. Elapse=" + (end - start) + " ms.");
			System.out.println("stream=" + stream);


			//System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(response));

			// -- NETWORK ROUND TRIP --

			StreamId sId = null;
			try {
				sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamId.class);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String result = ssm.getStream(sId).toString();
			//System.out.println("result=" + result);

			Pattern p = Pattern.compile("count=(.+?),");
			Matcher m = p.matcher(result);
			BigInteger countTotal = new BigInteger("0");
			while(m.find()){
				String countVal = m.group(1);
				countTotal = countTotal.add(new BigInteger(countVal));
			}
			System.out.println("count=" +countTotal);
			
			Pattern pSum = Pattern.compile("sum=(.+?)}");
			Matcher mSum = pSum.matcher(result);
			BigInteger sumCount = new BigInteger("0");
			while(mSum.find()){
				String sumVal = mSum.group(1);
				try{
				sumCount = sumCount.add(new BigInteger(sumVal));
				}catch(NumberFormatException e){
					System.err.println("Failed to parse " + sumVal + " in string " + result);
				}
			}
			System.out.println("sum=" +sumCount);

			Assert.assertEquals("133753001249", sumCount.toString());
			Assert.assertEquals("12791151", countTotal.toString());

		});
	}

}
