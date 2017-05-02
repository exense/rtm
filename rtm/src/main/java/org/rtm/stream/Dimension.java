package org.rtm.stream;

import java.util.HashMap;

import org.rtm.metrics.accumulation.LongAccumulationHelper;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dimension extends HashMap<String, Long>{
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

}
