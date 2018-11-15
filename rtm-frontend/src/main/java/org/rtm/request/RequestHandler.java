package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.commons.Configuration;
import org.rtm.db.DBClient;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.commons.PipelineExecutionHelper;
import org.rtm.pipeline.pull.PullPipeline;
import org.rtm.pipeline.pull.builders.pipeline.PullPipelineBuilder;
import org.rtm.pipeline.pull.builders.pipeline.SimplePullPipelineBuilder;
import org.rtm.pipeline.pull.builders.task.PartitionedPullTaskBuilder;
import org.rtm.pipeline.pull.builders.task.PullTaskBuilder;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.selection.Selector;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamComparator;
import org.rtm.stream.StreamId;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamBroker sb;
	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamBroker ssm){
		this.sb = ssm;
	}

	public StreamId aggregate(AggregationRequest aggReq) throws Exception{
		List<Selector> sel = aggReq.getSelectors1();
		LongTimeInterval lti = aggReq.getTimeWindow1();
		
		Properties prop = Configuration.getInstance().getUnderlyingPropertyObject();
		//prop.putAll(mapWhereNeeded(aggReq.getServiceParams()));
		prop.putAll(aggReq.getServiceParams());

		/* Parallization inputs*/
		long timeoutSecs = Long.parseLong(prop.getProperty("aggregateService.timeout"));
		int subPartitioning = Integer.parseInt(prop.getProperty("aggregateService.partition"));
		int subPoolSize = Integer.parseInt(prop.getProperty("aggregateService.cpu"));

		boolean useHeuristic = prop.getProperty("useHistHeuristic") != null? Boolean.parseBoolean(prop.getProperty("useHistHeuristic")) : true;
		if(useHeuristic)
		{
			int heuristicSampleSize = prop.getProperty("heuristicSampleSize") != null? Integer.parseInt(prop.getProperty("heuristicSampleSize")) : 1000;
			float errorMarginPercentage = prop.getProperty("errorMarginPercentage") != null? Float.parseFloat(prop.getProperty("errorMarginPercentage")) : 0.01F;
			int optimalHistApp = (int)Math.round(DBClient.run90PclOnFirstSample(heuristicSampleSize, sel) * errorMarginPercentage);
			prop.put("aggregateService.histSize", "100");
			prop.put("aggregateService.histApp", Integer.toString(optimalHistApp));
			logger.info("Using value " + optimalHistApp + " for histApp heuristic.");
		}
		
		LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
		Long optimalSize = getEffectiveIntervalSize(prop.getProperty("aggregateService.granularity"), effective);

		//TODO: compute target number of subbuckets and raise warning or circuit-break if > 1M?
		
		Stream<Long> stream = initStream(timeoutSecs, optimalSize, prop);
		ResultHandler<Long> rh = new StreamResultHandler(stream);

		logger.info("New Aggregation Request : TimeWindow=[effective=" + effective + "; optimalSize=" + optimalSize + "]; props=" + prop + "; selectors=" + aggReq.getSelectors1() + "; streamId=" + stream.getId());

		PullTaskBuilder tb = new PartitionedPullTaskBuilder(sel, prop, subPartitioning, subPoolSize, timeoutSecs);
		PullPipelineBuilder ppb = new SimplePullPipelineBuilder(
				effective.getBegin(), effective.getEnd(),
				optimalSize, rh, tb);

		// It's only useful to // at this level if we're looking to produce highly granular results,
		// which should almost never be the case
		PullPipeline pp = new PullPipeline(ppb, /*poolSize*/ 1, timeoutSecs, BlockingMode.BLOCKING);

		PipelineExecutionHelper.executeAndsetListeners(pp, stream);

		sb.registerStreamSession(stream);
		return stream.getId();
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

	private Stream<Long> initStream(long timeout, Long optimalSize, Properties prop) {
		
		prop.setProperty(Stream.INTERVAL_SIZE_KEY, optimalSize.toString());
				
		Stream<Long> stream = new Stream<>(prop);
		stream.setTimeoutDurationSecs(timeout);

		return stream;
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

}
