package org.rtm.metrics;

import java.util.HashMap;
import java.util.Map;

public class MapWorkObject implements WorkObject{

	Map<String, Object> backingMap;
	
	
	public MapWorkObject() {
		this.backingMap = new HashMap<String, Object>();
	}
	
	@Override
	public Object getValueObject(String key) {
		return backingMap.get(key);
	}

	@Override
	public void setValueObject(String key, Object wobj) {
		backingMap.put(key, wobj);
	}

}
