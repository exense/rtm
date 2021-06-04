package org.rtm.commons;

import ch.exense.commons.app.Configuration;
import step.core.collections.Collection;
import step.core.collections.CollectionFactory;
import step.core.collections.filesystem.FilesystemCollectionFactory;
import step.core.collections.Document;
import org.rtm.commons.utils.MeasurementUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class RtmContext {

    private Configuration configuration;
    private CollectionFactory collectionFactory;
    private MeasurementAccessor measurementAccessor;
    private MeasurementUtils measurementUtils;
    
    public static String EID_KEY = "model.key.eId";
    public static String BEGIN_KEY = "model.key.begin";
    public static String VALUE_KEY = "model.key.value";
    public static String NAME_KEY = "model.key.name";
    //Used in Aggregates only (could actually go back to End + Duration for more consistency)
    public static String SESSION_KEY = "model.key.sId";
    public static String END_KEY = "model.key.end";

    public RtmContext(Configuration configuration) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this(configuration,null);
    }

    public RtmContext(Configuration configuration, CollectionFactory collectionFactory) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.configuration = configuration;
        if (collectionFactory != null) {
            this.collectionFactory = collectionFactory;    
        } else {
            String collectionClassname = this.getConfiguration().getProperty("db.type", FilesystemCollectionFactory.class.getName());
            this.collectionFactory = (CollectionFactory) Class.forName(collectionClassname)
                    .getConstructor(Properties.class).newInstance(this.getConfiguration().getUnderlyingPropertyObject());
        }
        
        Collection<Document> collection = this.collectionFactory.getCollection(MeasurementAccessor.ENTITY_NAME, Document.class);
        measurementAccessor = new MeasurementAccessor(collection);
        measurementUtils = new MeasurementUtils(this);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public CollectionFactory getCollectionFactory() {
        return collectionFactory;
    }

    public void setCollectionFactory(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    public MeasurementAccessor getMeasurementAccessor() {
        return measurementAccessor;
    }

    public void setMeasurementAccessor(MeasurementAccessor measurementAccessor) {
        this.measurementAccessor = measurementAccessor;
    }

    public MeasurementUtils getMeasurementUtils() {
        return measurementUtils;
    }

    public void setMeasurementUtils(MeasurementUtils measurementUtils) {
        this.measurementUtils = measurementUtils;
    }

    public String getEidKey() {
        return configuration.getProperty(EID_KEY);
    }

    public String getBeginKey() {
        return configuration.getProperty(BEGIN_KEY);
    }

    public String getValueKey() {
        return configuration.getProperty(VALUE_KEY);
    }

    public String getNameKey() {
        return configuration.getProperty(NAME_KEY);
    }

    public String getSessionKey() {
        return configuration.getProperty(SESSION_KEY);
    }

    public String getEndKey() {
        return configuration.getProperty(END_KEY);
    }
}
