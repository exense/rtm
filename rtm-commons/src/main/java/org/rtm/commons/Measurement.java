package org.rtm.commons;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.jongo.marshall.jackson.oid.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class Measurement {
	
/** Keys should be externally configured and loaded from properties file **/
	
	public static final String BEGIN_KEY = "begin";
	public static final String VALUE_KEY = "value";
	public static final String NAME_KEY = "name";
	
	//TODO: Used in Aggregates only (could actually go back to End + Duration for more consistency)
	public static final String SESSION_KEY = "sId";
	public static final String END_KEY = "end";
	
/*****************************/
	
	@ObjectId
	private String _id;
		
	private Map<String,String> textAttributes;
	private Map<String,Long> numericalAttributes;
	
	public Measurement() {
		this.textAttributes = new TreeMap<String, String>();
		this.numericalAttributes = new TreeMap<String, Long>();
	}
	
	public String getId() {
		return _id;
	}

	@JsonIgnore
	public void setId(String id) {
		this._id = id;
	}

	@JsonProperty(Configuration.TEXT_PREFIX)
	public Map<String,String> getTextAttributes(){
		return this.textAttributes;
	}
	
	@JsonProperty(Configuration.NUM_PREFIX)
	public Map<String,Long> getNumericalAttributes(){
		return this.numericalAttributes;
	}
	
	@JsonIgnore
	public String getTextAttribute(String key){
		return this.textAttributes.get(key);
	}
	@JsonIgnore
	public Long getNumericalAttribute(String key){
		return this.numericalAttributes.get(key);
	}
	@JsonIgnore
	public void setTextAttribute(String key, String value){
		this.textAttributes.put(key,value);
	}
	@JsonIgnore
	public void setTextAttributes(Map<String,String> values){
		this.textAttributes.putAll(values);
	}
	@JsonIgnore
	public void setNumericalAttribute(String key, Long value){
		this.numericalAttributes.put(key,value);
	}
	@JsonIgnore
	public void setNumericalAttributes(Map<String,Long> values){
		this.numericalAttributes.putAll(values);
	}
	@JsonIgnore
	public String toString(){
		return "{ "+Configuration.TEXT_PREFIX+" : " + this.textAttributes + ", "+Configuration.NUM_PREFIX+" : " + this.numericalAttributes + "}";
	}

}
