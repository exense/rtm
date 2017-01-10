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

import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.rtm.dao.NumericalFilter;
import org.rtm.dao.Selector;
import org.rtm.dao.TextFilter;

public class JAXBMarshallingTest {
	
	private final static String contentType = "application/json";
	
	public static void main(String... args) throws Exception{
//		Class[] classArray = {Selector.class};
//		Selector slt = new Selector();
//		TextFilter f1 = new TextFilter();
//		f1.setKey("toto");
//		f1.setValue("tutu");
//		
//		TextFilter f2 = new TextFilter();
//		f2.setKey("allo");
//		f2.setValue("alhuile");
//		
//		NumericalFilter n1 = new NumericalFilter();
//		n1.setKey("begin");
//		n1.setMinValue(new Date().getTime());
//		n1.setMaxValue(new Date().getTime());
//		
//		slt.addTextFilter(f1);
//		slt.addTextFilter(f2);
//		
//		slt.addNumericalFilter(n1);
//		
//		JAXBContext jc = JAXB.createContext(classArray, null);
//
//        Marshaller marshaller = jc.createMarshaller();
//       // marshaller.setAdapter(new DateAdapter());
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, contentType);
//        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//        marshaller.marshal(slt, System.out);
	}

}
