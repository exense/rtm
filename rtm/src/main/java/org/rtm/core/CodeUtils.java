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
