package org.rtm.struct;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

import org.junit.Assert;
import org.junit.Test;

public class DimensionTest {

	@Test
	public void quicky(){
		Dimension d = new Dimension();
		Map<String, Map<String, LongAccumulator>> acc = d.getAccumulationHelper();
		acc.put("Transaction1", new HashMap<String, LongAccumulator>());
		Map<String, LongAccumulator> tr1 = acc.get("Transaction1");
		tr1.put("count", new LongAccumulator((x,y) -> x+1 , 0));
		LongAccumulator tr1_count = tr1.get("count");
		tr1_count.accumulate(1);
		tr1_count.accumulate(1);
		tr1_count.accumulate(1);
		
		Assert.assertEquals(null, d.get("Transaction1"));
		
		d.copyAndFlush();
		
		Assert.assertEquals(3L,((Map)d.get("Transaction1")).get("count"));
	}
}
