package org.rtm.orchestration;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.rtm.core.DateTimeInterval;
import org.rtm.core.LongTimeInterval;
import org.rtm.queries.TimebasedParallelExecutor;
import org.rtm.requests.guiselector.Selector;
import org.rtm.results.ResultHandler;

public class SplittingOrchestrator extends Orchestrator{

	private TimebasedParallelExecutor tpe;
	
	public SplittingOrchestrator(DateTimeInterval dateTimeInterval){
		LongTimeInterval lti = dateTimeInterval.toLongTime();
		tpe = new TimebasedParallelExecutor( lti, super.computeOptimalInterval(lti.getSpan(), 20));
	}

	public ConcurrentMap execute(List<Selector> sel, Properties requestProp) throws Exception {
		ResultHandler rh = new ResultHandler();
		tpe.processMongoQueryParallel(3, 30L, rh, sel, requestProp);
		return rh.getStreamHandle(); 
	}

}
