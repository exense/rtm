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
import org.rtm.commons.ExceptionHandling;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;
import org.rtm.e2e.ingestion.load.LoadDescriptor;
import org.rtm.e2e.ingestion.load.SimpleLoadDescriptor;
import org.rtm.e2e.ingestion.load.TransactionalProfile;
import org.rtm.e2e.ingestion.transport.TransportClient;
import org.rtm.e2e.ingestion.transport.TransportClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E2EIngestionSimulatorTest {

	private static final Logger logger = LoggerFactory.getLogger(E2EIngestionSimulatorTest.class);
	
	boolean executeEndToEndParallelTest_result;
	MeasurementAccessor ma;
	
	@Before
	public void init(){
		Configuration.initSingleton(new File("src/main/resources/rtm.properties"));
		ma = MeasurementAccessor.getInstance();
	}
	
	@Test
	public synchronized void simpleEndToEndTest(){
		
		removeAllData();
		
		Map<String, Object> m = TestMeasurementBuilder.buildStatic(TestMeasurementType.SIMPLE);
		
		boolean exception = false;
		try {
			TransportClient tc = TransportClientBuilder.buildHttpClient("localhost", 8099);
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
		
		TransportClient tc = TransportClientBuilder.buildHttpClient("localhost", 8099);
		LoadDescriptor ld = new SimpleLoadDescriptor();

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}
	
	@Test
	public synchronized void skewedLoadTestDemo(){
		
		TransportClient tc = TransportClientBuilder.buildHttpClient("localhost", 8099);
		LoadDescriptor ld = new TransactionalProfile(10, 10, 10, 1000, 200);

		Assert.assertEquals(true, executeEndToEndParallelTest(ld, tc));
		Assert.assertEquals(ld.getNbIterations() * ld.getNbTasks(), ma.getMeasurementCount());
	}
	
	public synchronized boolean executeEndToEndParallelTest(LoadDescriptor ld, TransportClient tc){
		
		removeAllData();
		executeEndToEndParallelTest_result = true;
		
		Vector<Future<Boolean>> tasks = new Vector<>();
		
		ExecutorService executor = Executors.newFixedThreadPool(ld.getNbTasks());
		IntStream.rangeClosed(1, ld.getNbTasks()).forEach( i -> tasks.addElement(executor.submit(new IngestionCallable(tc, ld, i))));  

		tasks.stream().forEach(f -> {
			try {
				if(!f.get(3, TimeUnit.SECONDS))
					this.executeEndToEndParallelTest_result = false;
			} catch (Exception e) {
				ExceptionHandling.processException(logger, e);
				this.executeEndToEndParallelTest_result = false;
			}
		});
		
		tc.close();
		
		return this.executeEndToEndParallelTest_result;
	}

	public synchronized void removeAllData() {
		ma.removeManyViaPattern(new HashMap<>());
	}

	public synchronized void removeTestData() {
		// TODO: + get specific test set counts to allow // execution of various JUnit tests
		// either that or dockerize everything and isolate test executions in multiple containers
	}

	
}
