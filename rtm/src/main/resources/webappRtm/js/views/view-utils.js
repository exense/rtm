
$.extend(Backbone.View.prototype, {

	cleanup: function(){ 
		this.$el.html('');
	}

});


/* Utility functions and extends*/
function displayError(msg){
	if($("#errorZone").html().indexOf(msg) < 0){
		$("#errorZone").append('<div class="alert alert-danger" role="alert"><span><strong>Error : </strong></span><span>&nbsp</span><span>'+ msg +'</span></div>');
	}
}
function displayWarning(msg){
	if($("#errorZone").html().indexOf(msg) < 0){
		$("#errorZone").append('<div class="alert alert-warning" role="alert"><span><strong>Warning : </strong></span><span>&nbsp</span><span>'+ msg +'</span></div>');
	}
}

function setMenuActive(choice){

	$(".menu-choice").each(function(){
		$(this).removeClass('active');
		if($(this).text() === choice){
			$(this).addClass('active');
		}
	});
}

function setMenuActive(choice){

	$(".menu-choice").each(function(){
		$(this).removeClass('active');
		if($(this).text() === choice){
			$(this).addClass('active');
		}
	});
}

function setTitleActive(choice){
	$(".manager-title").text(choice);
}

function getActiveMenu(choice){
	return $(".menu-choice.active").text();
}

function getNoCacheValue(){
	if(Config.getProperty("client.debug") === "true"){
		return (new Date()).getTime();
	}
	else{
		return Config.getProperty("rtmVersion");
	}
}
function resolveTemplate(templateName){

	return 'js/templates/'+ templateName + '.und'
	//+ '?'
	//+ 'nocache='+ getNoCacheValue()
	;
}

function pad(num, size) {
	var s = num+"";
	while (s.length < size) s = "0" + s;
	return s;
}
function getPrintableDate(d) {
	var year = d.getFullYear() ;
	var month = d.getMonth() + 1;
	var day = d.getDate();
	var hours = d.getHours();
	var minutes = d.getMinutes();
	var seconds = d.getSeconds();
	var millis = d.getMilliseconds();

	return year + '.' + pad(month, 2) + '.' + pad(day, 2) + ' ' + pad(hours, 2) + ':' + pad(minutes, 2) + ':' + pad(seconds, 2) + '.' + pad(millis, 3);
}
function guiSelectorParseDate(input) {

	var msg = 'Incorrect date : ' + input + ', format is : yyyy-MM-dd hh:mm:ss.SSS';

	if(Math.floor(input) == input && $.isNumeric(input)) 
		return input;

	var spaceSplit = input.split(' ');

	var date;
	var time;
	var hms;

	var dotSplit;
	var columnSplit;

	if(spaceSplit.length !== 2){
		displayError(msg + '(spaceSplit)');
		return;
	}else{
		date = spaceSplit[0];
		time = spaceSplit[1];
		dotSplit = date.split('.');


		if(dotSplit.length !== 3){
			displayError(msg  + '(dotSplit)');
			return;
		}else{
			/*
			dotSplit = time.split('.');

			if(dotSplit.length !== 2){
				displayError(msg  + '(dotSplit)');
				return;
			}else{
				hms = dotSplit[0];

				columnSplit = hms.split(':');
				if(columnSplit.length !== 3){
					displayError(msg + '(columnSplit)');
					return;
				}

			 */

			columnSplit = time.split(':');
			if(columnSplit.length !== 3){
				displayError(msg + '(columnSplit)');
				return;
			}
		}

	}
	//return new Date(dotSplit[0], dotSplit[1]-1, dotSplit[2], columnSplit[0], columnSplit[1], columnSplit[2], dotSplit[1]).getTime();
	return new Date(dotSplit[0], dotSplit[1]-1, dotSplit[2], columnSplit[0], columnSplit[1], columnSplit[2]).getTime();
}

function guiToBackendInputForSelector(guiSelectors){

//console.log('guiToBackendInputForSelector');
//console.log(guiSelectors);
	var selPayload = [];

	// iterate over selectors
	var arrayLength = guiSelectors.length;
	for (var i = 0; i < arrayLength; i++) {
		//console.log('i='+i);
		var curGuiSelector = guiSelectors[i];
		$.extend(curGuiSelector, guiSelectorFunctions);

		var curBSelector = new TBackendSelector();

		var subArrayLength = guiSelectors[i].filters.length;
		for (var j = 0; j < subArrayLength; j++) {
			//console.log('j='+j);
			var curFilter = curGuiSelector.getFilter(j);

			$.extend(curFilter, filterFunctions);

			if(curFilter.getValueGeneric('type') === 'text'){
				var regexVal = 'false';
				if(curFilter.getValueGeneric('regex') === 'regex')
					regexVal = 'true';
				curBSelector.addKVR(curFilter.getValueGeneric('key'), curFilter.getValueGeneric('value'), regexVal);
			}

			if(curFilter.getValueGeneric('type') === 'numerical'){
				curBSelector.addKMinMax(curFilter.getValueGeneric('key'), curFilter.getValueGeneric('minValue'), curFilter.getValueGeneric('maxValue'));
			}
			if(curFilter.getValueGeneric('type') === 'date'){
				curBSelector.addKMinMax(curFilter.getValueGeneric('key'), guiSelectorParseDate(curFilter.getValueGeneric('minDate')), guiSelectorParseDate(curFilter.getValueGeneric('maxDate')));
			}
			//console.log('toBackend :');
			//console.log(curBSelector);
		}

		selPayload.push(curBSelector);
	}

	return selPayload;
}


function guiToBackendInput(guiSelectors){

//	console.log('guiToBackendInput');
//	console.log(guiSelectors);
	var selPayload = { 'selectors1' : guiToBackendInputForSelector(guiSelectors.selectors1) };

	if(guiSelectors.selectors2)
		selPayload.selectors2 = guiToBackendInputForSelector(guiSelectors.selectors2);

	return selPayload;
}

function convertToOld(payload, metrics){

	var result = [];
	var curRow = {};

	var seriesNb = payload.length;
	var series = [];

	var first = payload[Object.keys(payload)[0]];
	if(first){

		series = getSeriesFullTraversal(payload);

		_.each(series, function(sery){ 

			var seriesData = {"groupby" : sery, "data" : []};

			for(dot in payload){

				if(dot){
					var thisMeasure = {};
					thisMeasure['begin'] = parseInt(dot);
					var complete = true;
					_.each(metrics, function(metric){
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
}

function getSeriesFullTraversal(payload){
	var series = [];
	for(dot in payload){
		for(sery in  payload[dot]){
			if($.inArray(sery, series) < 0)
				series.push(sery);
		}
	}
	return series;
}

function convertArrayOfObjectsToCSV(args) {  
	var result = "Groupby,";

	_.each(Object.keys(args.data[0].data[0]), function(key) {
		result += key + ',';
	});
	
	result = result.substring(0, result.length - 1);
	result += '\n';
	
    _.each(args.data, function(item) {
    	var series = item.groupby;
    	_.each(item.data, function(ytem) {
    		result += series + ',';
    		_.each(Object.keys(ytem), function(key) {
    			result += ytem[key];
        		result += ',';
            });
    		result = result.substring(0, result.length - 1);
            result += '\n';
    	});
    });
    return result;
}

function downloadCSV(args) {  
    var data, filename, link;
    var csv = convertArrayOfObjectsToCSV({
        data: args.data
    });
    if (csv == null) return;

    filename = args.filename || 'export.csv';

     data = encodeURI(csv);

    if(msieversion()){
    	
    	var IEwindow = window.open();
    	IEwindow.document.write(csv);
    	IEwindow.document.close();
    	IEwindow.document.execCommand('SaveAs', true, "rtm-data.csv");
    	IEwindow.close();
    	
    }else{

        if (!csv.match(/^data:text\/csv/i)) {
        	data = 'data:text/csv;charset=utf-8,' + data;
        }

        link = document.createElement('a');
        link.setAttribute('href', data);
    	link.setAttribute('id', 'tempDownloadElement');
    	link.setAttribute('download', filename);
    
    	$('#tempHolder').append(link);
    
    	link.click();
    }
}

function msieversion() {
	  var ua = window.navigator.userAgent;
	  var msie = ua.indexOf("MSIE ");
	  if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./)) // If Internet Explorer, return true
	  {
	    return true;
	  } else { // If another browser,
	  return false;
	  }
	  return false;
	}