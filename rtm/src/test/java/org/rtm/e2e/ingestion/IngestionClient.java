package org.rtm.e2e.ingestion;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import ch.exense.commons.app.Configuration;
import step.core.collections.mongodb.MongoDBCollectionFactory;
import step.core.collections.Document;
import org.junit.Assert;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;
import org.rtm.commons.TransportClient;
import org.rtm.e2e.ingestion.load.BasicLoadDescriptor;
import org.rtm.e2e.ingestion.load.LoadDescriptor;
import org.rtm.e2e.ingestion.load.TransactionalProfile;
import org.rtm.e2e.ingestion.transport.TransportClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: turn into real @Remote tests
public class IngestionClient {

	private static final Logger logger = LoggerFactory.getLogger(IngestionClient.class);

	static MeasurementAccessor ma;
	static TransportClient tc;

	static String hostname = "localhost";
	static int port = 8099;

	static boolean init = false;

	//@Before
	public synchronized void init() throws IOException {
		if(!init){
			Configuration configuration = new Configuration(new File("src/main/resources/rtm.properties"));
			MongoDBCollectionFactory factory = new MongoDBCollectionFactory(configuration.getUnderlyingPropertyObject());
			ma = new MeasurementAccessor(factory.getCollection(MeasurementAccessor.ENTITY_NAME, Document.class));
			//tc = TransportClientBuilder.buildHttpClient(hostname, port);
			tc = ma;//This is not used and just return an accessor as of now TransportClientBuilder.buildAccessorClient(hostname, port);
			init = true;
		}

		//removeAllData();
	}

	//@Test
	public synchronized void simpleEndToEndTest(){

		Map<String, Object> m = TestMeasurementBuilder.buildStatic(TestMeasurementType.SIMPLE);

		boolean exception = false;
		try {
			tc.sendStructuredMeasurement(m);
		} catch (Exception e) {
			exception = true;
		}
		Assert.assertEquals(false, exception);
		Assert.assertEquals(1L, ma.getMeasurementCount());
	}

	//@Test
	public synchronized void simpleParallelTest(){

		LoadDescriptor ld = new BasicLoadDescriptor();

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	//@Test
	public synchronized void skewedLoadTest(){

		LoadDescriptor ld = new TransactionalProfile(
				100,  // pauseTime
				10,   // nbIterations
				10,   // nbTasks
				10,   // timeOut
				1000, // skewFactor
				200, // stdFactor
				5);  // targetCardinality 

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	//@Test
	public synchronized void longSkewedLoadTest(){

		LoadDescriptor ld = new TransactionalProfile(
				5,  // pauseTime
				100000, // nbIterations
				300,    // nbTasks
				30000,  // timeOut
				1000, // skewFactor
				200, // stdFactor
				5);  //  targetCardinality

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	public synchronized boolean executeEndToEndParallelTest(LoadDescriptor ld, TransportClient tc){

		boolean result = true;

		Vector<Callable<Boolean>> tasks = new Vector<>();

		ExecutorService executor = Executors.newFixedThreadPool(ld.getNbTasks());
		IntStream.rangeClosed(1, ld.getNbTasks()).forEach( i -> tasks.addElement(new IngestionCallable(tc, ld, i)));
		logger.debug("submitting task vector: " + tasks);
		try {
			for(Future<Boolean> f : executor.invokeAll(tasks, ld.getTimeOut(), TimeUnit.SECONDS)){
				f.get();
			}
		}
		catch (Exception e2) {
			logger.error("Test failed.", e2);
			result = false;
		}
		return result;
	}
	
	//@After
	public void close(){
		tc.close();
		removeAllData();
	} 

	public synchronized void removeAllData() {
		//ma.removeManyViaPattern(new HashMap<>());
	}

	public synchronized void removeTestData() {
		// TODO: + get specific test set counts to allow // execution of various JUnit tests
		// either that or dockerize everything and isolate test executions in multiple containers
	}


}
