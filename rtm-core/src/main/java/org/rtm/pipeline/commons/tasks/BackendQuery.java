/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.pipeline.commons.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.QueryClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.mongodb.client.MongoCursor;

/**
 * @author doriancransac
 *
 */
public class BackendQuery {

	private List<Selector> sel;
	private RangeBucket<Long> bucket;
	private MeasurementAccumulator accumulator;
	private Properties prop;

	public BackendQuery(List<Selector> sel, RangeBucket<Long> bucket, MeasurementAccumulator accumulator, Properties prop) {
		this.sel = sel;
		this.bucket = bucket;
		this.accumulator = accumulator;
		this.prop = prop;
	}

	public LongRangeValue execute() {

		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		QueryClient client =  new QueryClient(this.prop);
		BsonQuery query = client.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));

		// temporary fix for cursor leaks (ugly but works for now)
		Iterable it = new QueryClient(prop).executeQuery(query.getQuery());
		MongoCursor iterator = (MongoCursor)it.iterator();
		
		accumulator.handle(lrv, it);

		iterator.close();
		//
		
		return lrv;
	}

}
