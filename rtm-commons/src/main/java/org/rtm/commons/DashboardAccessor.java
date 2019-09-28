package org.rtm.commons;

import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardAccessor {

	private static final Logger logger = LoggerFactory.getLogger(DashboardAccessor.class);

	private static DashboardAccessor INSTANCE = new DashboardAccessor();

	private DBClient client = DBClient.getInstance();

	public static DashboardAccessor getInstance() {
		return INSTANCE;
	}
	
	public static enum DashboardCollection{
		SESSIONS("sessions"), DASHBOARDS("dashboards"), DASHLETS("dashlets"), SERVICES("services"),
		QUERIES("queries"), PROCESSORS("processors"), FUNCTIONS("functions");
		
		private String collname;
		DashboardCollection(String collname) {
	        this.setCollname(collname);
	    }
		public String getCollname() {
			return collname;
		}
		public void setCollname(String collname) {
			this.collname = collname;
		}
	}

	public void insertObject(Map<String, Object> obj, DashboardCollection collection){
		client.getDb().getCollection(collection.getCollname()).insertOne(new Document(obj));
		logger.debug("Inserted " + obj.toString() + " into " + collection.getCollname());
	}
	
	public void insertDocument(Document doc, DashboardCollection collection){
		client.getDb().getCollection(collection.getCollname()).insertOne(doc);
		logger.debug("Inserted " + doc.toString() + " into " + collection.getCollname());
	}
	
	public Document getObject(String name, DashboardCollection collection){
		return client.getDb().getCollection(collection.getCollname()).find(new Document().append("name", name)).first();
	}

}
