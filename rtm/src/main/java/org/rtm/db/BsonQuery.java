package org.rtm.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.rtm.request.selection.NumericalFilter;
import org.rtm.request.selection.Selector;
import org.rtm.request.selection.TextFilter;

import com.mongodb.BasicDBObject;

public class BsonQuery extends Document {

	private static final long serialVersionUID = -2093436314450930059L;

	public BsonQuery(Document timelessQuery) {
		super(timelessQuery);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		super.putAll(m);
	}

	public static Document selectorsToQuery(List<Selector> selectors) {
		BasicDBObject top = new BasicDBObject();

		List<BasicDBObject> dbFilterList = new ArrayList<>();
		List<BasicDBObject> dbSelectorList = new ArrayList<>();

		for (Selector slt : selectors) {
			if (slt.hasTextFilters()) {
				List<TextFilter> textFilters = slt.getTextFilters();
				dbFilterList.addAll(processTextFilters(textFilters));
			}
			if (slt.hasNumericalFilters()) {
				List<NumericalFilter> numericalFilters = slt.getNumericalFilters();
				dbFilterList.addAll(processNumFilters(numericalFilters));
			}
			dbSelectorList.add(new BasicDBObject("$and", dbFilterList));
			dbFilterList = new ArrayList<>();
		}
		top.append("$or", dbSelectorList);
		//dbSelectorList = new ArrayList<>();
		return new Document(top);
	}

	private static List<BasicDBObject> processNumFilters(List<NumericalFilter> numericalFilters) {
		List<BasicDBObject> numFilterList = new ArrayList<>();

		for (NumericalFilter f : numericalFilters) {
			BasicDBObject numFilter = new BasicDBObject();
			BasicDBObject local = new BasicDBObject();

			if (f.hasMaxValue() || f.hasMinValue()) {
				if (f.hasMaxValue())
					local.append("$lt", f.getMaxValue());
				if (f.hasMinValue())
					local.append("$gte", f.getMinValue());

				numFilter.append(f.getKey(), local);
				numFilterList.add(numFilter);
			}
		}
		return numFilterList;
	}

	private static List<BasicDBObject> processTextFilters(List<TextFilter> textFilters) {
		List<BasicDBObject> textFilterList = new ArrayList<>();

		for (TextFilter f : textFilters) {
			BasicDBObject textFilter = new BasicDBObject();
			Object local = null;

			if (f.isRegex())
				local = new BasicDBObject("$regex", f.getValue());
			else
				local = f.getValue();

			textFilter.append(f.getKey(), local);
			textFilterList.add(textFilter);
		}
		return textFilterList;
	}

}
