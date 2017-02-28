package org.rtm.pipeline.seh.builders;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.execute.task.RangeTask;
import org.rtm.pipeline.execute.task.MongoQueryTask;
import org.rtm.request.selection.Selector;

public class SimpleMongoBuilder extends PartitionedBuilder {

	private List<Selector> selectors;
	private Properties prop; 
	
	public SimpleMongoBuilder(Long start, Long end, Long increment, List<Selector> selectors, Properties prop){
		super(start, end, increment);
		this.selectors = selectors;
		this.prop = prop;
	}

	@Override
	protected RangeTask createTask() {
		return new MongoQueryTask(this.selectors, this.prop);
	}

}
