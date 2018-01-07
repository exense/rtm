package org.rtm.stream;

import java.util.HashMap;

import org.rtm.metrics.accumulation.LongAccumulationHelper;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dimension extends HashMap<String, Object>{
	private static final long serialVersionUID = 5989391368060961616L;
	private static final Logger logger = LoggerFactory.getLogger(Dimension.class);

	private LongAccumulationHelper helper;
	private Histogram hist;
	
	private int histNbPairs;
	private int histApproxMs;

	public Histogram getHist() {
		return hist;
	}

	private String dimensionValue;

	public Dimension(String name, int histNbPairs, int histApproxMs){
		super();
		this.histNbPairs = histNbPairs;
		this.histApproxMs = histApproxMs;
		this.helper = new LongAccumulationHelper();
		this.hist = createHistogram();
		this.setDimensionValue(name);
	}

	public LongAccumulationHelper getAccumulationHelper(){
		return helper;
	}

	public void copyAndFlush() {
		if (helper != null) {
			helper.entrySet().stream().forEach(e -> {
				this.put(e.getKey(), e.getValue().longValue());
			});
		} else
			logger.error("Null helper");
		
		flush();
	}


	public void flush(){
		helper.clear();
		//hist.reset();
	}
	
	public String getDimensionValue() {
		return dimensionValue;
	}

	public void setDimensionValue(String value) {
		this.dimensionValue = value;
	}
	
	private Histogram createHistogram() {
		try {
			return new Histogram(this.histNbPairs, this.histApproxMs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getHistNbPairs() {
		return histNbPairs;
	}

	public int getHistApproxMs() {
		return histApproxMs;
	}
	
	@SuppressWarnings("unchecked")
	public Dimension diff(Dimension dim2) throws Exception {
		if(dim2 == null)
			throw new NoDimensionException("Null dimension, can't diff.");
		Dimension res = new Dimension(this.getDimensionValue(), this.getHistNbPairs(), this.getHistApproxMs());
		
		// Not actually needed in a context of a composite stream (since we don't post process metrics) 
		//res.hist = this.hist.diff(dim2.getHist());
		
		for(Object o : this.entrySet())
		{
			Entry<String, Object> e = (Entry<String, Object>) o;
			if(e == null || e.getKey() == null)
				throw new Exception("Null entry or null entry key for entry="+e);
			Object value2 = dim2.get(e.getKey());
			Object value1 = e.getValue();
			if(value2 instanceof Long)
				res.put(e.getKey(), (Long)dim2.get(e.getKey()) - (Long)e.getValue());
			if(value2 instanceof Float)
				res.put(e.getKey(), (Float)dim2.get(e.getKey()) - (Float)e.getValue());
		}
		
		return res;
	}

}
