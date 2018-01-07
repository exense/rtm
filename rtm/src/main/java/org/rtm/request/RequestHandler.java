package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.commons.Configuration;
import org.rtm.db.DBClient;
import org.rtm.metrics.postprocessing.PostMetricsFilter;
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

	public StreamId handle(AggregationRequest aggReq) throws Exception{
		List<Selector> sel = aggReq.getSelectors1();
		LongTimeInterval lti = aggReq.getTimeWindow1();
		Properties prop = aggReq.getServiceParams();

		try {//TODO: expose to client
			prop.put("histogram.nbPairs", Configuration.getInstance().getProperty("histogram.nbPairs"));
			prop.put("histogram.approxMs", Configuration.getInstance().getProperty("histogram.approxMs"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		/* Parallization inputs*/
		int poolSize = 1;
		long timeoutSecs = Long.parseLong(prop.getProperty("aggregateService.timeout"));
		int subPartitioning = Integer.parseInt(prop.getProperty("aggregateService.partition"));
		int subPoolSize = Integer.parseInt(prop.getProperty("aggregateService.cpu"));

		LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
		Long optimalSize = getEffectiveIntervalSize(prop.getProperty("aggregateService.granularity"), effective);

		Stream<Long> stream = initStream(timeoutSecs, optimalSize);
		ResultHandler<Long> rh = new StreamResultHandler(stream);

		logger.info("New Aggregation Request : TimeWindow=[effective=" + effective + "; optimalSize=" + optimalSize + "]; props=" + prop + "; selectors=" + aggReq.getSelectors1() + "; streamId=" + stream.getId());

		PullTaskBuilder tb = new PartitionedPullQueryBuilder(sel, prop, subPartitioning, subPoolSize, timeoutSecs);
		PullPipelineBuilder ppb = new SimplePipelineBuilder(
				effective.getBegin(), effective.getEnd(),
				optimalSize, rh, tb);

		PullPipeline pp = new PullPipeline(ppb, poolSize, timeoutSecs, BlockingMode.BLOCKING);

		PipelineExecutionHelper.executeAndsetListeners(pp, stream);

		sb.registerStreamSession(stream);
		return stream.getId();
	}

	private Stream<Long> initStream(long timeout, Long optimalSize) {
		Stream<Long> stream = new Stream<>();
		stream.setTimeoutDurationSecs(timeout);
		stream.getStreamProp().setProperty(Stream.INTERVAL_SIZE_KEY, optimalSize.toString());
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
	public StreamId handle(ComparisonRequest aggReq) throws Exception {
		AggregationRequest request1 = new AggregationRequest(aggReq.getTimeWindow1(), aggReq.getSelectors1(), aggReq.getServiceParams());
		AggregationRequest request2 = new AggregationRequest(aggReq.getTimeWindow2(), aggReq.getSelectors2(), aggReq.getServiceParams());

		logger.info("Launching comparison streams with request1=" + request1 + ", and request2=" + request2); 
		
		Stream s1 = sb.getStream(handle(request1));
		Stream s2 = sb.getStream(handle(request2));

		long timeoutSecs = Long.parseLong(aggReq.getServiceParams().getProperty("aggregateService.timeout")) * 1000;
		long start = System.currentTimeMillis();
		
		while(!s1.isComplete() || !s2.isComplete()){
			if(System.currentTimeMillis() > (start + timeoutSecs))
				throw new Exception("Timeout reached while waiting for compared streams to complete.");
			else
				Thread.currentThread().sleep(300);
		}
		
		logger.info("Comparison streams completed. Creating diff result stream.");
		
		s1 = new PostMetricsFilter().handle(s1);
		s2 = new PostMetricsFilter().handle(s2);
		
		Long intervalSize = Long.parseLong(s1.getStreamProp().getProperty(Stream.INTERVAL_SIZE_KEY));
		
		Stream<Long> outStream = initStream(s1.getTimeoutDurationSecs(), intervalSize);
		outStream.setCompositeStream(true);

		new StreamComparator(sb.getStream(s1.getId()), sb.getStream(s2.getId()), outStream, intervalSize).compare();

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
