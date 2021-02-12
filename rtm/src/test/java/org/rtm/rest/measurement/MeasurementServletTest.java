package org.rtm.rest.measurement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.AggregationRequest;
import org.rtm.requests.guiselector.TestSelectorBuilder;
import org.rtm.utils.DateUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import static org.junit.Assert.*;

public class MeasurementServletTest {

	//@Test
	//require data in mongoDB, cannot be run on build server
	public void testExport() throws IOException, InterruptedException {
		//Thread.sleep(5000);

		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));

		//AggregationRequest ar = new AggregationRequest(null, TestSelectorBuilder.buildTestSelectorList("5f77329117305718f0c172f2"), props);
		AggregationRequest ar = new AggregationRequest(null, TestSelectorBuilder.buildTestSelectorList("601d1c0972709f3ca0af69a7"), props);
		ar.getServiceParams().put("aggregateService.granularity", "auto");
		ar.getServiceParams().put("aggregateService.timeout", "600");
		ar.getServiceParams().put("aggregateService.partition", "8");
		ar.getServiceParams().put("aggregateService.cpu", "4");
		ar.getServiceParams().put("aggregateService.timeField", "begin");
		ar.getServiceParams().put("aggregateService.timeFormat", "long");
		ar.getServiceParams().put("aggregateService.valueField", "value");
		ar.getServiceParams().put("aggregateService.groupby", "name");
		ar.getServiceParams().put("measurementService.nextFactor", "0");
		ar.getServiceParams().put("measurementService.outputFormat", "CSV");


		MeasurementServlet measurementServlet = new MeasurementServlet();
		long start = System.currentTimeMillis();
		ObjectMapper objectMapper = new ObjectMapper();
		String s = objectMapper.writeValueAsString(ar);
		Response export = measurementServlet.exportForm(s);
		StreamingOutput entity = (StreamingOutput) export.getEntity();
		try (FileOutputStream fo = new FileOutputStream("test.zip")) {
			entity.write(fo);
		}
		System.out.println(System.currentTimeMillis()-start);
	}

}