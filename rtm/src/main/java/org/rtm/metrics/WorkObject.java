package org.rtm.metrics;

public interface WorkObject {

		public Object getPayload(String key);
		public void setPayload(String key, Object wobj);
}
