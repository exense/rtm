package org.rtm.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.core.AggregationService;
import org.rtm.core.ComplexServiceResponse;
import org.rtm.dao.NumericalFilter;
import org.rtm.dao.RTMMongoClient;
import org.rtm.dao.Selector;
import org.rtm.dao.TextFilter;

public class AggregationServiceTest {

	public static void main(String... args) throws Exception{

		Selector slt = new Selector();

		List<Selector> selectors = new ArrayList<Selector>();

		TextFilter f1 = new TextFilter();
		f1.setKey("name");
		f1.setValue("MyMeasurement_5");

		TextFilter f2 = new TextFilter();
		f2.setKey("status");
		f2.setValue("PASSED");

		//slt.addTextFilter(f1);
		//slt.addTextFilter(f2);
		
		/***/
		
		NumericalFilter nf1 = new NumericalFilter();
		nf1.setKey("begin");
		nf1.setMinValue(1411037310000L);
		nf1.setMaxValue(1411037320000L);

		NumericalFilter nf2 = new NumericalFilter();
		nf2.setKey("value");
		nf2.setMinValue(200L);
		nf2.setMaxValue(500L);

		slt.addNumericalFilter(nf1);
		//slt.addNumericalFilter(nf2);
		
		/***/
		
		selectors.add(slt);
		/*
		Map<String,List<Measurement>> res = new AggregationService()
		.buildAggregatesForTimeInconsistent(
				"mySid1",
				RTMMongoClient.getInstance().selectMeasurements(selectors, 0, 0, "n.begin")
				//, 14000, "toto", Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.VALUE_KEY, Measurement.SESSION_KEY);
				, 14000, "name", Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.VALUE_KEY, Measurement.SESSION_KEY);
				
		for(Entry<String,List<Measurement>> e : res.entrySet()){
			System.out.println(e.getKey() + " :         "+e.getValue());
		}
		
		*/
		
		Iterable<Measurement> it = RTMMongoClient.getInstance().selectMeasurements(selectors, 0, 0, Configuration.NUM_PREFIX +Configuration.SPLITTER+ Measurement.BEGIN_KEY);
		AggregationService as = new AggregationService();
		ComplexServiceResponse inconsistent = as.buildAggregatesForTimeInconsistent("mySid1", it, 1000, "name", Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.VALUE_KEY, Measurement.SESSION_KEY);
		ComplexServiceResponse consistent = AggregationService.makeDataConsistent(inconsistent, Measurement.SESSION_KEY, Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.NAME_KEY);
		
		for(Entry<String,List<Measurement>> e : consistent.getPayload().entrySet()){
			System.out.println(e.getKey() + " :         "+e.getValue());
		}
		
	}
	
}
