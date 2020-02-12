package org.rtm.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.rtm.selection.NumericalFilter;
import org.rtm.selection.Selector;
import org.rtm.selection.TextFilter;

import com.mongodb.BasicDBObject;

public class BsonQuery {

	private String timeField;
	private String timeFormat;
	private Document query;

	public BsonQuery() {}

	public BsonQuery(List<Selector> sel, String timeField, String timeFormat) {
		this.timeFormat = timeFormat;
		this.timeField = timeField;
		this.query = selectorsToQuery(sel);
	}

	public Document getQuery() {
		return query;
	}

	public void setQuery(Document query) {
		this.query = query;
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		query.putAll(m);
	}

	public Document selectorsToQuery(List<Selector> selectors) {
		BasicDBObject top = new BasicDBObject();

		List<BasicDBObject> dbFilterList = new ArrayList<>();
		List<BasicDBObject> dbSelectorList = new ArrayList<>();

		for (Selector slt : selectors) {
			boolean hasFilter = false;
			if (slt.hasTextFilters()) {
				List<TextFilter> textFilters = slt.getTextFilters();
				dbFilterList.addAll(processTextFilters(textFilters));
				hasFilter = true;
			}
			if (slt.hasNumericalFilters()) {
				List<NumericalFilter> numericalFilters = slt.getNumericalFilters();
				dbFilterList.addAll(processNumFilters(numericalFilters));
				hasFilter = true;
			}
			if(hasFilter)
				dbSelectorList.add(new BasicDBObject("$and", dbFilterList));
			dbFilterList = new ArrayList<>();
		}
		if(dbSelectorList.size()>0)
			top.append("$or", dbSelectorList);
		//dbSelectorList = new ArrayList<>();
		return new Document(top);
	}

	private List<BasicDBObject> processNumFilters(List<NumericalFilter> numericalFilters) {
		List<BasicDBObject> numFilterList = new ArrayList<>();

		for (NumericalFilter f : numericalFilters) {
			String key = f.getKey();
			BasicDBObject numFilter = new BasicDBObject();
			BasicDBObject local = new BasicDBObject();

			if (f.hasMaxValue() || f.hasMinValue()) {
				if (f.hasMaxValue()) {
					if(key.equals(this.timeField) && this.timeFormat.equals("date")) {
						local.append("$lt", new Date(f.getMaxValue()));							
					}else {
						local.append("$lt", f.getMaxValue());	
					}
				}
				if (f.hasMinValue()) {
					if(key.equals(this.timeField) && this.timeFormat.equals("date")) {
						local.append("$gte", new Date(f.getMinValue()));
					}else {
						local.append("$gte", f.getMinValue());
					}
				}
			}

			numFilter.append(key, local);
			numFilterList.add(numFilter);
		}

		return numFilterList;
	}

	private List<BasicDBObject> processTextFilters(List<TextFilter> textFilters) {
		List<BasicDBObject> textFilterList = new ArrayList<>();

		for (TextFilter f : textFilters) {
			BasicDBObject textFilter = new BasicDBObject();
			Object local = null;

			if (f.getIsRegex())
				local = new BasicDBObject("$regex", f.getValue());
			else
				local = f.getValue();

			textFilter.append(f.getKey(), local);
			textFilterList.add(textFilter);
		}
		return textFilterList;
	}

}
