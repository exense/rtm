package org.rtm.test.configuration;

import org.rtm.commons.Configuration;

public class ConfigurationGetter extends Thread{

	public final static int itNumber = 10000;
	
	public void run(){
		System.out.println("Hi I'm Getter Thread " + Thread.currentThread().getId());
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0; i < itNumber; i++){
			Configuration.getInstance();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if((i % 100) == 0)
				System.out.println(Configuration.getInstance().getProperty("test1"));
		}
		
		System.out.println(Thread.currentThread().getId() + ": Getter is done.");
		
	}
	
}
