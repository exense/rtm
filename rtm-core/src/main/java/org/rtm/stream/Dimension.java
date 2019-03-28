package org.rtm.stream;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, property="_class")
public abstract class Dimension<K, V> {

	private String dimensionName;
	
	protected HashMap<K, V> map = new HashMap<>();

	public Dimension(){}
	
	public Dimension(String name){
		setDimensionName(name);
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public void setDimensionName(String name) {
		this.dimensionName = name;
	}

	public V get(Object key) {
		return map.get(key);
	}

	public V put(K key, V value) {
		return map.put(key, value);
	}

	public HashMap<K, V> getMap() {
		return map;
	}

	public void setMap(HashMap<K, V> map) {
		this.map = map;
	}
}
