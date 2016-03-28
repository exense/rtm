package org.rtm.test.configuration;

import org.rtm.commons.Configuration;
import org.rtm.exception.ConfigurationException;

public class ConfigurationChanger extends Thread{
	
	public final static int itNumber = 200;


	public void run(){
		System.out.println("Hi I'm Changer Thread " + Thread.currentThread().getId());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(int i = 0; i < itNumber; i++){
			try {
				Configuration.triggerReload();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		System.out.println(Thread.currentThread().getId() + ": Changer is done");
		
	}
	
}
