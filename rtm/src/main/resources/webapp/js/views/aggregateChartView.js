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
		$.get(resolveTemplate('aggregateChart-template'), function (data) {
			template = _.template(data, {metricsList : that.getChartableMetricsList(), currentChartMetricChoice : that.currentChartMetricChoice});
			that.$el.append(template);
		}, 'html')
		.fail(function(model, response, options ) {
			displayError('response=' + JSON.stringify(response));
		})
		.success(function(){

			if(that.collection.models.length > 0){
				that.drawD3Chart(that.collection.models[0].get('payload'),
				{
					metric : that.currentChartMetricChoice,
					chartBeginKey : that.chartBeginKey,
					chartGroupbyKey : that.chartGroupbyKey,
					chartMaxSeries : that.chartMaxSeries,
					chartMaxDotsPerSeries : that.chartMaxDotsPerSeries
				});
	
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
	/*
	drawD3Chart(that.collection.models[0].get('payload'),{
	metric : that.currentChartMetricChoice,
	chartBeginKey : that.chartBeginKey,
	chartGroupbyKey : that.chartGroupbyKey,
	chartMaxSeries : that.chartMaxSeries,
	chartMaxDotsPerSeries : that.chartMaxDotsPerSeries
})
*/


var Sdata = this.convertToSeries(pAggregates, pChartParams);
var Tdata = this.convertToTable(pAggregates, pChartParams);


var svg = d3.select("svg"),
    margin = {top: 20, right: 90, bottom: 30, left: 50},
    width = svg.attr("width") - margin.left - margin.right,
    height = svg.attr("height") - margin.top - margin.bottom,
    g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var x = d3.scaleTime().range([0, width]),
    y = d3.scaleLinear().range([height, 0]),
    z = d3.scaleOrdinal(d3.schemeCategory10);

var line = d3.line()
    .curve(d3.curveBasis)
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
      .attr("class", "ser");

//console.log(ser);

  ser.append("path")
      .attr("class", "line")
      .attr("d", function(d) {
        //console.log('d = ');
        //console.log(d);
        //console.log('d.values = ');
        //console.log(d.values)
        return line(d.values); })
      .style("stroke", function(d) { return z(d.id); });

  ser.append("text")
      .datum(function(d) { return {id: d.id, value: d.values[d.values.length - 1]}; })
      .attr("transform", function(d) {
        //console.log(d.value);
        return "translate(" + x(d.value.date) + "," + y(d.value.metricVal) + ")"; })
      .attr("x", 3)
      .attr("dy", "0.35em")
      .style("font", "10px sans-serif")
      .text(function(d) { return d.id; });

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

drawChart: function(pAggregates, pChartParams){
	var payloadLength = pAggregates.length;
	var chartData;
	var isChartMaxSeriesReached = false;
	var isChartMaxDotsPerSeriesReached = false;
	if(payloadLength > 0) {

		// calculate number of aggregates in the series (must match)
		var dataLength = pAggregates[0].data.length;
		var headers = [];
		var index = 0;
		_.each(pAggregates, function(aggregate) {
			// pick up groupby values as column headers
			if(headers.length < pChartParams.chartMaxSeries){
				headers.push(aggregate[pChartParams.chartGroupbyKey]);
			}else{
				isChartMaxSeriesReached = true;
			}
			var curDataLength = aggregate.data.length;
			if(curDataLength !== dataLength) {
				displayError("Inconsistent data, cannot be charted.");
			}
			index = index + 1;
		});

		chartData = new google.visualization.DataTable();
		chartData.addColumn('datetime', 'time');
		var hLength = headers.length;
		for (var i = 0; i < hLength; i++) {
			chartData.addColumn('number', headers[i]);
		}

		var dataArray = [];
		var rowArray = [];
		var metric = pChartParams.metric;
		for (var i = 0; i < dataLength; i++) {

			if(dataArray.length >= pChartParams.chartMaxDotsPerSeries){
				isChartMaxDotsPerSeriesReached = true;
				break;
			}
			rowArray.push(new Date(new Number(pAggregates[0].data[i][pChartParams.chartBeginKey])));
			var limit = 0;
			_.each(pAggregates, function(aggregate) {
				if(limit < pChartParams.chartMaxSeries){
					rowArray.push(aggregate.data[i][metric]);
					limit = limit + 1;
				}else{
					isChartMaxSeriesReached = true;
				}
			});
			dataArray.push(rowArray);
			rowArray = [];
		}
		chartData.addRows(dataArray);
	}
	if(isChartMaxSeriesReached)
	displayWarning('The maximum number of displayable chart series in the Chart view (' +this.chartMaxSeries+ ') has been reached, but the Table view is not affected.');
	if(  isChartMaxDotsPerSeriesReached)
	displayWarning('The maximum number of dots per series in the Chart view (' +this.chartMaxDotsPerSeries+ ') has been reached, but the Table view is not affected.');

	var myoptions = {
		'curveType': 'function', 'legend': { position: 'bottom' }, 'vAxis': { viewWindow: { min: 0 } },'pointSize': 5, 'height':500,
		chartArea:{left:60,top:30,width:"90%",height:"85%"}
	};
	//var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
	var chart = new google.visualization.LineChart(this.$('#gviz').get(0));
	chart.draw(chartData, myoptions);
},
clearAll: function () {
	this.collection.reset();
}
});
