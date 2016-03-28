package org.rtm.core;

import java.util.ArrayList;
import java.util.List;

import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.dao.RTMMongoClient;
import org.rtm.dao.Selector;

public class MeasurementService{

	public MeasurementService(){}
	
	public List<Measurement> listAllMeasurements() throws Exception{
		List<Measurement> res = new ArrayList<Measurement>();
		for(Measurement m : RTMMongoClient.getInstance().selectMeasurements(null, 0, 0, "n.begin"))
			res.add(m);
		
		return res;
	}
	
	public List<Measurement> selectMeasurements(List<Selector> slt, String orderBy, int skip, int limit) throws Exception{
		List<Measurement> res = new ArrayList<Measurement>();
		Iterable<Measurement> it = RTMMongoClient.getInstance().selectMeasurements(slt, skip, limit, orderBy);
		
		boolean debug = Boolean.parseBoolean(Configuration.getInstance().getProperty("rtm.debug"));
		if(debug)
			System.out.println("selector=" + slt + ";" + "skip=" + skip+"; limit=" + limit + "; orderBy=" + orderBy);
		
		long start = System.currentTimeMillis();
		int itcount = 0;
		StringBuilder sb = new StringBuilder();
		for(Measurement m : it){
			itcount++;
			 if(debug){
				 long now = System.currentTimeMillis();
				 sb.append("iteration " + itcount + "; duration=" + (now - start) + "ms.\n");
				 start = now;
			 }
			
			res.add(m);
		}
		
		if(debug){
			long end = System.currentTimeMillis();
			sb.append("total duration=" + (end - start) + "ms.\n");
			System.out.println(sb.toString());
		}
		
		return res;
	}
}
