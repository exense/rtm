
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
		
		this.compares = new Compares();

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
		this.postControllerView = new PostControllerView({context : this.activeContext});

		this.listenTo( this.postControllerView, 'globalSearchEvent', this.dispatchTopLevelSearch );
		this.listenTo( this.postControllerView, 'pauseEvent', this.dispatchPause );
		this.listenTo( this.postControllerView, 'resumeEvent', this.dispatchResume );
		this.listenTo( this.aggSPControllerView, 'globalSearchEvent', this.dispatchTopLevelSearch );
		this.listenTo( this.measurements, 'MeasurementsRefreshed', this.dispatchMeasurementsRefreshed );
		this.listenTo( this.aggregates, 'AggregatesRefreshed', this.dispatchAggregatesRefreshed );
		this.listenTo( this.compares, 'ComparesRefreshed', this.dispatchComparesRefreshed );
		this.listenTo( this.aggregateDatapoints, 'streamConsumed', this.dispatchStreamConsumed );
		this.listenTo( this.aggregateDatapoints, 'pauseChartTimer', this.dispatchPause );
		this.listenTo( this.aggregateDatapoints, 'AggregateDatapointsRefreshed', this.dispatchAggregateDatapointsRefreshed );
		this.listenTo( this.measurementListView, 'MeasurementPrevious', this.sendSearch );
		this.listenTo( this.measurementListView, 'MeasurementNext', this.sendSearch );
		
			// Attempted to set a standard Cookie, but refused
			/*
			var backboneSync = Backbone.sync;
    		Backbone.sync = function (method, model, options) {
        		options.headers = {
            		'STEP-Cookie': Config.getProperty('curCookie')
        		};

        	backboneSync(method, model, options);
    		};*/
	},

dispatchStreamConsumed : function(){
		console.log('stream is consumed.');
		this.postControllerView.stopSpinner();
		this.isComplete = 'true';
		this.clearRefresh();
},
dispatchPause : function(){
		console.log('fetching paused.');
		this.clearRefresh();
},

dispatchResume : function(){
	console.log('fetching resumed.');
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
		
dispatchComparesRefreshed: function(){
		var that = this;
		var streamId = this.compares.models[0].get('payload');
		this.curStreamId = streamId;
//		console.log(streamId);
		this.refreshStart = Date.now();
		this.maxDate = this.refreshStart + (parseInt(this.timeout) * 1000);
		this.setRefresh();
	},
	
	setRefresh : function(){
		var that = this;
		var target = $('#spinner');
		
		this.postControllerView.resetSpinner();
		
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
		console.log('serializeInput')
		console.log(selPayload);
		var serviceParams = new ServiceParams();
		
		serviceParams.setFragment(this.measurementListView.getServiceDomain(), this.measurementListView.getServiceFragment()); // toSkipValue()
		serviceParams.setFragment(this.aggSPControllerView.getServiceDomain(), this.aggSPControllerView.getServiceFragment());

		var serviceInput = new ServiceInput();
		serviceInput.setSelectors(selPayload, this.activeContext);
		serviceInput.setServiceParams(serviceParams);
		
		//console.log('serializeInput');
		//console.log(selPayload);
		//console.log(serviceInput);		
		
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
		this.postControllerView.setContext(context);
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
		serviceInput.setSelectors(guiToBackendInput(serviceInput.getSelectors(this.activeContext)), this.activeContext);
		this.measurements.refreshData(serviceInput);
	},

	refreshAggregateModel: function(){
		var serviceInput = this.serializeInput();
		console.log('refreshAggregateModel');
		console.log(serviceInput);
		serviceInput.setSelectors(guiToBackendInput(serviceInput.getSelectors(this.activeContext)), this.activeContext);
		this.aggregates.refreshData(serviceInput);
	},
	
	refreshCompareModel: function(){
		var serviceInput = this.serializeInput();
		serviceInput.setSelectors(guiToBackendInput(serviceInput.getSelectors(this.activeContext)), this.activeContext);
		this.compares.refreshData(serviceInput);
	},

	hasControllerData: function(input){
		return this.postControllerView.hasData();
	}

});