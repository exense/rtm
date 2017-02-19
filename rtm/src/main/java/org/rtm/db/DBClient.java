package org.rtm.db;

import java.util.Map;

import org.bson.Document;
import org.rtm.commons.MeasurementAccessor;

public class DBClient {

	MeasurementAccessor ma = MeasurementAccessor.getInstance();
	
	public DBClient() {
		this.ma = MeasurementAccessor.getInstance();
	}

	@SuppressWarnings("rawtypes")
	public Iterable<? extends Map> executeQuery(Document timelessQuery) {
		return ma.find(timelessQuery);
	}

}
