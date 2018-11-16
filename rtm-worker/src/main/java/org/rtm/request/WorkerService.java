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
package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;


/**
 * @author doriancransac
 *
 */
public class WorkerService{

	public LongRangeValue produceValueForBucket(List<Selector> selectors, RangeBucket<Long> rangeBucket, Properties prop) {

	 		LongRangeValue lrv = new LongRangeValue(rangeBucket.getLowerBound());
			BsonQuery query = DBClient.buildQuery(selectors, RangeBucket.toLongTimeInterval(rangeBucket));
			
			new MeasurementAccumulator(prop).handle(lrv, new DBClient().executeQuery(query));
			
			return lrv;
	}

}
