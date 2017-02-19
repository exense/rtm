package org.rtm.orchestration;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.rtm.backend.db.DBClient;
import org.rtm.backend.queries.MongoQuery;
import org.rtm.backend.queries.TimebasedParallelExecutor;
import org.rtm.backend.results.ResultHandler;
import org.rtm.core.DateTimeInterval;
import org.rtm.core.LongTimeInterval;
import org.rtm.dao.Selector;

import com.mongodb.MongoClient;

public class MongoOrchestrator extends Orchestrator{

	private TimebasedParallelExecutor tpe;
	
	public MongoOrchestrator(DateTimeInterval dateTimeInterval){
		LongTimeInterval lti = dateTimeInterval.toLongTime();
		new TimebasedParallelExecutor( lti, super.computeOptimalInterval(lti.getSpan(), 20));
	}

	public ConcurrentMap execute(List<Selector> sel, Properties requestProp) throws Exception {
		ResultHandler rh = new ResultHandler();
		tpe.processParallel(3, 30, rh, MongoQuery.selectorsToQuery(sel), requestProp);
		return rh.getStreamHandle(); 
	}

}
