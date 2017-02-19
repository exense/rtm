package org.rtm.backend.queries;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.rtm.buckets.RangeBucket;
import org.rtm.dao.Selector;

import com.mongodb.util.JSON;

public class MongoQuery extends Document {

	private static final long serialVersionUID = -2093436314450930059L;

	public MongoQuery(Document timelessQuery, RangeBucket<Long> next) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		// TODO Auto-generated method stub

	}

	public static Document selectorsToQuery(List<Selector> sel){
		return (Document) JSON.parse("{}");
	}
}
