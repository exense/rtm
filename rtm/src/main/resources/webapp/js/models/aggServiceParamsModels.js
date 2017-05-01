function AggregateServiceParams(defaults){

  this.sessionId =  defaults.defaultSessionId;
  this.granularity = defaults.defaultGranularity;
  this.groupby = defaults.defaultGroupby;
  this.cpu = defaults.defaultCpu;
  this.partition = defaults.defaultPartition;

  this.getSessionId = function(){ return this.sessionId;};
  this.getGranularity = function(){ return this.granularity;};
  this.getGroupby = function(){ return this.groupby;};
  this.getCpu = function(){ return this.cpu;};
  this.getPartition = function(){ return this.partition;};
}
