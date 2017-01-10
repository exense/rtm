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
package org.rtm.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.rtm.commons.Measurement;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.core.MeasurementService;
import org.rtm.dao.RTMMongoClient;
import org.rtm.dao.NumericalFilter;
import org.rtm.dao.Selector;
import org.rtm.dao.TextFilter;

public class DAOTests {

	public static void main(String... args) throws Exception{

		new DAOTests().
		
		//testInsertAndFind();
		//testSelectors();
		listAll();
	}

	public void listAll() throws Exception{
		System.out.println(new MeasurementService().listAllMeasurements());
	}
	public void testInsertAndFind() throws Exception{

		Measurement m = new Measurement();
		m.setNumericalAttribute("value", 4L);
		m.setTextAttribute("name", "toto");
		MeasurementAccessor.getInstance().saveMeasurement(m);
		
		Integer skip = 0;
		Integer limit = 0;
		String sortAttribute = "begin";
		
		Selector slt = new Selector();

		List<Selector> selectors = new ArrayList<Selector>();

		/*
		
		TextFilter f1 = new TextFilter();
		f1.setKey("name");
		f1.setValue("toto");

		slt.addTextFilter(f1);
		
		*/
		
		NumericalFilter nf1 = new NumericalFilter();
		nf1.setKey("begin");
		nf1.setMinValue(new Date().getTime());
		nf1.setMaxValue(new Date().getTime());

		NumericalFilter nf2 = new NumericalFilter();
		nf2.setKey("value");
		nf2.setMinValue(0L);
		nf2.setMaxValue(200L);

		//slt.addNumericalFilter(nf1);
		slt.addNumericalFilter(nf2);
			
		selectors.add(slt);
		
		/**/
		
		System.out.println("selectors : \n" + selectors);
		System.out.println("--------------------");
		for (Measurement n : RTMMongoClient.getInstance().selectMeasurements(selectors, skip, limit, sortAttribute))
		{
			System.out.println(n);
		}
		


	}

	public void testSelectors() throws Exception{
		Selector slt = new Selector();

		List<Selector> selectors = new ArrayList<Selector>();
		List<Object> result = new ArrayList<Object>();

		TextFilter f1 = new TextFilter();
		f1.setKey("name");
		f1.setValue("MyMeasurement_5");

		TextFilter f2 = new TextFilter();
		f2.setKey("status");
		f2.setValue("PASSED");

		slt.addTextFilter(f1);
		//slt.addTextFilter(f2);
		
		/***/
		
		NumericalFilter nf1 = new NumericalFilter();
		nf1.setKey("begin");
		nf1.setMinValue(new Date().getTime());
		nf1.setMaxValue(new Date().getTime());

		NumericalFilter nf2 = new NumericalFilter();
		nf2.setKey("value");
		nf2.setMinValue(200L);
		nf2.setMaxValue(500L);

		//slt.addNumericalFilter(nf1);
		slt.addNumericalFilter(nf2);
		
		/***/
		
		selectors.add(slt);
		selectors.add(slt);
		//selectors.add(slt);

		System.out.println(selectors);
		System.out.println("--------------------");
		System.out.println(RTMMongoClient.buildQuery(selectors, result));
		System.out.println("--------------------");
		System.out.println(result);
	}
}
