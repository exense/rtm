package org.rtm.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CodeUtils {
	
	public static String getCurrentDate(){

		   DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		   Date date = new Date();

		return dateFormat.format(date);
	}
	
	public static Date getCurrentDateAsDateObject(){

		   Date date = new Date();

		return date;
	}
	
	public static String getRandomUUID(){
		return java.util.UUID.randomUUID().toString();
		}

	public static void main (String[] args){
		
		System.out.println(getCurrentDate() + " , " + getRandomUUID());
	}
	
public static String readfile(String filename){
		
		StringBuilder stringBuilder = new StringBuilder();
		String resultStr = "";
		Reader reader = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));	
			int data = reader.read(); 
			while(data != -1) {
				stringBuilder.append((char) data);
				data = reader.read(); 
				}

			resultStr = stringBuilder.toString();
		}
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
		finally{
			try{
				if (reader != null)
					reader.close();
				
			}catch (Exception e){e.printStackTrace();}
			
		}
		return resultStr;
	}
	
	
}
