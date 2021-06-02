package org.rtm.db;

import java.util.ArrayList;
import java.util.List;

import step.core.collections.Filters;
import step.core.collections.Filter;
import org.rtm.request.selection.NumericalFilter;
import org.rtm.request.selection.Selector;
import org.rtm.request.selection.TextFilter;

public class FilterQuery {

	private String timeField;
	private String timeFormat;
	private Filter query;

	public FilterQuery() {}

	public FilterQuery(List<Selector> sel, String timeField, String timeFormat) {
		this.timeFormat = timeFormat;
		this.timeField = timeField;
		this.query = selectorsToFilter(sel);
	}

	public Filter getQuery() {
		return query;
	}

	public void setQuery(Filter query) {
		this.query = query;
	}

	public Filter selectorsToFilter(List<Selector> selectors) {
		Filter top = Filters.empty();

		List<Filter> dbFilterList = new ArrayList<>();
		List<Filter> dbSelectorList = new ArrayList<>();
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
				dbSelectorList.add(Filters.and(dbFilterList));
			dbFilterList = new ArrayList<>();
		}
		if(dbSelectorList.size()>0)
			top = Filters.or(dbSelectorList);
		return top;
	}

	private List<Filter> processNumFilters(List<NumericalFilter> numericalFilters) {
		List<Filter> numFilterList = new ArrayList<>();

		for (NumericalFilter f : numericalFilters) {
			String key = f.getKey();

			if (f.hasMaxValue() || f.hasMinValue()) {
				if (f.hasMaxValue()) {
					if(key.equals(this.timeField) && this.timeFormat.equals("date")) {
						throw new RuntimeException("Date format is not supported");					
					}else {
						numFilterList.add(Filters.lt(key,f.getMaxValue()));
					}
				}
				if (f.hasMinValue()) {
					if(key.equals(this.timeField) && this.timeFormat.equals("date")) {
						throw new RuntimeException("Date format is not supported");
					}else {
						numFilterList.add(Filters.gte(key,f.getMinValue()));
					}
				}
			}
		}
		return numFilterList;
	}

	private List<Filter> processTextFilters(List<TextFilter> textFilters) {
		List<Filter> textFilterList = new ArrayList<>();
		for (TextFilter f : textFilters) {
			if (f.isRegex()) {
				textFilterList.add(Filters.regex(f.getKey(),f.getValue(),false));
			} else {
				textFilterList.add(Filters.equals(f.getKey(),f.getValue()));
			}
		}
		return textFilterList;
	}

}
