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
		this.seriesCount = this.collection.models[0].get('payload').length;
		$.get(resolveTemplate('aggregateChart-template'), function (data) {
			template = _.template(data, {metricsList : that.getChartableMetricsList(), currentChartMetricChoice : that.currentChartMetricChoice, nbSeries : that.seriesCount, factor : that.svgLegendFactor});
			that.$el.append(template);
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
		.success(function(){

			if(that.collection.models.length > 0){
				/* // D3 */
				that.drawD3Chart(that.collection.models[0].get('payload'),
				{
					metric : that.currentChartMetricChoice,
					chartBeginKey : that.chartBeginKey,
					chartGroupbyKey : that.chartGroupbyKey,
					chartMaxSeries : that.chartMaxSeries,
					chartMaxDotsPerSeries : that.chartMaxDotsPerSeries
				});
			}
		});
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

var svg = d3.select("#chartSVG"),
    margin = {top: 20, right: 90, bottom: 30, left: 50},
    width = svg.attr("width") - margin.left - margin.right,
    height = svg.attr("height") - margin.top - margin.bottom;

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


  var ser = g.selectAll(".ser")
    				 .data(Sdata)
    		 		 .enter().append("g")
      			 .attr("class", "ser")
						 .attr("active", "false")
						 .attr("id",function(d, i) {
			        	//console.log("iteration " + i + " : " + JSON.stringify(d));
			        	return d.id;
						 });


  ser.append("path")
      .attr("class", "line")
      .attr("d", function(d) {
        //console.log('d = ');
        //console.log(d);
        //console.log('d.values = ');
        //console.log(d.values)
        return line(d.values); })
      	.style("stroke", function(d) { return z(d.id); })
				.attr("originalStyle", function(d) { return z(d.id); })
				.attr("originalWidth", "2")
				.attr("activeWidth", "4");

// Legend
var lsvg = d3.select("#legendSVG"),
    margin = {top: 20, right: 90, bottom: 30, left: 50},
    width = svg.attr("width") - margin.left - margin.right,
    height = svg.attr("height") - margin.top - margin.bottom;

var tnode = lsvg.append("text")
							.attr("id", "legend")
							.attr("x", "50px")
							.attr("y", "20px")
							.attr("text-anchor", "middle");

var containerWidth = $(".panel").width();
var tDy = 20;

var ty = 0;
var tDx = (containerWidth -20) / Math.abs(this.svgLegendFactor +1);
//var origin = $("#legendSVG").position().left + 20;
var origin = margin.left + 50;
var tx = origin;
var factor = this.svgLegendFactor;
	var lser = tnode.selectAll(".lser")
								  .data(Sdata)
								  .enter()
									.append("tspan")
									.attr('x', function(d, i){
										if((i+1) % factor <1)
											tx = origin;
										else {
											tx += tDx;
										}
										return tx + "px";
									})
									.attr('y', function(d, i){
										if(i % factor <1)
											ty+=tDy;
										return ty + "px";
									})
									.on("click", function(d, i){
										var thisSeries = d3.select("#" + d.id);
										var path = thisSeries.select("path");
										var originalStrokeStyle = path.attr("originalStyle");
										var originalWidth = path.attr("originalWidth");
										var activeWidth = path.attr("activeWidth");

										var newActive;
										if(thisSeries.attr("active") == "true"){
											console.log("Active!");
											path.style(originalStrokeStyle);
											path.style("stroke-width", originalWidth);
											newActive = "false";
										}
										if(thisSeries.attr("active") == "false"){
											console.log("Not Active!");
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
