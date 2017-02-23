package org.rtm.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.rtm.request.selection.Selector;
import org.rtm.requests.guiselector.TestSelectorBuilder;

@SuppressWarnings("unchecked")
public class JSONMapperTest {
	
	@Test
	public void quicky() throws Exception{
		Map<String, Object> m = new HashMap<>();
		m.put("foo", "bar");
		m.put("1", 2);
		
		Map<String, Object> selector = new JSONMapper().convertObjectToType(TestSelectorBuilder.buildSimpleSelectorList().get(0), Map.class);

		System.out.println("map:" + selector);
		
		Selector s = new JSONMapper().convertObjectToType(selector, Selector.class);
		System.out.println("bean:" + s);
		
		String str = new JSONMapper().convertToJsonString(selector);
		System.out.println("bean:" + str);
		

	}

}
