function initNewApplication() {

	var svg = d3.select("svg");
	svg.append("g").attr("test", "1");
	
	var obj = {"timeWindow":{"begin":1388440000000,"end":1591465779439},"intervalSize":null,"primaryDimensionKey":null,"selectors":[{"textFilters":[{"key":"name","value":"Transaction.*","regex":true},{"key":"eId","value":".*","regex":true}],"numericalFilters":[{"key":"value","minValue":0,"maxValue":100000000}]}],"properties":{}}
	
				$.ajax(
					{
						type: 'POST',
						url: '/rtm/rest/aggregate/get',
						contentType: "application/json",
						data: JSON.stringify(obj),
						success:function(result){
							setInterval( function() { myTimer(result.payload); }, 1000 );
						}
					});
	

}

function myTimer(arg1) {
	
	var textDiv = $("#textData");
	
				$.ajax(
					{
						type: 'POST',
						url: '/rtm/rest/aggregate/refresh',
						contentType: "application/json",
						data: JSON.stringify(arg1),
						success:function(result){
							//console.log(result);
							textDiv.text(result);
						}
					});
}

function drawD3Chart(pAggregates, pChartParams){
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

}

function convertToTable(payload, pChartParams){

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
}

function convertToSeries(payload, pChartParams){


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
}