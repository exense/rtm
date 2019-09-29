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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.request.selection.Selector;


/**
 * @author doriancransac
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MeasurementService{

	public MeasurementService(){}

	public List<Map<String, Object>> selectMeasurements(List<Selector> slt, String orderBy, int direction, int skip, int limit) throws Exception{
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		Iterable it = new DBClient().executeQuery(BsonQuery.selectorsToQuery(slt), orderBy, direction, skip, limit);

		for(Object o : it){
			Map<String, Object> m = (Map) o;
			res.add(m);
		}

		return res;
	}
}
