package org.rtm.backend.queries;

import org.junit.Test;
import org.rtm.time.RangeBucket;

import com.mongodb.BasicDBObject;


public class MongoSubqueryCallableTest {
	
	@Test
	public void testTimeCriterion(){
		RangeBucket<Long> b = new RangeBucket<>(1L, 10L);
		//MongoSubqueryCallable callable = new MongoSubqueryCallable((Document)JSON.parse("{ \"name\" : \"Transaction1\"}"), b, null);
		//Assert.assertEquals(true,MongoSubqueryCallable.addTimeCriterion(b).toString().equals("{ \"$and\" : [ { \"begin\" : { \"$gte\" : 1}} , { \"begin\" : { \"$lt\" : 10}}]}"));
	}

	@Test
	public void quicky(){
		BasicDBObject o1 = new BasicDBObject();
		o1.append("abc", 1);
		
		BasicDBObject o2 = new BasicDBObject("abc", 1);
		
		System.out.println(o1);
		System.out.println(o2);
	}
}
