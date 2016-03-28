package org.rtm.test.configuration;

public class ConfigurationTest {

	public static final int nbChangers = 2;
	public static final int nbGetters = 5;
	
	public static void main(String... args){
		
		for (int i = 0; i < nbGetters; i++)
			new ConfigurationGetter().start();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < nbChangers; i++)
			new ConfigurationChanger().start();

}
	
	
}
