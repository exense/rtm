function AggregateServiceParams(defaults){

	this.timeField =  defaults.defaultTimeField;
	this.timeFormat =  defaults.defaultTimeFormat;
	this.valueField = defaults.defaultValueField;
	this.sessionId =  defaults.defaultSessionId;
	this.granularity = defaults.defaultGranularity;
	this.groupby = defaults.defaultGroupby;
	this.cpu = defaults.defaultCpu;
	this.partition = defaults.defaultPartition;
	this.timeout = defaults.defaultTimeout;
	this.histSize = defaults.defaultHistSize;
	this.histApp = defaults.defaultHistApp;

	this.getTimeField = function(){ return this.timeField;};
	this.getTimeFormat = function(){ return this.timeFormat;};
	this.getValueField = function(){ return this.valueField;};
	this.getSessionId = function(){ return this.sessionId;};
	this.getGranularity = function(){ return this.granularity;};
	this.getGroupby = function(){ return this.groupby;};
	this.getCpu = function(){ return this.cpu;};
	this.getPartition = function(){ return this.partition;};
	this.getTimeout = function(){ return this.timeout;};
	this.getHistSize = function(){ return this.histSize;};
	this.getHistApp = function(){ return this.histApp;};
}
