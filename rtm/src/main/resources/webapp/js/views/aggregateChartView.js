var AggregateChartView = Backbone.View.extend({
	el: '.ChartView',
	events : {
		"click .metricChoice": "updateChartMetricChoice",
	},

	currentChartMetricChoice: '',
	chartBeginKey : '',
	chartGroupbyKey : '',
	svgLegendFactor : 5,
	seriesCount : 0,
	
	initialize : function(){
		this.currentChartMetricChoice = Config.getProperty('client.AggregateChartView.currentChartMetricChoice');
		this.chartBeginKey = Config.getProperty('client.AggregateChartView.chartBeginKey');
		this.chartGroupbyKey = Config.getProperty('client.AggregateChartView.chartGroupbyKey');
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
		
		this.cleanupChartOnly();
		this.renderChartOnly();
		e.preventDefault();
	},
	render: function () {

		var that = this;

		this.$el.html('');

		this.renderTemplate(this.renderChart);
	},

	renderTemplate: function(callback) {
		var that = this;
		var thisCallback = callback.bind(this);
	
		$.get(resolveTemplate('aggregateChart-template'), function (data) {	
			template = _.template(data, {metricsList : that.getChartableMetricsList(), currentChartMetricChoice : that.currentChartMetricChoice, nbSeries : that.seriesCount, factor : that.svgLegendFactor});
			that.$el.append(template);
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
		.success(function(){
			thisCallback();
		});
},

cleanupChartOnly: function() {
  	$( "svg" ).empty();
	$("#legendSVG").empty();
},

renderChartOnly: function(){
			var length = $("#svgContainer").length;
			if(length < 1)
				this.renderTemplate(this.renderChart);
			else
				this.renderChart();
},

renderChart: function(){

			if(this.collection.models[0]){
				var payload = this.collection.models[0].get('payload');
	
				if(payload && Object.keys(payload).length > 0){
							if(payload.stream.streamData && Object.keys(payload.stream.streamData).length > 0){
								var chartParams = { metric : this.currentChartMetricChoice};
								var convertedResult = convertToOld(payload.stream.streamData, this.getChartableMetricsList());
								this.drawD3Chart(convertedResult, chartParams);
							}
				}
			}
},

isNumeric: function(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
},

getChartableMetricsList: function(){
	return this.collection.models[0].get('payload').metricList;
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
		chartGroupbyKey : that.chartGroupbyKey
})*/

var that = this;
var Sdata = this.convertToSeries(pAggregates, pChartParams);
var Tdata = this.convertToTable(pAggregates, pChartParams);

var svg = d3.select("#chartSVG")
					.attr("preserveAspectRatio", "xMinYMin meet")

//var wMargin = $("svg").width() / 8;  
//var hMargin = $("svg").height() / 6;
var wMargin = 100;  
var hMargin = 50; 
var width = $("svg").width() - wMargin;
var height = $("svg").height() - hMargin;

var g = svg.append("g").attr("transform", "translate(" + 80 + "," + 20 + ")");

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
											  + '<p style="display: inline-block">' + that.currentChartMetricChoice+'='+ circ.attr("value") + '</p>')
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
									  var thisSeries = d3.select("#" + CSS.escape(d.id.toString()));
									  var path = thisSeries.select("path");
									  var rgbStyle= that.getRGB(path.attr("style"));
									  d3.select(this).attr("style", "border-width: 2px; border-color: " + rgbStyle +"; display: inline-block; margin-left: 10px; margin-bottom: 5px;");
									})
									.on("click", function(d, i){
										var thisSeries = d3.select("#" + CSS.escape(d.id.toString()));
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
	if(cssStyle.indexOf('rgb') >= 0)
		return rgbSplit = 'rgb'+cssStyle.split("rgb")[1].split(";")[0];
	else{
		if(cssStyle.indexOf('#') >= 0)
			return '#' + cssStyle.split("#")[1].split(";")[0];
		else{
			console.log('unknown RGB pattern.');
			return null;
		}
	}
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

	  //console.log(payload);
	  
	  var beginArray = this.getBeginArray(payload);
	  var payloadLen = beginArray.length;
	  var headers = [];

	  _.each(payload, function(series){
	    headers.push(series.groupby);
	  });

	  for (var i = 0; i < payloadLen; i++) {
		curRow.date = new Date(beginArray[i]);
	    _.each(headers, function(h){
	    	var index = headers.indexOf(h);
	    	if(payload[index].data[i])
	    		curRow[h] = +payload[index].data[i][pChartParams.metric];
	    });
	    result.push(curRow);
	    curRow = {};
	  }
	  return result;
},

getBeginArray: function (payload){
	var bArray = [];
	_.each(payload, function(series){
		_.each(series.data, function(datapoint){
			if(!_.contains(bArray, datapoint.begin))
				bArray.push(datapoint.begin);
		});
	});
	return bArray.sort();
},

convertToSeries: function (payload, pChartParams){


	  var result = [];
	  var curSeries = [];

	  var seriesNb = payload.length;

	  _.each(payload, function(series){
		  var payloadLen = series.data.length;
	      for (var i = 0; i < payloadLen; i++) {
	    	  if(series.data[i])
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
