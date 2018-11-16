package org.rtm.pipeline.builders.query;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.builders.task.RangeTaskBuilder;
import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.pipeline.commons.tasks.RemoteQueryTask;
import org.rtm.selection.Selector;

public class RemoteQueryTaskBuilder implements RangeTaskBuilder{
	private List<Selector> selectors;
	private Properties prop;

	public RemoteQueryTaskBuilder(List<Selector> selectors, Properties prop){
		this.selectors = selectors;
		this.prop = prop;
	}
	
	@Override
	public RangeTask build() {
		return new RemoteQueryTask(this.selectors, this.prop);
	}

}
