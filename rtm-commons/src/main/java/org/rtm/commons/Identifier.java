package org.rtm.commons;

public abstract class Identifier<T> {

	private T underlyingObject;
	
	public Identifier(T underlyingObject) {
		this.underlyingObject = underlyingObject;
	}	
	
	public T getIdAsTypedObject() {
		return underlyingObject;
	}
	
	public void setIdAsTypedObject(T object) {
		underlyingObject = object;
	}

}
