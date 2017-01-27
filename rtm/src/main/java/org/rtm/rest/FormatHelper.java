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
package org.rtm.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rtm.commons.Measurement;
import org.rtm.core.AggregateResult;
import org.rtm.rest.ServiceOutput;

/**
 * @author doriancransac
 *
 */
public class FormatHelper {

	public static ServiceOutput convertForJson(Map<String, List<Measurement>> data){

		ServiceOutput so = new ServiceOutput();
		List<AggregateResult> res = new ArrayList<AggregateResult>();

		for(Entry<String, List<Measurement>> e : data.entrySet())
		{
			AggregateResult ar = new AggregateResult();
			ar.setGroupby(e.getKey());
			ar.setData(e.getValue());
			res.add(ar);
		}
		so.setPayload(res);
		return so;
	}
}
