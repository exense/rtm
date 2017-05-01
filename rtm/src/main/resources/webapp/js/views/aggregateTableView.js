var AggregateTableView = Backbone.View.extend({
	el: '.TableView',
	events : {
		"click .mlCheckbox": "updateTableMetricChoice",
		"click .displayTable": "switchTable"
	},

	checkedAggTableMetrics: [],
	dateMetrics : [],
	excludeList : [],
	switchedOn: '',
	templateContent : '',
	
	initialize : function(){
		
		var splitter = Config.getProperty('client.splitChar');
		
		this.checkedAggTableMetrics =  Config.getProperty('client.AggregateTableView.checkedAggTableMetrics').split(splitter);
		this.dateMetrics =  Config.getProperty('client.AggregateTableView.dateMetrics').split(splitter);
		this.excludeList =  Config.getProperty('client.AggregateTableView.excludeList').split(splitter);
		this.switchedOn =  Config.getProperty('client.AggregateTableView.switchedOn');
		
		var that = this;
		$.get(resolveTemplate('aggregateTableData-template'), function (data) {
			that.templateContent = data;
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
	},
	
	switchTable:function(e){
		if(this.switchedOn === 'false'){
			this.switchedOn = 'true';
		}else{
			this.switchedOn = 'false';
		}
		this.render();
	},
	getGuiFragment :function(){
		return {
			'checkedAggTableMetrics' : this.getCurrentTableMetricChoices(),
			'isSwitchedOn' : this.switchedOn
		};
	},

	getServiceFragment :function(){
		return {};
	},

	getGuiDomain: function(){
		return 'aggregateTableView';
	},

	getServiceDomain: function(){
		return 'aggregateService';
	},

	getCurrentTableMetricChoices: function () {
		return this.checkedAggTableMetrics;
	},
	loadGuiState: function (guiParams) {
		this.checkedAggTableMetrics = guiParams.checkedAggTableMetrics;
		this.switchedOn = guiParams.isSwitchedOn;
	},
	updateTableMetricChoice: function (e) {
		var choice = e.currentTarget.id;
		var index = $.inArray(choice, this.checkedAggTableMetrics);
		if(index < 0){
			this.checkedAggTableMetrics.push(choice);
		}else{
			this.checkedAggTableMetrics.splice(index,1);
		}
		this.cleanupTableOnly();
		this.renderTableOnly();
		e.preventDefault();
	},
	render: function () {
		var that = this;

		this.$el.html('');

		this.renderTemplate(this.renderTable);
	},

	renderTemplate: function (callback) {
	
		var that = this;
		var thisCallback = callback.bind(this);
		
		$.get(resolveTemplate('aggregateTableHeader-template'), function (data) {

			template = _.template(data, 
					{
						aggregates: that.collection.models, 
						metricsList : that.getMetricsList(), 
						checkedAggTableMetrics : that.getCurrentTableMetricChoices(),
						dateMetric : that.dateMetrics,
						displayTable : that.switchedOn
					});
			that.$el.append(template);
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
		.success(function(){
			thisCallback();	
		});
	},

cleanupTableOnly: function() {
	// No need to clean up manually since the template gets re-rendered entirely
	//$("#collapseATable").empty();
},

renderTableOnly: function(){
			var length = $("#dataPanelContainer").length;
			if(length < 1)
				this.renderTemplate(this.renderTable);
			else
				this.renderTable();
},

renderTable: function(){


	var that = this;
	if(that.collection.models.length > 0 && that.collection.models[0].get('payload')){
		
			template = _.template(that.templateContent, 
					{
						aggregates: convertToOld(that.collection.models[0].get('payload').streamData), 
						metricsList : that.getMetricsList(), 
						checkedAggTableMetrics : that.getCurrentTableMetricChoices(),
						dateMetric : that.dateMetrics,
						displayTable : that.switchedOn
					});
			$("#dataPanelContainer").html(template);
	}
},
	
isNumeric: function(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
},
	
	
getChartableMetricsList: function(){
	var metricsList = [];
	var firstModel = this.collection.models[0];
	var excludes = this.getExcludeList();
	if(firstModel){
		for ( var prop in firstModel.attributes.payload[0].data[0]){
				if (firstModel.attributes.payload[0].data[0].hasOwnProperty(prop)) {
        		if(this.isNumeric(firstModel.attributes.payload[0].data[0][prop])){
					metricsList.push(prop);
    			}
    		}
		}
	}
	
	return metricsList;
},

getMetricsList: function(){
	var metricsList = [];

	var payload = this.collection.models[0].get('payload');
	
	var excludes = this.getExcludeList();
	
	if(payload.streamData && Object.keys(payload.streamData).length > 0){
		var first = payload.streamData[Object.keys(payload.streamData)[0]];
		var obj = first[Object.keys(first)[0]];
		for ( var prop in obj){
			metricsList.push(prop);
		}
	}

	return metricsList;
},

getExcludeList: function(){ // CONFIGURATIVE
		return this.excludeList;
	},
	clearAll: function () {
		this.collection.reset();
	}
});
