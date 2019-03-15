package org.rtm.request;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.rtm.commons.Configuration;
import org.rtm.db.DBClient;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.rest.partitioner.PartitionerRequest;
import org.rtm.selection.Selector;
import org.rtm.stream.StreamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import step.grid.GridImpl;
import step.grid.TokenWrapper;
import step.grid.client.GridClient;
import step.grid.io.AgentError;
import step.grid.io.OutputMessage;

public class RequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public StreamId aggregate(AggregationRequest aggReq, GridClient client, TokenWrapper handle) throws Exception{
		
		logger.info(" --- New Aggregation Request ---"); 
		List<Selector> sel = aggReq.getSelectors1();
		LongTimeInterval lti = aggReq.getTimeWindow1();

		// load props from conf first
		Properties prop = Configuration.getInstance().getUnderlyingPropertyObject();
		//override with props from request
		prop.putAll(aggReq.getServiceParams());

		/* Parallization inputs*/
		int timeoutSecs = Integer.parseInt(prop.getProperty("aggregateService.timeout"));
		int subPoolSize = Integer.parseInt(prop.getProperty("aggregateService.cpu"));
		int subPartitioning = Integer.parseInt(prop.getProperty("aggregateService.partition"));
		//TODO: Short-circuit user input: simply make sure each thread is fed enough
		//int subPartitioning = subPoolSize * 2; //(?)
		
		logger.info("Perf info: timeoutSecs=" + timeoutSecs + "; subPartitioning="+subPartitioning+"; subPoolSize=" + subPoolSize);

		boolean useHeuristic = prop.getProperty("useHistHeuristic") != null? Boolean.parseBoolean(prop.getProperty("useHistHeuristic")) : true;
		if(useHeuristic)
		{
			int heuristicSampleSize = prop.getProperty("heuristicSampleSize") != null? Integer.parseInt(prop.getProperty("heuristicSampleSize")) : 1000;
			float errorMarginPercentage = prop.getProperty("errorMarginPercentage") != null? Float.parseFloat(prop.getProperty("errorMarginPercentage")) : 0.01F;
			long effective90pcl = DBClient.run90PclOnFirstSample(heuristicSampleSize, sel);
			int optimalHistApp = (int)Math.round(effective90pcl * errorMarginPercentage);
			//We're using the heuristic here, so we're ignoring user inputs for Hist App. (client side fields should actually be removed) 
			prop.put("aggregateService.histApp", Integer.toString(optimalHistApp));
			
			logger.info("Using histogram heuristic with values: histSize="+prop.getProperty("aggregateService.histApp") + "; heuristicSampleSize="+heuristicSampleSize+"; errorMarginPercentage="+errorMarginPercentage+"; effective90pcl="+effective90pcl+"; optimalHistApp=" + optimalHistApp+";");
		}

		LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
		Long optimalSize = getEffectiveIntervalSize(prop.getProperty("aggregateService.granularity"), effective);

		//TODO: compute target number of subbuckets and raise warning or circuit-break if > 1M?

		logger.info("Request info: TimeWindow=[effective=" + effective + "; optimalSize=" + optimalSize + "]; props=" + prop + "; selectors=" + aggReq.getSelectors1() + ";");

		/* SHIP TO PARTITIONER */
		// Add map of <streamId,partitionerId> to know where to forward the stream refresh calls

		PartitionerRequest req = new PartitionerRequest();
		req.setSubPoolSize(subPoolSize);
		req.setSubPartitioning(subPartitioning);
		req.setSel(sel);
		req.setOptimalSize(optimalSize);
		req.setIncrement(optimalSize);
		req.setStart(effective.getBegin());
		req.setEnd(effective.getEnd());
		req.setProp(prop);
		req.setTimeoutSecs(timeoutSecs);
		ObjectMapper om = new ObjectMapper();
		
        OutputMessage message = null;
		try {
			message = client.call(handle, om.valueToTree(req), "org.rtm.request.PartitionerService", null, new HashMap<>(), 300000);
		} catch (Exception e) {e.printStackTrace();}
		
		StreamId sId = om.treeToValue(message.getPayload(), StreamId.class);
		
		if(message.getAgentError() != null) {
			AgentError error = message.getAgentError();
			System.err.println("Error in agent.");
			System.err.println(error.getClass());
			System.err.println(error.getErrorCode());
			System.err.println(error.getErrorDetails());
		}
		
		logger.info(" --- Returning StreamId --- (" +sId+")"  );
		return sId;
	}

	// keeping implicitly unified values between services and conf for now
	@SuppressWarnings("unused")
	private Properties mapWhereNeeded(Properties serviceParams) {
		map(serviceParams, "aggregateService.histSize", "histogram.nbPairs");
		map(serviceParams, "aggregateService.histApp", "histogram.approxMs");
		return serviceParams;
	}

	private void map(Properties serviceParams, String key, String newKey) {
		String prop = serviceParams.getProperty(key);
		serviceParams.put(newKey, prop);
		serviceParams.remove(newKey);
	}

	private Long getEffectiveIntervalSize(String hardInterval, LongTimeInterval effective) throws Exception {
		Long optimalSize = null;
		if( (hardInterval != null) && (hardInterval.toLowerCase().trim().length() > 0))
		{
			switch(hardInterval){
			case "auto":
				optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.defaultTargetDots")));
				break;
			case "max":
				optimalSize = effective.getSpan() + 1;
				break;
			default:
				optimalSize = Long.parseLong(hardInterval);
			}
		}
		return optimalSize;
	}
	/*
	//TODO: implement concurrent comparator (instead of waiting for both streams to complete)
	@SuppressWarnings("rawtypes")
	public StreamId compare(ComparisonRequest aggReq) throws Exception {
		AggregationRequest request1 = new AggregationRequest(aggReq.getTimeWindow1(), aggReq.getSelectors1(), aggReq.getServiceParams());
		AggregationRequest request2 = new AggregationRequest(aggReq.getTimeWindow2(), aggReq.getSelectors2(), aggReq.getServiceParams());

		logger.info("Launching comparison streams with request1=" + request1 + ", and request2=" + request2); 

		Stream s1 = sb.getStream(aggregate(request1));
		Stream s2 = sb.getStream(aggregate(request2));

		Properties props = s1.getStreamProp();

		long timeoutSecs = Long.parseLong(aggReq.getServiceParams().getProperty("aggregateService.timeout")) * 1000;
		long start = System.currentTimeMillis();

		while(!s1.isComplete() || !s2.isComplete()){
			if(System.currentTimeMillis() > (start + timeoutSecs))
				throw new Exception("Timeout reached while waiting for compared streams to complete.");
			else {
				Thread.sleep(300);
			}
		}

		logger.info("Comparison streams completed. Creating diff result stream.");

		s1 = new MetricsManager(props).handle(s1);
		s2 = new MetricsManager(props).handle(s2);

		Long intervalSize = Long.parseLong(s1.getStreamProp().getProperty(Stream.INTERVAL_SIZE_KEY));

		Stream outStream = initStream(s1.getTimeoutDurationSecs(), intervalSize, s1.getStreamProp());
		outStream.setCompositeStream(true);

		new StreamComparator(s1, s2, outStream, intervalSize).compare();

		outStream.setComplete(true);

		//TODO: implement a simple and clean close method
		s1.closeStream();
		sb.getStreamRegistry().remove(s1.getId().getStreamedSessionId());

		s2.closeStream();
		sb.getStreamRegistry().remove(s2.getId().getStreamedSessionId());

		sb.registerStreamSession(outStream);

		logger.info("Diff stream completed, results are available at id=" + outStream.getId());

		return outStream.getId();
	}
	 */
}
