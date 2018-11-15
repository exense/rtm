var Compares = Backbone.Collection.extend({
      url: '/rtm/rest/aggregate/compare',
      model: Aggregate,

      refreshData: function(rawInput){

        var that = this;

        input = JSON.stringify(rawInput);
        //console.log('Fetching Aggregates with following service input : ');
        //console.log(input);
        this.fetch({
          type : 'POST',
          dataType:'json',
          contentType:'application/json; charset=utf-8',
          data: input,
          success: function (response) {
           	//console.log('loaded ' + response.length + ' objects.');
            //console.log(response.models[0].get('warning'));
        	//console.log(JSON.stringify(response.models[0]));
        	//console.log(JSON.stringify(response.models[0].get('payload')));
        	if(response.models[0].get('status') !== 'SUCCESS')
           		displayError('[SERVER_CALL] Technical Error=' + JSON.stringify(response.models[0].get('metaMessage')));
           else{
           if(response.models[0].get('payload') && Object.keys(response.models[0].get('payload')).length > 0){
           		//console.log('triggering compare event.');
           		that.trigger('ComparesRefreshed');
           	}
           	}
         },
         error: function( model, response, options ){
          //console.log('model=' + JSON.stringify(model) + ', response=' + JSON.stringify(response) + ', options=' + JSON.stringify(options));
	         that.trigger('pauseChartTimer');
          	 displayError('[SERVER_CALL] Technical Error=' + JSON.stringify(response)+ ';       input=' + input);
          that.reset();
        }
      });
        return status;
      }
    });