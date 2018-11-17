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
import java.util.Map.Entry;

import org.rtm.metrics.WorkObject;
import org.rtm.stream.WorkDimension;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author doriancransac
 *
 */
public class WorkDimensionSerializer extends StdSerializer<WorkDimension>{

	private static final long serialVersionUID = 799068921949038388L;

	protected WorkDimensionSerializer() {
		super(WorkDimension.class);
	}
	
	protected WorkDimensionSerializer(Class<WorkDimension> t) {
		super(t);
	}

	@Override
	public void serialize(WorkDimension obj, JsonGenerator gen, SerializerProvider prov)
			throws IOException, JsonGenerationException {

        	gen.writeStartObject();
        	gen.writeFieldName("dimensionName");
        	gen.writeObject(obj.getDimensionName());
        	
        	gen.writeFieldName("metrics");
        	gen.writeStartObject();
        	for(Entry<String, WorkObject> entry : obj.entrySet()){
        		gen.writeObjectField(entry.getKey(), entry.getValue());
            }
        	gen.writeEndObject();
        	gen.writeEndObject();
		
	}

}
