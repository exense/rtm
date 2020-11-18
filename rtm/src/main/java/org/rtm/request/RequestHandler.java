package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.commons.Configuration;
import org.rtm.db.QueryClient;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.pipeline.PipelineExecutionHelper;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.pull.PullPipeline;
import org.rtm.pipeline.pull.builders.PullPipelineBuilder;
import org.rtm.pipeline.pull.builders.SimplePipelineBuilder;
import org.rtm.pipeline.pull.builders.tasks.PartitionedPullQueryBuilder;
import org.rtm.pipeline.pull.builders.tasks.PullTaskBuilder;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.selection.Selector;
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
		long startTs=0;
		if (logger.isTraceEnabled()){
			startTs = System.currentTimeMillis();
		}
		List<Selector> sel = aggReq.getSelectors1();
		LongTimeInterval lti = aggReq.getTimeWindow1();
		
		Properties prop = new Properties();
		prop.putAll(Configuration.getInstance().getUnderlyingPropertyObject());
		//prop.putAll(mapWhereNeeded(aggReq.getServiceParams()));
		prop.putAll(aggReq.getServiceParams());

		QueryClient db = new QueryClient(prop);
		
		/* Parallization inputs*/
		long timeoutSecs = Long.parseLong(prop.getProperty("aggregateService.timeout"));
		int subPartitioning = Integer.parseInt(prop.getProperty("aggregateService.partition"));
		int subPoolSize = Integer.parseInt(prop.getProperty("aggregateService.cpu"));

		if (logger.isTraceEnabled()){
			logger.trace("METRIC - init DB and settings: " + (System.currentTimeMillis()-startTs));
		}
		boolean useHeuristic = prop.getProperty("useHistHeuristic") != null? Boolean.parseBoolean(prop.getProperty("useHistHeuristic")) : true;
		if(useHeuristic)
		{
			int heuristicSampleSize = prop.getProperty("heuristicSampleSize") != null? Integer.parseInt(prop.getProperty("heuristicSampleSize")) : 1000;
			float errorMarginPercentage = prop.getProperty("errorMarginPercentage") != null? Float.parseFloat(prop.getProperty("errorMarginPercentage")) : 0.01F;
			int optimalHistApp = (int)Math.round(db.run90PclOnFirstSample(heuristicSampleSize, sel) * errorMarginPercentage + 1);
			
			// allowing user to set histSize (in case of memory problems)
			//prop.put("aggregateService.histSize", "40");
			// however, the heuristic will override the user-defined parameter fow now if useHistHeuristic is set to true in central conf
			prop.put("aggregateService.histApp", Integer.toString(optimalHistApp));
			if (logger.isDebugEnabled())
				logger.debug("Using value " + optimalHistApp + " for histApp heuristic.");
		}
		if (logger.isTraceEnabled()){
			logger.trace("METRIC - global heuristic calculated: " + (System.currentTimeMillis()-startTs));
		}
		
		LongTimeInterval effective = db.findEffectiveBoundariesViaMongo(lti, sel);
		Long optimalSize = getEffectiveIntervalSize(prop.getProperty("aggregateService.granularity"), effective);

		Stream<Long> stream = initStream(timeoutSecs, optimalSize, prop);
		ResultHandler<Long> rh = new StreamResultHandler(stream);

		if (logger.isDebugEnabled())
			logger.debug("New Aggregation Request : TimeWindow=[effective=" + effective + "; optimalSize=" + optimalSize + "]; props=" + prop + "; selectors=" + aggReq.getSelectors1() + "; streamId=" + stream.getId());

		PullTaskBuilder tb = new PartitionedPullQueryBuilder(sel, prop, subPartitioning, subPoolSize, timeoutSecs);
		PullPipelineBuilder ppb = new SimplePipelineBuilder(
				effective.getBegin(), effective.getEnd(),
				optimalSize, rh, tb);

		// It's only useful to // at this level if we're looking to produce highly granular results,
		// which should almost never be the case
		PullPipeline pp = new PullPipeline(ppb, /*poolSize*/ 1, timeoutSecs, BlockingMode.BLOCKING);

		PipelineExecutionHelper.executeAndsetListeners(pp, stream);

		sb.registerStreamSession(stream);
		if (logger.isTraceEnabled()){
			logger.trace("METRIC - RequestHandler aggregate call completed after: " + (System.currentTimeMillis()-startTs));
		}
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
				optimalSize = QueryClient.computeOptimalIntervalSize(effective.getSpan(), Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.defaultTargetDots")));
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

		logger.debug("Launching comparison streams with request1=" + request1 + ", and request2=" + request2); 
		
		Stream s1 = sb.getStream(aggregate(request1));
		Stream s2 = sb.getStream(aggregate(request2));

		Properties props = s1.getStreamProp();
		
		long timeoutSecs = Long.parseLong(aggReq.getServiceParams().getProperty("aggregateService.timeout")) * 300;
		long start = System.currentTimeMillis();
		
		while(!s1.isComplete() || !s2.isComplete()){
			if(System.currentTimeMillis() > (start + timeoutSecs))
				throw new Exception("Timeout reached while waiting for compared streams to complete.");
			else {
				Thread.sleep(300);
			}
		}
		
		logger.debug("Comparison streams completed. Creating diff result stream.");
		
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
		
		logger.debug("Diff stream completed, results are available at id=" + outStream.getId());
		
		return outStream.getId();
	}

}
