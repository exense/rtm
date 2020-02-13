package org.rtm.request;

import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.junit.Test;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ComparisonRequest;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.selection.TestSelectorBuilder;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RequestHandlerTest {

	@SuppressWarnings("rawtypes")
	//@Test
	public void basicTest() throws Exception{

		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));
		
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime twoWeeksAgo = today.minus(10, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildSimpleSelectorList(), props);
		
		ar.getServiceParams().put("aggregateService.granularity", "10000");
		ar.getServiceParams().put("aggregateService.timeout", "600");
		ar.getServiceParams().put("aggregateService.partition", "8");
		ar.getServiceParams().put("aggregateService.cpu", "4");
		//ar.getServiceParams().put("targetOChartDots", "1");

		//TODO: re-enable tests
		//RequestHandler rh = new RequestHandler();
		RequestHandler rh = null;

		IntStream.rangeClosed(1, 1).forEach(i -> {

			System.out.println("-- iteration " + i + "--");

			long start = System.currentTimeMillis();
			AbstractResponse response = null;
			try {
				response = new SuccessResponse(rh.aggregate(ar,null,null), "Stream initialized. Call the streaming service next to start retrieving data.");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			Stream stream = new Stream<>(props);
			//TODO
			// ssm.getStream(((StreamId)response.getPayload()));

			long waitInterval = 500;

			while(stream.getStreamData().size() < 1){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long firstByte = System.currentTimeMillis();
			System.out.println("TimeToFirstByte=" + (firstByte - start) + " ms.");

			while(!stream.isComplete()){
				try {
					Thread.sleep(500);
					System.out.println("Size = " + stream.getStreamData().size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("Done. Elapse=" + (end - start) + " ms.");
			//System.out.println("stream=" + stream);

			Properties fknProps = stream.getStreamProp();
			fknProps.putAll(props);
			Stream result = null;
			try {
				result = new MetricsManager(fknProps).handle(stream);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			System.out.println("result=" + result);

			
			try {
				System.out.println("Sending streamHandle to client: " + new JSONMapper().convertToJsonString(result));
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// -- NETWORK ROUND TRIP --

			StreamId sId = null;
			try {
				sId = new JSONMapper().convertObjectToType(response.getPayload(), StreamId.class);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		});
	}
	

	@SuppressWarnings("rawtypes")
	@Test
	public void compareTest() throws JsonProcessingException{

		LocalDateTime today = LocalDateTime.now();
		LocalDateTime twoWeeksAgo = today.minus(10, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());

		ComparisonRequest cr = new ComparisonRequest(lti, lti, TestSelectorBuilder.buildSimpleSelectorList(), TestSelectorBuilder.buildSimpleSelectorList(), new Properties());
		
		cr.getServiceParams().put("aggregateService.granularity", "10000");
		cr.getServiceParams().put("aggregateService.timeout", "600000");
		cr.getServiceParams().put("aggregateService.partition", "8");
		cr.getServiceParams().put("aggregateService.cpu", "4");
		//ar.getServiceParams().put("targetChartDots", "1");

		StreamBroker ssm = new StreamBroker();
		RequestHandler rh = null;

		IntStream.rangeClosed(1, 2).forEach(i -> {

			System.out.println("-- iteration " + i + "--");

			long start = System.currentTimeMillis();
			AbstractResponse response = null;
			try {
				//response = new SuccessResponse(rh.compare(cr), "Stream initialized. Call the streaming service next to start retrieving data.");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Stream stream = ssm.getStream(((StreamId)response.getPayload()));
			
			long waitInterval = 500;

			while(stream.getStreamData().size() < 1){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long firstByte = System.currentTimeMillis();
			System.out.println("TimeToFirstByte=" + (firstByte - start) + " ms.");

			while(!stream.isComplete()){
				try {
					Thread.sleep(500);
					System.out.println("Size = " + stream.getStreamData().size());
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

			//Assert.assertEquals("133753001249", sumCount.toString());
			//Assert.assertEquals("12791151", countTotal.toString());

		});
	}

}
