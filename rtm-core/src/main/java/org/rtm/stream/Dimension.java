package org.rtm.stream;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, property="_class")
public abstract class Dimension<K, V> extends HashMap<K, V>{

	private static final long serialVersionUID = 4857774275498734248L;

	private String dimensionName;

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
}
