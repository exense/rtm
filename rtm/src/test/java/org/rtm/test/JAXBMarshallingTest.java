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
