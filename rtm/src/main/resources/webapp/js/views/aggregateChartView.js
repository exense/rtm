var AggregateChartView = Backbone.View.extend({
	el: '.ChartView',
	events : {
		"click .metricChoice": "updateChartMetricChoice",
	},

	currentChartMetricChoice: '',
	chartBeginKey : '',
	chartGroupbyKey : '',
	chartMaxSeries : 0,
	chartMaxDotsPerSeries : 0,
	svgLegendFactor : 5,
	seriesCount : 0,

	lastSetInterval : '',
	curPayload : '',
	refreshSpeed : 500,

	initialize : function(){
		this.currentChartMetricChoice = Config.getProperty('client.AggregateChartView.currentChartMetricChoice');
		this.chartBeginKey = Config.getProperty('client.AggregateChartView.chartBeginKey');
		this.chartGroupbyKey = Config.getProperty('client.AggregateChartView.chartGroupbyKey');
		this.chartMaxSeries = Config.getProperty('client.AggregateChartView.chartMaxSeries');
		this.chartMaxDotsPerSeries = Config.getProperty('client.AggregateChartView.chartMaxDotsPerSeries');

	},
	getGuiFragment :function(){
		return {
			'chartMetricChoice' : this.getCurrentChartMetricChoice()
		};
	},

	getServiceFragment :function(){
		return {};
	},

	getGuiDomain: function(){
		return 'aggregateGraphView';
	},

	getServiceDomain: function(){
		return 'aggregateService';
	},

	getCurrentChartMetricChoice: function () {
		return this.currentChartMetricChoice;
	},
	loadGuiState: function (guiParams) {
		this.currentChartMetricChoice = guiParams.chartMetricChoice;
	},
	updateChartMetricChoice: function (e) {
		this.currentChartMetricChoice = e.currentTarget.id;
		this.render();
		e.preventDefault();
	},
	render: function () {

		var that = this;

		this.$el.html('');

		this.renderChart();
	},

	renderChart: function () {

		var that = this;
		if(this.collection.models.length > 0){

		this.curPayload = this.collection.models[0].get('payload');

		console.log();

		$.get(resolveTemplate('aggregateChart-template'), function (data) {
			template = _.template(data, {metricsList : that.getChartableMetricsList(), currentChartMetricChoice : that.currentChartMetricChoice, nbSeries : that.seriesCount, factor : that.svgLegendFactor});
			that.$el.append(template);
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
		.success(function(){
			var pauser = that.pauseChartTimer.bind(that);
			var resumer = that.resume.bind(that);
			$('input[function="pause"]').click(pauser);
			$('input[function="resume"]').click(resumer);
			clearInterval(that.lastSetInterval);
			that.lastSetInterval = setInterval( function() { that.chartTimer(that.curPayload); }, that.refreshSpeed );
		});

	}
},

chartTimer: function(arg1) {
	
	var that = this;
	
	$.ajax({
			type: 'POST',
			url: '/rtm/rest/aggregate/refresh',
			contentType: "application/json",
			data: JSON.stringify(arg1),
			success:function(result){
					if(Object.keys(result).length > 0){
						var chartParams = { metric : 'count'};
						$( "svg" ).empty();
						$("#legendSVG").empty();
						var convertedResult = that.convertToOld(result.payload);
						that.drawD3Chart(convertedResult, chartParams);
					}
				}
			});
},

pauseChartTimer : function(){
	clearInterval(this.lastSetInterval);
},

resume : function(){
	var that = this;
	this.lastSetInterval = setInterval( function() { that.chartTimer(that.curPayload); }, that.refreshSpeed);
},

isNumeric: function(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
},

getChartableMetricsList: function(){
	var metricsList = [];
	//TODO: dynimicize
	metricsList.push('count');
	metricsList.push('sum');
	return metricsList;
},

getMetricsList: function(){
	var metricsList = [];
	var firstModel = this.collection.models[0];
	var excludes = this.getExcludeList();
	if(firstModel){
		for ( var prop in firstModel.attributes.payload[0].data[0]){
			if (firstModel.attributes.payload[0].data[0].hasOwnProperty(prop)) {
				metricsList.push(prop);
    		}
		}
	}
	return metricsList;
},
getExcludeList: function(){ // CONFIGURATIVE
	return this.excludeList;
},

drawD3Chart: function(pAggregates, pChartParams){
	/*drawD3Chart(that.collection.models[0].get('payload'),{
		metric : that.currentChartMetricChoice,
		chartBeginKey : that.chartBeginKey,
		chartGroupbyKey : that.chartGroupbyKey,
		chartMaxSeries : that.chartMaxSeries,
		chartMaxDotsPerSeries : that.chartMaxDotsPerSeries
})*/

var Sdata = this.convertToSeries(pAggregates, pChartParams);
var Tdata = this.convertToTable(pAggregates, pChartParams);

var svg = d3.select("#chartSVG")
					.attr("preserveAspectRatio", "xMinYMin meet")

var margin = {top: 20, right: 90, bottom: 30, left: 50};
var width = $("svg").width() - margin.right - margin.left;
var height = $("svg").height() - margin.top - margin.bottom;

//console.log("width=" + width + "; height=" + height);

var g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var x = d3.scaleTime().range([0, width]),
    y = d3.scaleLinear().range([height, 0]),
    z = d3.scaleOrdinal(d3.schemeCategory10);

var line = d3.line()
    .curve(d3.curveMonotoneX)
    .x(function(d) { return x(d.date); })
    .y(function(d) { return y(d.metricVal); });

  x.domain(d3.extent(Tdata, function(d) { return d.date; }));

  y.domain([
    d3.min(Sdata, function(c) { return d3.min(c.values, function(d) { return d.metricVal; }); }),
    d3.max(Sdata, function(c) { return d3.max(c.values, function(d) { return d.metricVal; }); })
  ]);

  z.domain(Sdata.map(function(c) { return c.id; }));

  g.append("g")
      .attr("class", "axis axis--x")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

  g.append("g")
      .attr("class", "axis axis--y")
      .call(d3.axisLeft(y))
      .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", "0.71em")
      .attr("fill", "#000")
      .text("value");

// Series
  var ser = g.selectAll(".ser")
    				 .data(Sdata)
    		 		 .enter().append("g")
      			 .attr("class", "ser")
						 .attr("active", "false")
						 .attr("id",function(d, i) {
			        	return d.id;
						 });

// Curves
  ser.append("path")
      .attr("class", "line")
      .attr("d", function(d) {
        return line(d.values); })
      	.style("stroke", function(d) { return z(d.id); })
				.attr("originalStyle", function(d) { return z(d.id); })
				.attr("originalWidth", "2")
				.attr("activeWidth", "4");
/*
				console.log("Sdata");
				console.log(JSON.stringify(Sdata));
				console.log("Tdata");
				console.log(JSON.stringify(Tdata));
*/

var tooltipDiv = d3.select(".divToolTip");
	tooltipDiv.style("opacity", 0);
// Dots
g.selectAll("g.dot")
    .data(Sdata)
    .enter().append("g")
    	.attr("class", "dotSer")
    	.selectAll("circle")
    	.data(function(d) { return d.values; })
    	.enter()
				.append("circle")
    		.attr("r", 2)
    		.attr("cx", function(d,i) {  return x(d.date); })
    		.attr("cy", function(d,i) { return y(d.metricVal); })
				.style("stroke", function(d) {
    			return z(this.parentNode.__data__.id);
				})
				.style("stroke-width","2")
				.style("fill","none")
				.attr("class", "dot")
				.attr("date", function(d){
					return getPrintableDate(d.date);
				})
				.attr("serName", function(d){
					return this.parentNode.__data__.id;
				})
				.attr("value", function(d){
					return d.metricVal;
				})
				.on("mouseover", function(d, i, f){
					var ser_g = d3.select(this.parentNode);
					var circ = d3.select(this);
					//console.log("series=" + circ.attr("serName") + "; date=" + circ.attr("date") + "; value=" + circ.attr("value"));

					tooltipDiv.transition()
							.duration(200)
							.style("opacity", .85);
					tooltipDiv.html('<p style="display: inline-block">series='+circ.attr("serName") + '</p><br />'
					 						  + '<p style="display: inline-block">date=' + circ.attr("date") + '</p><br />'
											  + '<p style="display: inline-block">value='+ circ.attr("value") + '</p>')
							.style("left", (d3.event.pageX) + "px")
							.style("top", (d3.event.pageY - 28) + "px");
				})
				.on("mouseout", function(d) {
					tooltipDiv.transition()
					.duration(500)
					.style("opacity", 0);
});;

// Legend

var lsvg = d3.select("#legendSVG"),
    margin = {top: 20, right: 90, bottom: 30, left: 50},
    width = svg.attr("width") - margin.left - margin.right,
    height = svg.attr("height") - margin.top - margin.bottom;

var legPosx = $("#legendSVG").position().left;
var legPosy = $("#legendSVG").position().bottom;

var tnode = lsvg.append("text")
							.attr("id", "legend")
							.attr("x", legPosx + 20 + "px")
							.attr("dy", "20px");

var containerWidth = $(".panel").width();
var tDy = 20;

var ty = 0;
var tDx = (containerWidth -20) / Math.abs(this.svgLegendFactor +1);
//var origin = $("#legendSVG").position().left + 20;
var origin = margin.left + 50;
var tx = origin;
var factor = this.svgLegendFactor;
var that = this;
	var lser = tnode.selectAll(".lser")
								  .data(Sdata)
								  .enter()
									.append("button")
									.attr("class","btn btn-default btn-sm")
									.each(function(d, i){
											var thisSeries = d3.select("#" + d.id);
											var path = thisSeries.select("path");
											var rgbStyle= that.getRGB(path.attr("style"));
											//console.log(rgbStyle);
											d3.select(this).attr("style", "border-width: 2px; border-color: rgb" + rgbStyle +"; display: inline-block; margin-left: 10px; margin-bottom: 5px;");
									})
									.on("click", function(d, i){
										var thisSeries = d3.select("#" + d.id);
										var path = thisSeries.select("path");
										var originalStrokeStyle = path.attr("originalStyle");
										var originalWidth = path.attr("originalWidth");
										var activeWidth = path.attr("activeWidth");
										var newActive;
										if(thisSeries.attr("active") == "true"){
											path.style(originalStrokeStyle);
											path.style("stroke-width", originalWidth);
											newActive = "false";
										}
										if(thisSeries.attr("active") == "false"){
											path.style(originalStrokeStyle);
											path.style("stroke-width", activeWidth);
											newActive = "true";
										}
										thisSeries.attr("active",newActive);
									})
									.text(function(d, i) {
											        return d.id;
														});
},
getRGB: function (cssStyle){
	return rgbSplit = cssStyle.split("rgb")[1].split(";")[0];
},

convertToOld : function(payload){

	  var result = [];
	  var curRow = {};

	  var seriesNb = payload.length;
	  var series = [];
	  var metrics = [];
	  var first = payload[Object.keys(payload)[0]];
	  if(first){
		for(attribute in first){
			series.push(attribute);
		}
		
		for(metric in first[series[0]]){
			//console.log('-->first'); console.log(JSON.stringify(first));console.log('-->series[0]'); console.log(series[0]);console.log('-->metric'); console.log(metric);
			metrics.push(metric)
		}

		//console.log('-->metrics'); console.log(JSON.stringify(metrics));
		
		_.each(series, function(sery){ 
			var seriesData = {"groupby" : sery, "data" : []};
			
			for(dot in payload){
				//console.log(' --> result');	console.log(JSON.stringify(result));
				if(Object.keys(dot).length > 0 ){
					var thisMeasure = {};
					thisMeasure['begin'] = parseInt(dot);
					var complete = true;
					_.each(metrics, function(metric){
						//console.log('-->metric'); console.log(metric); console.log('-->dot'); console.log(dot); console.log('-->sery'); console.log(sery);
						/*if(!payload[dot][sery]){
						console.log(' ---> payload[dot][sery]');console.log(JSON.stringify(payload[dot][sery]));
						console.log(' ---> payload[dot]');console.log(JSON.stringify(payload[dot]));
						console.log(' ---> payload');console.log(JSON.stringify(payload));
						}*/
						if(payload[dot][sery]){
							thisMeasure[metric] = payload[dot][sery][metric];
						}else
						    complete = false;
					});
				
					if(complete){
						seriesData.data.push(thisMeasure);
					}
				}
			}
		result.push(seriesData);
		});
	  }
	  
	  return result;
},

getSdataIndexById: function (serId, Sdata){
	var index = 0;
	var arrayLength = Sdata.length;
	for (var i = 0; i < arrayLength; i++) {
		if(Sdata[i]["id"] == serId){
			return index;
		}else{
			index++;
		}
	}
},

convertToTable: function (payload, pChartParams){

	  var result = [];
	  var curRow = {};

	  var seriesNb = payload.length;
	  var payloadLen = payload[0].data.length;
	  var headers = [];

	  //console.log('seriesNb = ' + seriesNb + '; ' + 'payloadLen = ' + payloadLen + ';');
	  _.each(payload, function(series){
	    //console.log("series =");
	    //console.log(series);
	    headers.push(series.groupby);
	  });
	  /**/

	  for (var i = 0; i < payloadLen; i++) {
	    curRow.date = new Date(payload[0].data[i].begin);
	    _.each(headers, function(h){
	        curRow[h] = +payload[headers.indexOf(h)].data[i][pChartParams.metric];
	    });
	    result.push(curRow);
	    curRow = {};
	  }
	  //console.log('result = ');
	  //console.log(result);

	  return result;
},

convertToSeries: function (payload, pChartParams){


	  var result = [];
	  var curSeries = [];

	  var seriesNb = payload.length;
	  var payloadLen = payload[0].data.length;

	  //console.log('seriesNb = ' + seriesNb + '; ' + 'payloadLen = ' + payloadLen + ';');
	  _.each(payload, function(series){
	      for (var i = 0; i < payloadLen; i++) {
	        curSeries.push({ 'date' : new Date(series.data[i].begin), 'metricVal' : +series.data[i][pChartParams.metric] });
	      }

	      result.push({ 'id' : series.groupby, 'values' : curSeries})
	      curSeries = [];
	  });

	  return result;
},

clearAll: function () {
	this.collection.reset();
}
});
