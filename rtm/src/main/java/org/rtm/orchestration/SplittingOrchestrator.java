package org.rtm.orchestration;

import java.util.List;
import java.util.Properties;

import org.rtm.core.DateTimeInterval;
import org.rtm.core.LongTimeInterval;
import org.rtm.queries.TimebasedParallelExecutor;
import org.rtm.requests.guiselector.Selector;
import org.rtm.results.ResultHandler;
import org.rtm.struct.Stream;

public class SplittingOrchestrator extends Orchestrator{

	private TimebasedParallelExecutor tpe;
	
	public SplittingOrchestrator(DateTimeInterval dateTimeInterval){
		LongTimeInterval lti = dateTimeInterval.toLongTime();
		tpe = new TimebasedParallelExecutor( lti, super.computeOptimalInterval(lti.getSpan(), 20));
	}

	public Stream execute(List<Selector> sel, Properties requestProp) throws Exception {
		Stream stream = new Stream();
		ResultHandler rh = new ResultHandler(stream);
		tpe.processMongoQueryParallel(3, 30L, rh, sel, requestProp);
		
		return stream; 
	}

}
