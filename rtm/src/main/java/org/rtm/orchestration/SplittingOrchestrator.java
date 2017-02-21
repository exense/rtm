package org.rtm.orchestration;

import java.util.List;
import java.util.Properties;

import org.rtm.core.DateTimeInterval;
import org.rtm.core.LongTimeInterval;
import org.rtm.queries.TimebasedParallelExecutor;
import org.rtm.requests.guiselector.Selector;
import org.rtm.stream.TimebasedResultHandler;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamResultHandler;

public class SplittingOrchestrator extends Orchestrator{

	private TimebasedParallelExecutor tpe;
	
	public SplittingOrchestrator(DateTimeInterval dateTimeInterval){
		LongTimeInterval lti = dateTimeInterval.toLongTime();
		tpe = new TimebasedParallelExecutor( lti, super.computeOptimalInterval(lti.getSpan(), 20));
	}

	public Stream<Long> execute(List<Selector> sel, Properties requestProp) throws Exception {
		Stream<Long> stream = new Stream<>();
		TimebasedResultHandler<Long> rh = new StreamResultHandler(stream);
		tpe.processMongoQueryParallel(1, 60L, rh, sel, requestProp);
		
		return stream; 
	}

}
