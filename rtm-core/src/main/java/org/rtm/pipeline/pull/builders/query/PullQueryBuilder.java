package org.rtm.pipeline.pull.builders.query;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.pipeline.commons.tasks.distributed.DistributedQueryTask;
import org.rtm.pipeline.pull.builders.task.PullTaskBuilder;
import org.rtm.selection.Selector;

public class PullQueryBuilder implements PullTaskBuilder{
	private List<Selector> selectors;
	private Properties prop;

	public PullQueryBuilder(List<Selector> selectors, Properties prop){
		this.selectors = selectors;
		this.prop = prop;
	}
	
	@Override
	public RangeTask build() {
		return new DistributedQueryTask(this.selectors, this.prop);
	}

}
