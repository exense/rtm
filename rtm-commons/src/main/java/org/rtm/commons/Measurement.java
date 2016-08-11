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
	
	@JsonProperty(Configuration.TEXT_PREFIX)
	private Map<String,String> t;
	@JsonProperty(Configuration.NUM_PREFIX)
	private Map<String,Long> n;
	
	public Measurement() {
		this.t = new TreeMap<String, String>();
		this.n = new TreeMap<String, Long>();
	}
	
	public String getId() {
		return _id;
	}

	@JsonIgnore
	public void setId(String id) {
		this._id = id;
	}
	@JsonProperty(Configuration.TEXT_PREFIX)
	public Map<String,String> gett(){
		return this.t;
	}
	@JsonProperty(Configuration.NUM_PREFIX)
	public Map<String,Long> getn(){
		return this.n;
	}
	
	@JsonIgnore
	public String getTextAttribute(String key){
		return this.t.get(key);
	}
	@JsonIgnore
	public Long getNumericalAttribute(String key){
		return this.n.get(key);
	}
	@JsonIgnore
	public void setTextAttribute(String key, String value){
		this.t.put(key,value);
	}
	@JsonIgnore
	public void setTextAttributes(Map<String,String> values){
		this.t.putAll(values);
	}
	@JsonIgnore
	public void setNumericalAttribute(String key, Long value){
		this.n.put(key,value);
	}
	@JsonIgnore
	public void setNumericalAttributes(Map<String,Long> values){
		this.n.putAll(values);
	}
	@JsonIgnore
	public String toString(){
		return "{ "+Configuration.TEXT_PREFIX+" : " + this.t + ", "+Configuration.NUM_PREFIX+" : " + this.n + "}";
	}

}
