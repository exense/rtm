package org.rtm.dao;

import java.util.List;

public class MongoQueryBuilder {
	
	public static String buildMongoQuery(List<Selector> selectors) throws Exception {

		int sltSize = selectors.size();
		StringBuilder genQuerySb = new StringBuilder();													// {

		if(sltSize != 0){
			if(sltSize > 1)
				genQuerySb.append("{$or : [");											// { $or : [

			for(Selector slt : selectors)
			{
				if(slt == null)
					throw new Exception("Selector is null :" + slt);
				else
				{
					genQuerySb.append("{");												// { $or : [ {

					if(slt.hasTextFilters())
					{
						List<TextFilter> textFilters = slt.getTextFilters();
						for(TextFilter f : textFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							genQuerySb.append("\"");
							genQuerySb.append(f.getKey());
							genQuerySb.append("\":");

							if(f.isRegex())
								genQuerySb.append("{$regex : \""+f.getValue()+"\"}");
							else
								genQuerySb.append("\""+f.getValue()+"\"");
							genQuerySb.append(",");
						}

					}


					if(slt.hasNumericalFilters())
					{
						List<NumericalFilter> numericalFilters = slt.getNumericalFilters();
						for(NumericalFilter f : numericalFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							if(f.hasMaxValue() || f.hasMinValue()){
								genQuerySb.append("\""+f.getKey()+"\": {");

								if(f.hasMaxValue()){
									genQuerySb.append("$lt : " + f.getMaxValue());
									if(f.hasMinValue()){
										genQuerySb.append(",");
									}
								}
								if(f.hasMinValue()){
									genQuerySb.append("$gte : " + f.getMinValue());
								}
								genQuerySb.append("},");
							}

						}
						genQuerySb.deleteCharAt(genQuerySb.length() - 1);
					}// End If Num Filters

				} // End If Selector null

				genQuerySb.append("},");
			} // End For Selectors
			if(sltSize > 1)
			{
				genQuerySb.deleteCharAt(genQuerySb.length() - 1);
				genQuerySb.append("]}");
			}

		}
		return genQuerySb.toString();
	}

	public static String buildJongoQuery(List<Selector> selectors, List<Object> bindValues) throws Exception {

		int sltSize = selectors.size();
		StringBuilder genQuerySb = new StringBuilder();													// {

		if(sltSize != 0){
			if(sltSize > 1)
				genQuerySb.append("{$or : [");											// { $or : [

			for(Selector slt : selectors)
			{
				if(slt == null)
					throw new Exception("Selector is null :" + slt);
				else
				{
					genQuerySb.append("{");												// { $or : [ {

					if(slt.hasTextFilters())
					{
						List<TextFilter> textFilters = slt.getTextFilters();
						for(TextFilter f : textFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							genQuerySb.append("\"");
							genQuerySb.append(f.getKey());
							genQuerySb.append("\":");

							if(f.isRegex())
								genQuerySb.append("{$regex : #}");
							else
								genQuerySb.append("#");

							bindValues.add(f.getValue());
							genQuerySb.append(",");
						}

					}


					if(slt.hasNumericalFilters())
					{
						List<NumericalFilter> numericalFilters = slt.getNumericalFilters();
						for(NumericalFilter f : numericalFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							if(f.hasMaxValue() || f.hasMinValue()){
								genQuerySb.append("\""+f.getKey()+"\": {");

								if(f.hasMaxValue()){
									bindValues.add(f.getMaxValue());
									genQuerySb.append("$lt : #");
									if(f.hasMinValue()){
										genQuerySb.append(",");
									}
								}
								if(f.hasMinValue()){
									bindValues.add(f.getMinValue());
									genQuerySb.append("$gte : #");
								}

								genQuerySb.append("},");
							}

						}
						genQuerySb.deleteCharAt(genQuerySb.length() - 1);
					}// End If Num Filters

				} // End If Selector null

				genQuerySb.append("},");
			} // End For Selectors
			if(sltSize > 1)
			{
				genQuerySb.deleteCharAt(genQuerySb.length() - 1);
				genQuerySb.append("]}");
			}

		}
		return genQuerySb.toString();
	}
}
