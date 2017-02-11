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

package org.rtm.dao;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MongoQueryBuilderTest{

	List<Selector> selList = new ArrayList<>();
	List<Object> bindValues = new ArrayList<>();
	String jongoQuery;
	String mongoQuery;

	boolean init = false;

	@Before
	public void buildJongoValueListAndInitMA() throws Exception{
		if(!init){

			NumericalFilter nf = new NumericalFilter();
			nf.setKey("numKey");
			nf.setMinValue(1L);
			nf.setMaxValue(10L);

			TextFilter tf = new TextFilter();
			tf.setKey("textKey");
			tf.setValue("textVal");

			TextFilter regTf = new TextFilter();
			regTf.setKey("textKey");
			regTf.setValue(".*");
			regTf.setRegex(true);

			Selector sel = new Selector();
			sel.addNumericalFilter(nf);
			sel.addTextFilter(tf);
			sel.addTextFilter(regTf);

			Selector selBis = new Selector();
			selBis.addNumericalFilter(nf);
			selBis.addTextFilter(tf);
			selBis.addTextFilter(regTf);

			this.selList.add(sel);
			this.selList.add(selBis);

			this.jongoQuery = MongoQueryBuilder.buildJongoQuery(this.selList, this.bindValues);
			this.mongoQuery = MongoQueryBuilder.buildMongoQuery(this.selList);
			//System.out.println("mongo :" + replaceOperatorsAndBinds(mongoQuery, false, true));
			//System.out.println("jongo :" + replaceOperatorsAndBinds(jongoQuery, true, false));
		}
	}

	@Test
	public void validateJsonSyntaxForMongo() throws Exception{
		Assert.assertEquals(true, isJsonValid(replaceOperatorsAndBinds(mongoQuery, false, true)));
	}

	@Test
	public void validateJsonSyntaxForJongo() throws Exception{
		Assert.assertEquals(true, isJsonValid(replaceOperatorsAndBinds(jongoQuery, true, false)));
	}

	@Test
	public void validateConsistentJongoBinds() throws Exception{
		Assert.assertEquals(this.bindValues.size(), countPatternOccurences(this.jongoQuery,"#"));
	}

	@Test
	public void validateNumberOfOpsForJongo() throws Exception{
		testOccurences(jongoQuery);
	}

	@Test
	public void validateNumberOfOpsForMongo() throws Exception{
		testOccurences(mongoQuery);
	}

	private void testOccurences(String query){
		Assert.assertEquals(1, countPatternOccurences(query,"\\$or"));
		Assert.assertEquals(2, countPatternOccurences(query,"\\$regex"));
		Assert.assertEquals(2, countPatternOccurences(query,"\\$lt"));
		Assert.assertEquals(2, countPatternOccurences(query,"\\$gt"));
	}

	private String replaceOperatorsAndBinds(String query, boolean replaceHashasBindCharacter, boolean surroundRegexPatterns ){
		String replacedQuery = query
				.replace("$gte", "\"gte\"")
				.replace("$lte", "\"lte\"")
				.replace("$or", "\"or\"")
				.replace("$lt", "\"lt\"")
				.replace("$gt", "\"gt\"")
				.replace("$regex", "\"regex\"");

		if(replaceHashasBindCharacter)
			replacedQuery = replacedQuery.replace("#", "\"val\"");
		
		if(surroundRegexPatterns)
			replacedQuery = replacedQuery.replace(".*", "\"val\"");

		return replacedQuery;
	}

	private static boolean isJsonValid(String json) {
		boolean is = true;

		JsonReader jr = Json.createReader(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		try{
			@SuppressWarnings("unused")
			JsonObject jsonObj = jr.readObject();
		}catch(JsonParsingException e){
			e.printStackTrace();
			is = false;
		}

		return is;
	}

	private static int countPatternOccurences(String str, String pat){
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(str);
		int count = 0;

		while (m.find())
			count++;

		return count;
	}

}
