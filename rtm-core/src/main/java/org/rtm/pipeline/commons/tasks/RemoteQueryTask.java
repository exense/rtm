package org.rtm.pipeline.commons.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.rtm.client.HttpClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.WorkerRequest;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	public RemoteQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.accumulator = new MeasurementAccumulator(prop);
		this.prop = prop;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) throws IOException {

		HttpClient client = new HttpClient("localhost", 8097);
		WorkerRequest req = new WorkerRequest();
		req.setSelectors(this.sel);
		req.setRangeBucket(bucket);
		req.setProp(this.prop);
		
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String response = client.call(om.writeValueAsString(req), "/worker" ,"/work");
		client.close();
		return om.readValue(response, LongRangeValue.class);
	}
}


