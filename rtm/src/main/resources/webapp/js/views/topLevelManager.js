
var TopLevelManager = function () {};

$.extend(TopLevelManager.prototype, Backbone.Events, {

	lastSetInterval : '',
	curPayload : '',
	refreshSpeed : 1000,
	isComplete : 'false',
	curStreamId : '',
	timeout : '',

	setRouterReference: function(obj){
		this.router = obj;
	},
	
	start: function () {

		this.activeContext = '';
		this.dynamicViewsManager = [];

		this.timeout = Config.getProperty('aggregateService.defaultStreamTimeoutSecs');
		
		// collections
		this.measurements = new Measurements();
		this.aggregates = new Aggregates();
		this.aggregateDatapoints = new AggregateDatapoints();

		// These views are static to the context :
		this.navbarView = new NavBarView();
		this.mainHeaderView = new MainHeaderView();

		// These views are dynamic to the context :
		this.measurementListView = new MeasurementListView({collection : this.measurements});
		//this.dynamicViewsManager.push({view : this.measurementListView, contextRelevancy : ['Measurement']});
		this.aggregateChartView = new AggregateChartView({collection : this.aggregateDatapoints});
		this.aggregateTableView = new AggregateTableView({collection : this.aggregateDatapoints});
		//this.dynamicViewsManager.push({view : this.aggregateListView, contextRelevancy : ['Measurement']});

		this.aggSPControllerView = new AggSPControllerView();
		this.postControllerView = new PostControllerView();

		this.listenTo( this.postControllerView, 'globalSearchEvent', this.dispatchTopLevelSearch );
		this.listenTo( this.postControllerView, 'pauseEvent', this.dispatchPause );
		this.listenTo( this.postControllerView, 'resumeEvent', this.dispatchResume );
		this.listenTo( this.aggSPControllerView, 'globalSearchEvent', this.dispatchTopLevelSearch );
		this.listenTo( this.measurements, 'MeasurementsRefreshed', this.dispatchMeasurementsRefreshed );
		this.listenTo( this.aggregates, 'AggregatesRefreshed', this.dispatchAggregatesRefreshed );
		this.listenTo( this.aggregateDatapoints, 'streamConsumed', this.dispatchStreamConsumed );
		this.listenTo( this.aggregateDatapoints, 'pauseChartTimer', this.dispatchPause );
		this.listenTo( this.aggregateDatapoints, 'AggregateDatapointsRefreshed', this.dispatchAggregateDatapointsRefreshed );
		this.listenTo( this.measurementListView, 'MeasurementPrevious', this.sendSearch );
		this.listenTo( this.measurementListView, 'MeasurementNext', this.sendSearch );
	},

//	can just reuse sendSearch?
//	dispatchMeasurementPrevious
//	dispatchMeasurementNext

dispatchStreamConsumed : function(){
		console.log('stream is consumed.');
		this.isComplete = 'true';
		this.clearRefresh();
},
dispatchPause : function(){
		console.log('fetching paused.');
		this.clearRefresh();
},

dispatchResume : function(){
		this.setRefresh();
},

	dispatchMeasurementsRefreshed: function(){
		this.measurementListView.cleanup();
		this.measurementListView.render();
	},

	dispatchAggregatesRefreshed: function(){
		var that = this;
		var streamId = this.aggregates.models[0].get('payload');
		this.curStreamId = streamId;
		this.refreshStart = Date.now();
		this.maxDate = this.refreshStart + (parseInt(this.timeout) * 1000);
		this.setRefresh();
	},
	
	setRefresh : function(){
		var that = this;
		this.lastSetInterval = setInterval( function() {
			if(Date.now() < that.maxDate){ 
				that.aggregateDatapoints.refreshData(that.curStreamId);
			} else {
				console.log('stream timed out.');
				that.clearRefresh();
			}
		}, that.refreshSpeed );
	},
	
	clearRefresh : function(){
		clearInterval(this.lastSetInterval);
	},
	
	dispatchAggregateDatapointsRefreshed: function(){
	
		this.aggregateChartView.cleanupChartOnly();
		this.aggregateChartView.renderChartOnly();
		// if Table turned on
		this.aggregateTableView.cleanupTableOnly();
		this.aggregateTableView.renderTableOnly();
	},
	
	serializeInput: function(){
		
		var selPayload = this.postControllerView.getServiceFragment();

		var serviceParams = new ServiceParams();
		
		serviceParams.setFragment(this.measurementListView.getServiceDomain(), this.measurementListView.getServiceFragment()); // toSkipValue()
		serviceParams.setFragment(this.aggSPControllerView.getServiceDomain(), this.aggSPControllerView.getServiceFragment());

		var serviceInput = new ServiceInput();
		serviceInput.setSelectors(selPayload);
		serviceInput.setServiceParams(serviceParams);
		
		return serviceInput;
	},
	
	serializeGui: function(){

		var guiState = new GuiState();
		
		guiState.setGuiParam(this.postControllerView.getGuiDomain(), this.postControllerView.getGuiFragment());
		guiState.setGuiParam(this.measurementListView.getGuiDomain(), this.measurementListView.getGuiFragment());
		guiState.setGuiParam(this.aggSPControllerView.getGuiDomain(), this.aggSPControllerView.getGuiFragment());
		guiState.setGuiParam(this.aggregateChartView.getGuiDomain(), this.aggregateChartView.getGuiFragment());
		guiState.setGuiParam(this.aggregateTableView.getGuiDomain(), this.aggregateTableView.getGuiFragment());
		
		return guiState;
	},

	setActiveContext: function(context){
		this.activeContext = context;
		this.navbarView.setActiveContext(context);
		this.mainHeaderView.setTitle(context);
	},

	getActiveContext: function(){
		return this.activeContext;
	},

	dispatchTopLevelSearch: function(){
		if(!this.postControllerView.hasValidFilters())
			displayError('filters are empty');
		else{
			this.measurementListView.resetPager();
			this.sendSearch();
		}
	},

	setNewClientTimeout: function(){
		this.timeout = parseInt(this.aggSPControllerView.aggserviceparams.timeout);
	},
	sendSearch: function(){
		this.setNewClientTimeout();
		var guiState = JSON.stringify(this.serializeGui());
		var route = this.activeContext + "/select/"+ encodeURIComponent(guiState) + "/" + Date.now();
		this.router.navigate(route, true);
	},
	renderDefaultViews: function(){
		this.mainHeaderView.render();
		this.navbarView.render();
	},

	renderControllerForMeasurement: function(){
		this.aggSPControllerView.cleanup();
		this.postControllerView.render();
	},

	renderControllerForAggregate: function(){
		this.postControllerView.render();
		this.aggSPControllerView.render();
	},

	cleanupViews: function(){
		$("#errorZone").html('');
		this.measurementListView.cleanup();
		this.postControllerView.cleanup();
		this.aggSPControllerView.cleanup();
		this.aggregateChartView.cleanup();
		this.aggregateTableView.cleanup();
		},

	clearCollections: function(){
		this.measurementListView.clearAll();
		this.aggregateChartView.clearAll();
		this.aggregateTableView.clearAll();
	},

	renderMeasurementViews: function(){
		this.postControllerView.render();
		this.measurementListView.render();
	},

	renderAggregateViews: function(){
		this.postControllerView.render();
		this.aggSPControllerView.render();
		//this.aggregateChartView.render();
		//this.aggregateTableView.render();
		
	},

	loadGuiState: function(guiState){
		$.extend(guiState, guiStateFunctions);
	
		this.postControllerView.loadGuiState(guiState.getGuiParam(this.postControllerView.getGuiDomain()));
		this.aggSPControllerView.loadGuiState(guiState.getGuiParam(this.aggSPControllerView.getGuiDomain()));
		this.measurementListView.loadGuiState(guiState.getGuiParam(this.measurementListView.getGuiDomain()));
		this.aggregateChartView.loadGuiState(guiState.getGuiParam(this.aggregateChartView.getGuiDomain()));
		this.aggregateTableView.loadGuiState(guiState.getGuiParam(this.aggregateTableView.getGuiDomain()));
	},

	refreshMeasurementModel: function(){
		var serviceInput = this.serializeInput();
		serviceInput.setSelectors(guiToBackendInput(serviceInput.getSelectors()));
		this.measurements.refreshData(serviceInput);
	},

	refreshAggregateModel: function(){
		var serviceInput = this.serializeInput();
		serviceInput.setSelectors(guiToBackendInput(serviceInput.getSelectors()));
		
		this.aggregates.refreshData(serviceInput);
	},

	hasControllerData: function(input){
		return this.postControllerView.hasData();
	}

});