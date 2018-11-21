package org.rtm.pipeline.commons.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.rtm.client.HttpClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.WorkerRequest;
import org.rtm.selection.Selector;
import org.rtm.serialization.LongRangeValueDeserializer;
import org.rtm.stream.LongRangeValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class RemoteQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	public RemoteQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.accumulator = new MeasurementAccumulator(prop);
		this.prop = prop;
	}

	@SuppressWarnings("unchecked")
	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) throws IOException {
		LongRangeValue lrv = null;
		String workerIp = null;
		String response = null;
		try{
			String[] workerIps = System.getProperty("clusterArray").split(";");
			workerIp = workerIps[ThreadLocalRandom.current().nextInt(0, workerIps.length)];
		}catch(Throwable e){
			e.printStackTrace();
			//workerIp = "localhost";
		}

		HttpClient client = new HttpClient(workerIp, 8097);
		WorkerRequest req = new WorkerRequest();
		req.setSelectors(this.sel);
		req.setRangeBucket(bucket);
		req.setProp(this.prop);
		
		//TODO: pool/cache/static mapper
		ObjectMapper om = new ObjectMapper();
		final SimpleModule module = new SimpleModule();
        module.addDeserializer(LongRangeValue.class, new LongRangeValueDeserializer());
        om.registerModule(module);
 
		try{
		response = client.call(om.writeValueAsString(req), "/worker" ,"/work");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		client.close();
		lrv = om.readValue(response, LongRangeValue.class);
		
		return lrv;
	}
}


