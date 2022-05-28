/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.db.FilterQuery;
import org.rtm.db.QueryClient;
import org.rtm.request.selection.Selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.StreamingOutput;


/**
 * @author doriancransac
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MeasurementService{

	private static int MAX_SAMPLING_CSV_FIELDS=10000;

	private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);
	
	private MeasurementAccessor measurementAccessor;

	public MeasurementService(MeasurementAccessor measurementAccessor){
		this.measurementAccessor = measurementAccessor;
	}

	public List<Map<String, Object>> selectMeasurements(List<Selector> slt, String orderBy, int direction, int skip, int limit, Properties prop) throws Exception{
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		Stream<? extends Map> stream = new QueryClient(prop, measurementAccessor).executeQuery(new FilterQuery(slt, timeField, timeFormat).getQuery(), orderBy, direction, skip, limit);
		
		stream.forEach(o-> {
			Map<String, Object> m = (Map) o;
			res.add(m);
		});
		return res;
	}

	public StreamingOutput getMeasurementsAsOutputStream(List<Selector> slt, String orderBy, int direction, int skip, int limit, Properties prop) throws Exception{
		StreamingOutput streamingOutput = outputStream -> {
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));
			String outputFormat = (String) prop.get("measurementService.outputFormat");
			boolean isCSV = outputFormat != null && outputFormat.equals("CSV");
			MeasurementFormatter formatter = (isCSV) ? new MeasurementCSVFormatter() : new MeasrurementJsonFormatter();
			ZipEntry zipEntry = new ZipEntry("measurements" + formatter.getExtension());
			zipOut.putNextEntry(zipEntry);
			//Write header (for CSV need a sample to get list of fields (no exhaustive) )
			long start = System.currentTimeMillis();
			int sampling = (limit>0 && limit<MAX_SAMPLING_CSV_FIELDS) ? limit : MAX_SAMPLING_CSV_FIELDS;
			Stream<? extends Map> measurements = (isCSV) ? findMeasurements(slt, orderBy, direction, skip, sampling, prop) : null;
			formatter.writeHeader(zipOut,measurements);
			if (measurements != null) { measurements.close();}
			logger.trace("Elapse header " + (System.currentTimeMillis()-start));
			//Write content
			measurements = findMeasurements(slt, orderBy, direction, skip, limit, prop);
			formatter.writeBody(zipOut, measurements);
			if (measurements != null) { measurements.close();}
			logger.trace("Elapse content " + (System.currentTimeMillis()-start));
			//Write footer
			formatter.writeFooter(zipOut);
			zipOut.closeEntry();
			zipOut.close();
			outputStream.flush();
			outputStream.close();
			logger.trace("Elapse all " + (System.currentTimeMillis()-start));
		};

		return streamingOutput;
	}

	protected interface MeasurementFormatter {
		String getExtension();
		void writeHeader(ZipOutputStream zipOut,Stream<? extends Map> measurements) throws IOException;
		void writeBody(ZipOutputStream zipOut,Stream<? extends Map> measurements) throws IOException;
		void writeFooter(ZipOutputStream zipOut) throws IOException;
	}

	protected class MeasurementCSVFormatter implements  MeasurementFormatter {
		Set<String> fields=null;

		@Override
		public String getExtension() {
			return ".csv";
		}

		@Override
		public void writeHeader(ZipOutputStream zipOut, Stream<? extends Map> measurements) throws IOException {
			//Round 1 get unique fields list and write header
			fields = new HashSet();
			measurements.forEach(m -> fields.addAll(m.keySet()));
			
			StringBuffer stringBuffer = new StringBuffer();
			fields.forEach(k-> {
				if (stringBuffer.length() > 0) {
					stringBuffer.append(",");
				}
				stringBuffer.append(k);
			});
			stringBuffer.append('\n');
			zipOut.write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public void writeBody(ZipOutputStream zipOut, Stream<? extends Map> measurements) throws IOException {
			measurements.forEach(m-> {
				StringBuffer stringBuffer = new StringBuffer();
				for (String key: fields) {
					if (stringBuffer.length()>1) stringBuffer.append(",");
					String v = (m.get(key)!=null) ? m.get(key).toString().replace(",",";") : "";
					stringBuffer.append(v);
				}
				stringBuffer.append('\n');
				try {
					zipOut.write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException("Unable to write to body",e);
				}
			});

		}

		@Override
		public void writeFooter(ZipOutputStream zipOut) throws IOException {

		}
	}

	protected class MeasrurementJsonFormatter implements MeasurementFormatter {
		@Override
		public String getExtension() {
			return ".json";
		}

		@Override
		public void writeHeader(ZipOutputStream zipOut, Stream<? extends Map> measurements) throws IOException {
			zipOut.write('[');
		}

		@Override
		public void writeBody(ZipOutputStream zipOut, Stream<? extends Map> measurements) throws IOException {
			AtomicInteger count= new AtomicInteger();
			measurements.forEach(mo -> {
				Map<String,Object> m = (Map) mo;
				StringBuffer stringBuffer = new StringBuffer();
				if (count.get() > 0) {
					stringBuffer.append(',');
				}
				stringBuffer.append("{");
				for (String key: m.keySet()) {
					if (stringBuffer.length()>1) stringBuffer.append(",");
					stringBuffer.append("\"").append(key).append("\":\"").append(m.get(key).toString().replace("\"","\\\"")).append("\"");
				}
				stringBuffer.append("}");
				//only for readiness
				stringBuffer.append('\n');
				try {
					zipOut.write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException("Unable to write to body",e);
				}
				count.getAndIncrement();
			});
		}

		@Override
		public void writeFooter(ZipOutputStream zipOut) throws IOException {
			zipOut.write(']');
		}
	}

	private Stream<? extends Map> findMeasurements(List<Selector> slt, String orderBy, int direction, int skip, int limit, Properties prop) {
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		return new QueryClient(prop, measurementAccessor).executeQuery(new FilterQuery(slt, timeField, timeFormat).getQuery(), orderBy, direction, skip, limit);
	}



}
