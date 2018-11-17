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
import java.util.Map;

import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;

/**
 * @author doriancransac
 *
 */
public class LongRangeValueDeserializer extends JsonDeserializer {

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Object deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		final JsonNode node = parser.getCodec().readTree(parser);

		final Long id = node.get("streamPayloadIdentifier").asLong();
		final ObjectMapper objectMapper = (ObjectMapper)parser.getCodec();
		final SimpleModule module = new SimpleModule();
        module.addDeserializer(Dimension.class, new DimensionDeserializer());
        objectMapper.registerModule(module);
        MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Dimension.class);
    
		LongRangeValue lrv = new LongRangeValue(id);
		String dimensionMap = node.get("dimensionMap").toString();
		lrv.setDimensionsMap(objectMapper.readValue(dimensionMap, mapType));

		return lrv;
	}

}
