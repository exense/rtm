package org.rtm.metrics.accumulation;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rtm.db.QueryClient;
import org.rtm.measurement.MeasurementHelper;
import org.rtm.metrics.AccumulationManager;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.WorkDimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class MeasurementAccumulator {
	
	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccumulator.class);

	private MeasurementHelper mh;
	private AccumulationManager amgr;
	private Properties prop;
	private List<Selector> sel;
	private boolean approxBySeries;

	public MeasurementAccumulator(Properties prop){
		this.prop = prop;
		this.mh = new MeasurementHelper(prop);
		this.amgr = new AccumulationManager(prop);
		approxBySeries = Boolean.parseBoolean(prop.getProperty("aggregateService.bySeries", "true"));
	}

	public void handle(LongRangeValue lrv, Iterable<? extends Map> iterable, List<Selector> sel) {
		long start = System.currentTimeMillis();
		this.sel = sel;
		int count=0;
		for(Map m : iterable) {
			amgr.accumulateAll(getOrInitDimension(lrv, m), m.get(prop.get("aggregateService.valueField")));
			count++;
		}
		if (logger.isTraceEnabled())
			logger.trace("accumulate all measurements in range: " + (System.currentTimeMillis()-start) + " for count of " + count);
	}

	protected WorkDimension getOrInitDimension(LongRangeValue lrv, Map m) {
		String m_dimension = mh.getActualDimensionName(m);

		if(m_dimension == null || m_dimension.trim().isEmpty()){
			m_dimension = "default";
		}

		WorkDimension dimension = (WorkDimension)lrv.getDimension(m_dimension);
		if(dimension == null){
			dimension = new WorkDimension(m_dimension);
			lrv.setDimension(dimension);
			//calculate approxMs by dimension
			if (approxBySeries) {
				initApproxMsByDimemsion(m);
			}
		}
		return dimension;
	}
	
	protected synchronized void initApproxMsByDimemsion(Map m) {
		String dimensionName = mh.getActualDimensionName(m);
		boolean useHeuristic = prop.getProperty("useHistHeuristic") != null? Boolean.parseBoolean(prop.getProperty("useHistHeuristic")) : true;
		if(useHeuristic && prop.getProperty("aggregateService.histApp." + dimensionName,null) == null)
		{
			List<Selector> selector = mh.getDimensionSelectors(sel,m);
			QueryClient db = new QueryClient(prop);
			int heuristicSampleSize = prop.getProperty("heuristicSampleSize") != null? Integer.parseInt(prop.getProperty("heuristicSampleSize")) : 1000;
			float errorMarginPercentage = prop.getProperty("errorMarginPercentage") != null? Float.parseFloat(prop.getProperty("errorMarginPercentage")) : 0.01F;
			int optimalHistApp = (int)Math.round(db.run90PclOnFirstSample(heuristicSampleSize, selector) * errorMarginPercentage + 1);

			prop.setProperty("aggregateService.histApp." + dimensionName, Integer.toString(optimalHistApp));
			logger.debug("Using value " + optimalHistApp + " for histApp heuristic of dimention: " + mh.getActualDimensionName(m) + ".");
		}
	}


	public void mergeDimensionsLeft(WorkDimension dimension1, WorkDimension dimension2) {
		amgr.mergeAllLeft(dimension1, dimension2);
	}

}
