package org.rtm.pipeline.seh.builders;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.execute.task.IterableTask;
import org.rtm.pipeline.execute.task.MongoQueryTask;
import org.rtm.request.selection.Selector;

public class SingleLevelMongoBuilder extends SingleLevelPartitionedBuilder {

	private List<Selector> selectors;
	
	public SingleLevelMongoBuilder(
			Long start, Long end, Long increment,
			List<Selector> selectors,
			Properties prop){
		super(start, end, increment, prop);
		this.selectors = selectors;
	}

	@Override
	protected IterableTask createTask() {
		return new MongoQueryTask(this.selectors);
	}

}
