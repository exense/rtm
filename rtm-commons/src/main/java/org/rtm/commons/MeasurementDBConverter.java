package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.mongodb.BasicDBObject;
import step.core.collections.Document;

@SuppressWarnings({"rawtypes","unchecked"})
public class MeasurementDBConverter {

	public static Document convertToMongo(String m){
		return new Document(BasicDBObject.parse(m));
	}
	
	public static List<Document> convertManyToMongo(List<Object> lm){
		List<Document> insertables = new ArrayList<>();

		lm.stream()
		.forEach(o -> {
			if (o instanceof String)
				insertables.add(convertToMongo((String)o));
			else{
				if (o instanceof Map)
					insertables.add(new Document((Map)o));
			}
		});
		return insertables;
	}
		
}
