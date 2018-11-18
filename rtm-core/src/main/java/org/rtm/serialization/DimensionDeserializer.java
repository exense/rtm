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

import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.WorkDimension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author doriancransac
 *
 */
public class DimensionDeserializer extends JsonDeserializer<Dimension> {

	@Override
	public Dimension deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
		final SimpleModule module = new SimpleModule();
        module.addDeserializer(WorkDimension.class, new WorkDimensionDeserializer());
        mapper.registerModule(module);
        
        Dimension dim = mapper.treeToValue(node.get("metrics"), WorkDimension.class);
        dim.setDimensionName(node.get("dimensionName").toString());
        return dim;
	}

}