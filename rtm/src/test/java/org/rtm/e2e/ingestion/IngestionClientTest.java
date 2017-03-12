package org.rtm.e2e.ingestion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rtm.commons.Configuration;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;
import org.rtm.e2e.ingestion.load.BasicLoadDescriptor;
import org.rtm.e2e.ingestion.load.LoadDescriptor;
import org.rtm.e2e.ingestion.load.TransactionalProfile;
import org.rtm.e2e.ingestion.transport.TransportClient;
import org.rtm.e2e.ingestion.transport.TransportClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestionClientTest {

	private static final Logger logger = LoggerFactory.getLogger(IngestionClientTest.class);

	MeasurementAccessor ma;
	TransportClient tc;

	String hostname = "localhost";
	int port = 8099;

	boolean init = false;

	@Before
	public void init(){
		if(!init){
			Configuration.initSingleton(new File("src/main/resources/rtm.properties"));
			ma = MeasurementAccessor.getInstance();
			tc = TransportClientBuilder.buildHttpClient(hostname, port);
		}

		removeAllData();
	}

	@Test
	public synchronized void simpleEndToEndTest(){

		Map<String, Object> m = TestMeasurementBuilder.buildStatic(TestMeasurementType.SIMPLE);

		boolean exception = false;
		try {
			TransportClient tc = TransportClientBuilder.buildHttpClient(hostname, port);
			IngestionClient.sendStructuredMeasurement(tc, m);
			tc.close();
		} catch (Exception e) {
			exception = true;
		}
		Assert.assertEquals(false, exception);
		Assert.assertEquals(1L, ma.getMeasurementCount());
	}

	@Test
	public synchronized void simpleParallelTest(){

		LoadDescriptor ld = new BasicLoadDescriptor();

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	@Test
	public synchronized void skewedLoadTest(){

		LoadDescriptor ld = new TransactionalProfile(
				100,  // pauseTime
				10,   // nbIterations
				10,   // nbTasks
				10,   // timeOut
				1000, // skewFactor
				200); // stdFactor

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	@Test
	public synchronized void longLoadTest(){

		LoadDescriptor ld = new TransactionalProfile(
				100,  // pauseTime
				1000, // nbIterations
				20,    // nbTasks
				120,  // timeOut
				1000, // skewFactor
				200); // stdFactor

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
				if(!f.get()){
					// in practice will never be executed because f.get throws an exception
					result = false;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Test failed.", e);
			result = false;
		}finally{
			tc.close();
		}

		return result;
	}

	public synchronized void removeAllData() {
		ma.removeManyViaPattern(new HashMap<>());
	}

	public synchronized void removeTestData() {
		// TODO: + get specific test set counts to allow // execution of various JUnit tests
		// either that or dockerize everything and isolate test executions in multiple containers
	}


}
