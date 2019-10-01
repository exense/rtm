function PerformanceDashboard() {

	var widgetsArray = [];

	addLastMeasurements(widgetsArray);	
	//addLastMeasurementsTpl(widgetsArray);
	//addAggregatesOverTime(widgetsArray);
	
	var dashboardObject = new Dashboard(widgetsArray, 'Perf dashboard', new DefaultMgrState());
	dashboardObject.oid = "perfDashboardId";
	return dashboardObject;
};

var getMasterSlaveConfig = function(rawOrTransformed, masterTitle, slaveTitle){
	var masterId, slaveId, masterTitle, slaveTitle, masterConfig, slaveConfig, datatype;
	
	if(rawOrTransformed === 'raw'){
		datatype = 'state.data.rawresponse';
	}else{
		datatype = 'state.data.transformed';
	}
	
	var random = getUniqueId();
	masterId = random + "-master";
	slaveId = random + "-slave";

	masterConfig = new Config('On', true, false, 'unnecessaryAsMaster');
	slaveConfig = new Config('Off', false, true, datatype);
	slaveConfig.currentmaster = {
			oid: masterId,
			title: masterTitle
	};

	return {masterid: masterId, slaveid: slaveId, mastertitle: masterTitle, slavetitle: slaveTitle, masterconfig : masterConfig, slaveconfig: slaveConfig};
};

function RTMLatestMeasurementBaseQuery(){
	return new SimpleQuery(
			"Raw", new Service(
					"/rtm/rest/measurement/latest", "Post",
					"{\"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \".*\", \"regex\": \"true\" }], \"numericalFilters\": [] }],\"serviceParams\": { \"measurementService.nextFactor\": \"100\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" }\}",
					new Preproc("data", ""), new Postproc("", "function (response, args) {\r\n    var x = 'begin', y = 'value', z = 'name';\r\n    var retData = [], index = {};\r\n    var payload = response.data.payload;\r\n    for (var i = 0; i < payload.length; i++) {\r\n        retData.push({\r\n            x: payload[i][x],\r\n            y: payload[i][y],\r\n            z: payload[i][z]\r\n        });\r\n    }\r\n    return retData;\r\n}",
							[], {}, "")
			)
	);
};

var addLastMeasurementsTpl = function(widgetsArray){
	function RTMLatestMeasurementTemplatedQuery(){
		return new TemplatedQuery(
				new RTMLatestMeasurementBaseQuery(),
				new DefaultOffPaging(),
				//new Paging("On", new Offset("__FACTOR__", "return 0;", "return value + 1;", "if(value > 0){return value - 1;} else{return 0;}"), null),
				new TemplateControls("{ \"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \"__eId__\", \"regex\": \"true\" }, { \"key\": \"name\", \"value\": \"__name__\", \"regex\": \"true\" }], \"numericalFilters\": [{\"key\":\"value\",\"minValue\":\"__minValue__\",\"maxValue\":\"__maxValue__\"}] }], \"serviceParams\": { \"measurementService.nextFactor\": \"__FACTOR__\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" } }",
						null,
						[new Placeholder("__eId__", ".*", false)]));
	};
	
	var config = getMasterSlaveConfig("raw", "Last 100 Measurements - Scattered values (ms)", "Last 100 Measurements - Value table (ms)");

	var latestMaster = new Widget(config.masterid,'col-md-6', new DashletState(config.masterTitle, false, 0, {}, new ChartOptions('scatterChart'), config.masterconfig, new RTMLatestMeasurementTemplatedQuery()) );
	var latestSlave = new Widget(config.slaveid,'col-md-6', new DashletState(config.slaveTitle, false, 0, {}, new ChartOptions('table'), config.slaveconfig, new RTMLatestMeasurementQuery()) );

	widgetsArray.push(latestMaster);
	widgetsArray.push(latestSlave);
};

var addAggregatesOverTime = function(widgetsArray){
	var config = getMasterSlaveConfig("raw", "Average Response Time over time (ms)", "Average Transaction count over time (#)");
};

var addLastMeasurements = function(widgetsArray){
	var config = getMasterSlaveConfig("raw", "Last 100 Measurements - Scattered values (ms)", "Last 100 Measurements - Value table (ms)");

	var latestMaster = new Widget(config.masterid,'col-md-6', new DashletState(config.masterTitle, false, 0, {}, new ChartOptions('scatterChart'), config.masterconfig, new RTMLatestMeasurementBaseQuery()) );
	var latestSlave = new Widget(config.slaveid,'col-md-6', new DashletState(config.slaveTitle, false, 0, {}, new ChartOptions('table'), config.slaveconfig, new RTMLatestMeasurementBaseQuery()) );

	widgetsArray.push(latestMaster);
	widgetsArray.push(latestSlave);
};

function StaticPresets() {
	return {
		queries: [
			{
				"name": "RTM-Msmt-Browse",
				"query": {
					"inputtype": "Raw",
					"type": "Simple",
					"datasource": {
						"service": {
							"url": "/rtm/rest/measurement/find",
							"method": "Post",
							"data": "{\
								\"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \".*\", \"regex\": \"true\" }], \"numericalFilters\": [] }],\
								\"serviceParams\": { \"measurementService.nextFactor\": \"0\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" }\
								}",
								"postproc": {
									"transform": {
										"function": "function (response) {\r\n    var x = 'begin', y = 'value', z = 'name';\r\n    var retData = [], index = {};\r\n    var payload = response.data.payload;\r\n    for (var i = 0; i < payload.length; i++) {\r\n        retData.push({\r\n            x: payload[i][x],\r\n            y: payload[i][y],\r\n            z: payload[i][z]\r\n        });\r\n    }\r\n    return retData;\r\n}",
										"abs": { "title": "time", "unit": "seconds" }, "ord": { "title": "duration", "unit": "ms" },
										"transformations": [{ "path": "timestamp", "function": "function () {Math.random().toString(36).substr(2, 9);}" }]
									}
								}
						}
					}
				}
			},
			{
				"name": "RTM-Msmt-Latest",
				"query": {
					"inputtype": "Raw",
					"type": "Simple",
					"datasource": {
						"service": {
							"url": "/rtm/rest/measurement/latest",
							"method": "Post",
							"data": "{\
								\"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \".*\", \"regex\": \"true\" }], \"numericalFilters\": [] }],\
								\"serviceParams\": { \"measurementService.nextFactor\": \"100\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" }\
								}",
								"postproc": {
									"transform": {
										"function": "function (response) {\r\n    var x = 'begin', y = 'value', z = 'name';\r\n    var retData = [], index = {};\r\n    var payload = response.data.payload;\r\n    for (var i = 0; i < payload.length; i++) {\r\n        retData.push({\r\n            x: payload[i][x],\r\n            y: payload[i][y],\r\n            z: payload[i][z]\r\n        });\r\n    }\r\n    return retData;\r\n}",
										"args": []}
								}
						}
					}
				}
			},
			{
				"name": "RTM-Agg-Realtime",
				"query": {
					"inputtype": "Raw",
					"type": "Async",
					"datasource": {
						"service": {
							"url": "/rtm/rest/aggregate/get",
							"method": "Post",
							"data": "{\
								\"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \".*\", \"regex\": \"true\" }], \"numericalFilters\": [] }],\
								\"serviceParams\": { \"measurementService.nextFactor\": \"0\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" }\
								}",
								"postproc": {
									"save": {
										"function": "function(response){return [{ placeholder : '__streamedSessionId__', value : response.data.payload.streamedSessionId, isDynamic : false }];}",
									}
								}
						},
						"callback": {
							"url": "/rtm/rest/aggregate/refresh",
							"method": "Post",
							"data": "{\"streamedSessionId\": \"__streamedSessionId__\"}",
							"preproc": {
								"replace": {
									"target": "data",
									"function": "function(requestFragment, workData){var newRequestFragment = requestFragment;for(i=0;i<workData.length;i++){newRequestFragment = newRequestFragment.replace(workData[i].placeholder, workData[i].value);}return newRequestFragment;}",
								}
							},
							"postproc": {
								"asyncEnd": {
									"function": "function(response){return response.data.payload.stream.complete;}",
								},
								"transform": {
									"function": "function (response) {\r\n    var metric = 'avg';\r\n    var retData = [], series = {};\r\n\r\n    var payload = response.data.payload.stream.streamData;\r\n    var payloadKeys = Object.keys(payload);\r\n\r\n    for (i = 0; i < payloadKeys.length; i++) {\r\n        var serieskeys = Object.keys(payload[payloadKeys[i]])\r\n        for (j = 0; j < serieskeys.length; j++) {\r\n            retData.push({\r\n                x: payloadKeys[i],\r\n                y: payload[payloadKeys[i]][serieskeys[j]][metric],\r\n                z: serieskeys[j]\r\n            });\r\n        }\r\n    }\r\n    return retData;\r\n}",
								}
							}
						}
					}
				}
			},
			{
				"name": "RTM-Agg-Summary",
				"query": {
					"inputtype": "Raw",
					"type": "Async",
					"datasource": {
						"service": {
							"url": "/rtm/rest/aggregate/get",
							"method": "Post",
							"data": "{\
								\"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \".*\", \"regex\": \"true\" }], \"numericalFilters\": [] }],\
								\"serviceParams\": { \"measurementService.nextFactor\": \"0\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"max\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" }\
								}",
								"postproc": {
									"save": {
										"function": "function(response){return [{ placeholder : '__streamedSessionId__', value : response.data.payload.streamedSessionId, isDynamic : false }];}",
									}
								}
						},
						"callback": {
							"url": "/rtm/rest/aggregate/refresh",
							"method": "Post",
							"data": "{\"streamedSessionId\": \"__streamedSessionId__\"}",
							"preproc": {
								"replace": {
									"target": "data",
									"function": "function(requestFragment, workData){var newRequestFragment = requestFragment;for(i=0;i<workData.length;i++){newRequestFragment = newRequestFragment.replace(workData[i].placeholder, workData[i].value);}return newRequestFragment;}",
								}
							},
							"postproc": {
								"asyncEnd": {
									"function": "function(response){return response.data.payload.stream.complete;}",
								},
								"transform": {
									"function": "function (response) {\r\n    var metric = 'avg';\r\n    var retData = [], series = {};\r\n\r\n    var payload = response.data.payload.stream.streamData;\r\n    var payloadKeys = Object.keys(payload);\r\n\r\n    for (i = 0; i < payloadKeys.length; i++) {\r\n        var serieskeys = Object.keys(payload[payloadKeys[i]])\r\n        for (j = 0; j < serieskeys.length; j++) {\r\n            retData.push({\r\n                x: payloadKeys[i],\r\n                y: payload[payloadKeys[i]][serieskeys[j]][metric],\r\n                z: serieskeys[j]\r\n            });\r\n        }\r\n    }\r\n    return retData;\r\n}",
								}
							}
						}
					}
				}
			}
			],
			controls: {
				templates: [
					{
						"name": 'RTM Measurements Template',
						"placeholders": [{ "key": "__eId__", "value": ".*", "isDynamic": false }, { "key": "__name__", "value": ".*", "isDynamic": false }, { "key": "__minValue__", "value": "0", "isDynamic": false }, { "key": "__maxValue__", "value": "999999999", "isDynamic": false }],
						"templatedPayload": "{ \"selectors1\": [{ \"textFilters\": [{ \"key\": \"eId\", \"value\": \"__eId__\", \"regex\": \"true\" }, { \"key\": \"name\", \"value\": \"__name__\", \"regex\": \"true\" }], \"numericalFilters\": [{\"key\":\"value\",\"minValue\":\"__minValue__\",\"maxValue\":\"__maxValue__\"}] }], \"serviceParams\": { \"measurementService.nextFactor\": \"__FACTOR__\", \"aggregateService.sessionId\": \"defaultSid\", \"aggregateService.granularity\": \"auto\", \"aggregateService.groupby\": \"name\", \"aggregateService.cpu\": \"1\", \"aggregateService.partition\": \"8\", \"aggregateService.timeout\": \"600\" } }",
						"templatedParams": "",
						"queryTemplate": {
							"inputtype": "Controls",
							"controltype": "Template",
							"type": "Simple",
							"datasource": {
								"service": {
									"url": "/rtm/rest/measurement/find",
									"method": "Post",
									"data": "",
									"params": "",
									"preproc": {
										"replace": {
											"target": "data",
											"function": "function(requestFragment, workData){var newRequestFragment = requestFragment;for(i=0;i<workData.length;i++){newRequestFragment = newRequestFragment.replace(workData[i].key, workData[i].value);}return newRequestFragment;}",
										}
									},
									"postproc": {
										"transform": {
											"function": "function (response) {\r\n    var x = 'begin', y = 'value', z = 'name';\r\n    var retData = [], index = {};\r\n    var payload = response.data.payload;\r\n    for (var i = 0; i < payload.length; i++) {\r\n        retData.push({\r\n            x: payload[i][x],\r\n            y: payload[i][y],\r\n            z: payload[i][z]\r\n        });\r\n    }\r\n    return retData;\r\n}",
										}
									}
								}
							},
							"paged": {
								"ispaged": "On",
								"offsets": {
									"first": { "vid": "__FACTOR__", "start": "return 0;", "next": "return value + 1;", "previous": "if(value > 0){return value - 1;} else{return 0;}" },
									"second": {}
								}
							}
						}
					}
					]
			},
			configs: [
				{
					name: 'defaultConfig',
					display: {
						type: 'lineChart',
						autorefresh: 'Off'
					}
				}
				]
	};
}