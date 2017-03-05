package org.rtm.pipeline.callables.push.harvest;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings({"rawtypes", "unchecked"})
public class HarvestCallableImpl implements HarvestCallable {

		private Future<LongRangeValue> future;
		private ResultHandler rh;
		private int timeoutSecs;

		public HarvestCallableImpl(ResultHandler rh, Future<LongRangeValue> future, int timeoutSecs){
			this.future = future;
			this.rh = rh;
			this.timeoutSecs = timeoutSecs;
		}

		@Override
		public Boolean call() throws Exception {

			LongRangeValue lrv = future.get(timeoutSecs, TimeUnit.SECONDS);
			if(lrv != null)
				rh.attachResult(lrv);
			else
				throw new Exception("Null result.");

			return true;
		}

	}