package org.rtm.e2e.ingestion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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

public class E2EIngestionSimulatorTest {

	private static final Logger logger = LoggerFactory.getLogger(E2EIngestionSimulatorTest.class);

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
			E2EIngestionSimulator.sendStructuredMeasurement(tc, m);
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

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc, 10));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	@Test
	public synchronized void skewedLoadTest(){

		LoadDescriptor ld = new TransactionalProfile(
				100,  // pauseTime
				10,   // nbIterations
				10,   // nbTasks
				1000, // skewFactor
				200); // stdFactor

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc, 10));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}
	
	@Test
	public synchronized void longLoadTest(){

		int timeout = 120;
		
		LoadDescriptor ld = new TransactionalProfile(
				100,  // pauseTime
				1000,   // nbIterations
				3,   // nbTasks
				1000, // skewFactor
				200); // stdFactor
		
		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc, timeout));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}

	public synchronized boolean executeEndToEndParallelTest(LoadDescriptor ld, TransportClient tc, int timeoutSecs){

		boolean result = true;

		Vector<Future<Boolean>> tasks = new Vector<>();

		ExecutorService executor = Executors.newFixedThreadPool(ld.getNbTasks());
		IntStream.rangeClosed(1, ld.getNbTasks()).forEach( i -> tasks.addElement(executor.submit(new IngestionCallable(tc, ld, i))));  

		try {
			for(Future<Boolean> f : tasks){
				if(!f.get(timeoutSecs, TimeUnit.SECONDS)){
					result = false;
					logger.error("Failed due to task :" + f);
					executor.shutdownNow();
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
