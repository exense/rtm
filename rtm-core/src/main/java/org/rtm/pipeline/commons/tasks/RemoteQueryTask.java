package org.rtm.pipeline.commons.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.WorkerRequest;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.fasterxml.jackson.databind.ObjectMapper;

import step.grid.TokenWrapper;
import step.grid.client.AbstractGridClientImpl.AgentCommunicationException;
import step.grid.client.GridClient;
import step.grid.client.GridClientException;
import step.grid.io.OutputMessage;

public class RemoteQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	//TODO: pass gracefully and isolate outside of "core"
	public static GridClient gridCLient;
	
	public RemoteQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.accumulator = new MeasurementAccumulator(prop);
		this.prop = prop;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) throws IOException {
		LongRangeValue lrv = null;
		
		TokenWrapper tokenHandle = null;
		try {
			tokenHandle = gridCLient.getTokenHandle(new HashMap<>(), new HashMap<>(), false);
		} catch (AgentCommunicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		WorkerRequest req = new WorkerRequest();
		req.setSelectors(this.sel);
		req.setRangeBucket(bucket);
		req.setProp(this.prop);
		
		//TODO: pool/cache/static mapper
		ObjectMapper om = new ObjectMapper();
        //gridCLient.registerFile(new File())
        OutputMessage message = null;
		try {
			message = gridCLient.call(tokenHandle.getID(), om.valueToTree(req), "org.rtm.request.WorkerService", null, new HashMap<>(), 300000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				gridCLient.returnTokenHandle(tokenHandle.getID());
			} catch (AgentCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GridClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		lrv = om.treeToValue(message.getPayload(), LongRangeValue.class);
		System.out.println("["+lrv.getStreamPayloadIdentifier().getIdAsTypedObject()+"]=" + lrv);
		return lrv;
	}
}


