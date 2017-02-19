package org.rtm.queries;

import java.util.HashMap;
import java.util.Map;

import org.rtm.core.AggregateResult;
import org.rtm.results.AggregationResult;

public class QueryHandler extends AggregateResult {

	public AggregationResult handle(Iterable<? extends Map> iterable) {
		for(Map<String, Object> m : iterable)
			System.out.println(m);
		
		Map<String, Object> res = new HashMap<>();
		res.put("name", "Transaction1");
		res.put("begin", 123L);
		
		return new AggregationResult(123L, res, "name");
	}

}
