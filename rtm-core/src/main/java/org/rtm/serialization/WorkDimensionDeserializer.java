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
package org.rtm.serialization;

import java.io.IOException;
import java.util.concurrent.atomic.LongAccumulator;

import org.rtm.metrics.WorkObject;
import org.rtm.stream.WorkDimension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author doriancransac
 *
 */
public class WorkDimensionDeserializer extends JsonDeserializer<WorkDimension> {

	@Override
	public WorkDimension deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		
		final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(LongAccumulator.class, new LongAccumulatorDeserializer());
        mapper.registerModule(module);        
        
        WorkDimension workDimension = new WorkDimension();
        
        node.fields().forEachRemaining(e -> {
        	String stateClass = getStateClassForAccumulator(e.getKey());
        	try {
				WorkObject workObject = (WorkObject)mapper.readValue(e.getValue().toString(), Class.forName(stateClass));
				workDimension.put(e.getKey(), workObject);
			} catch (Exception e1) {
				
				e1.printStackTrace();
			}
        	});
        
        return workDimension;
	}
	
	private String getStateClassForAccumulator(String accumulatorClass){
		String[] accShortName = accumulatorClass.split("\\.");
		//return accumulatorClass + "$" + accShortName[accShortName.length - 1] + "State";
		return accumulatorClass + "State";
	}

}
