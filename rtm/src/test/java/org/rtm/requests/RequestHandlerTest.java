package org.rtm.requests;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

import ch.exense.commons.app.Configuration;
import step.core.collections.mongodb.MongoDBCollectionFactory;
import step.core.collections.Document;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.stream.FinalDimension;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.rtm.stream.result.FinalAggregationResult;
import org.rtm.utils.DateUtils;
import org.rtm.utils.JSONMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

//TODO: mongo mock
public class RequestHandlerTest {

	public static void main(String[] args) throws Exception{

		List<Long> timeToFirstByte = new ArrayList<Long>();
		List<Long> elapse = new ArrayList<Long>();
		List<Long> elapseWithResults = new ArrayList<Long>();

		int iterations=1;
		//int[] partitions = {2,4,8,16,32};
		int[] partitions = {4};
		//int[] histSize = {10000,1000,100};
		int[] histSize = {5000};
		//int[] histApp = {1,5,50};
		int[] histApp = {1};
		for (int p=0; p < partitions.length;p++) {

			for (int h = 0; h < histApp.length; h++) {
				for (int i = 0; i < iterations; i++) {
					RequestHandlerTest requestHandlerTest = new RequestHandlerTest();
					requestHandlerTest.basicTest(histApp[h], histSize[h], partitions[p], timeToFirstByte, elapse, elapseWithResults);
				}
				System.out.println("High level results---- for " + partitions[p] + " partitions" + ", histSize: " + histSize[h] + ", histApp: " + histApp[h]);
				long count = 0;
				long sum = 0;
				for (long v : timeToFirstByte) {
					count++;
					sum += v;
				}
				System.out.println("timeToFirstByte avg: " + sum / count);
				count = 0;
				sum = 0;
				for (long v : elapse) {
					count++;
					sum += v;
				}
				System.out.println("elapse stream avg: " + sum / count);
				count = 0;
				sum = 0;
				for (long v : elapseWithResults) {
					count++;
					sum += v;
				}
				System.out.println("elapse total avg: " + sum / count);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	//@Test
	public void basicTest(int histApp, int histSize, int partition, List<Long> timeToFirstByte, List<Long> elapse, List<Long> elapseWithResults) throws Exception{
		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));

		Configuration configuration = new Configuration(new File("src/main/resources/rtm.properties"));
		MeasurementAccessor ma = new MeasurementAccessor(new MongoDBCollectionFactory(configuration.getUnderlyingPropertyObject()).getCollection(MeasurementAccessor.ENTITY_NAME, Document.class));
		
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime twoWeeksAgo = today.minus(10, ChronoUnit.WEEKS);
		LongTimeInterval lti = new LongTimeInterval(DateUtils.asDate(twoWeeksAgo).getTime(), DateUtils.asDate(today).getTime());
		//AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildTestSelectorList("5ea289e3ccdd9212862cd1dd"), props);
		//AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildTestSelectorList("5ea958fab91e616fa53bbb68"), props);
		AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildTestSelectorList("5f77329117305718f0c172f2"), props);
		//AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildTestSelectorListWithName("5f77329117305718f0c172f2","Custom_login"), props);

		//AggregationRequest ar = new AggregationRequest(lti, TestSelectorBuilder.buildSimpleSelectorList(), props);

		ar.getServiceParams().put("aggregateService.granularity", "auto");
		ar.getServiceParams().put("aggregateService.timeout", "600");
		ar.getServiceParams().put("aggregateService.partition", String.valueOf(partition));
		ar.getServiceParams().put("aggregateService.cpu", "4");
		ar.getServiceParams().put("aggregateService.timeField", "begin");
		ar.getServiceParams().put("aggregateService.timeFormat", "long");
		ar.getServiceParams().put("aggregateService.valueField", "value");
		ar.getServiceParams().put("aggregateService.groupby", "name");
		ar.getServiceParams().put("aggregateService.histSize",String.valueOf(histSize));
		ar.getServiceParams().put("aggregateService.histApp",String.valueOf(histApp));

		//ar.getServiceParams().put("targetChartDots", "1");

		StreamBroker ssm = new StreamBroker(configuration);
		RequestHandler rh = new RequestHandler(ssm, configuration, ma);

		IntStream.rangeClosed(1, 1).forEach(it -> {

			System.out.println("-- iteration " + it + "--");

			long start = System.currentTimeMillis();
			AbstractResponse response = null;
			try {
				response = new SuccessResponse(rh.aggregate(ar), "Stream initialized. Call the streaming service next to start retrieving data.");
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
			System.out.println("TRACE - TimeToFirstByte=" + (firstByte - start) + " ms.");
			timeToFirstByte.add(firstByte - start);

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
			System.out.println("TRACE - Done. Elapse=" + (end - start) + " ms.");
			elapse.add(end - start);
			//System.out.println("stream=" + stream);

			Properties fknProps = stream.getStreamProp();
			fknProps.putAll(props);
			Stream result = null;
			//print results to CSV
			String[] metricKeys = {"cnt","avg","min","max","50th pcl","tps","tpm","80th pcl","90th pcl","99th pcl"};

			try {
				result = new MetricsManager(fknProps, configuration).handle(stream);
				FinalAggregationResult f = (FinalAggregationResult) result.getStreamData().firstEntry().getValue();
				System.out.println("cnt,avg,min,max,50th pcl,tps,tpm,80th pcl,90th pcl,99th pcl");
				result.getStreamData().forEach((k,v) -> {
					FinalAggregationResult fResult = (FinalAggregationResult) v;
					Set<String> series = fResult.getDimensionsMap().keySet();
					for (String serie: series) {
						FinalDimension metrics = (FinalDimension) fResult.getDimensionsMap().get(serie);
						StringBuffer buf = new StringBuffer();
						buf.append(serie).append(",");
						for ( int i=0; i < metricKeys.length; i++) {
							buf.append(metrics.get(metricKeys[i]));
							buf.append(",");
						}
						System.out.println(buf.toString());
					}
					//Map dimensionsMap = fResult.getDimensionsMap();
					/*fResult.getDimensionsMap().forEach((mk,mv) -> {
						System.out.println(mk + "," + mv.toString().replace("{", "").replace("}", ""));

					});*/
				});
				long resultTs = System.currentTimeMillis();
				System.out.println("TRACE - Results computed. Elapse=" + (resultTs - start) + " ms.");
				elapseWithResults.add(resultTs - start);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}



			
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
	

	/*@SuppressWarnings("rawtypes")
	//@Test
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
		RequestHandler rh = new RequestHandler(ssm);

		IntStream.rangeClosed(1, 2).forEach(i -> {

			System.out.println("-- iteration " + i + "--");

			long start = System.currentTimeMillis();
			AbstractResponse response = null;
			try {
				response = new SuccessResponse(rh.compare(cr), "Stream initialized. Call the streaming service next to start retrieving data.");
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
	
	//@Test
	public void benchmarkTest() throws Exception {
		for (int i=0;i<1;i++) {
			basicTest(200,100,8, timeToFirstByte, elapse, elapseWithResults);
		}
		long count=0;
		long sum=0;
		for (long v :timeToFirstByte){
			count++; 
			sum+=v;
		}
		System.out.println("timeToFirstByte avg: " + sum/count);
		count=0;
		sum=0;
		for (long v :elapse){
			count++; 
			sum+=v;
		}
		System.out.println("elapse stream avg: " + sum/count);
		count=0;
		sum=0;
		for (long v :elapseWithResults){
			count++;
			sum+=v;
		}
		System.out.println("elapse total avg: " + sum/count);



	}*/

}

