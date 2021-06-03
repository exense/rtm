var Measurement = Backbone.Model.extend({});

var Measurements = Backbone.Collection.extend({
      url: '/rtm/rest/measurement/find',
      model: Measurement,

      refreshData: function(rawInput){

        var that = this;

        input = JSON.stringify(rawInput);
        //console.log('Fetching Measurements with following input : ' + input);
        this.fetch({
          type : 'POST',
          dataType:'json',
          contentType:'application/json; charset=utf-8',
          data: input,
          success: function (response) {
            //console.log('loaded ' + response.length + ' objects.');
            //console.log(response);
            if(response.models[0].get('status') !== 'SUCCESS')
           		displayError('[SERVER_CALL] Technical Error=' + JSON.stringify(response.models[0].get('metaMessage')));
           else{
            that.trigger('MeasurementsRefreshed');
            }
          },
          error: function( model, response, options ){
            //console.log('model=' + JSON.stringify(model) + ', response=' + JSON.stringify(response) + ', options=' + JSON.stringify(options));
          	 displayError('[SERVER_CALL] Technical Error=' + JSON.stringify(response)+ ';       input=' + input);
            that.reset();
          }
        });
        return status;
      }
    });
