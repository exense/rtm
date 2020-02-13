package org.rtm.stream.result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, property="_class")
public abstract class Identifier<T> {

	private T idAsTypedObject;

	public Identifier() {
	}	
	
	public Identifier(T idAsTypedObject) {
		this.idAsTypedObject = idAsTypedObject;
	}	
	
	public T getIdAsTypedObject() {
		return idAsTypedObject;
	}
	
	public void setIdAsTypedObject(T idAsTypedObject) {
		this.idAsTypedObject = idAsTypedObject;
	}

}
