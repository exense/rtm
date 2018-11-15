var Config = function(){

	Config.config = {};
	Config.confUrl  = '/rtm/rest/configuration/getConfiguration?noCache=' + (new Date()).getTime();
	Config.initVar = false;
};

var ConfigFunctions = {
		loadConfig : function(callback){

			var that = this;

			$.ajax(
					{
						url: that.getConfUrl(),
						success:function(result){
							that.setConfigObject(result['config']);
							that.setInit(true);
							callback();
						},
						error: function(result){
						
							if(result.status === 401){
								$("#errorZone").html("SSO Login is active. You are not allowed to access RTM directly. Please log into your third party application first.<br /><br /><br />");
							}else{
								$("#errorZone").html("Something went wrong. Please check your browser console as well as RTM's server-side logs for errors.<br /><br /><br />");
							}
						}
					});

		},
		getProperties : function(){return Config.config;},
		getProperty : function(key){return Config.config[key];},
		setProperty : function(key, value){Config.config[key] = value;},
		getConfUrl : function(){return Config.confUrl;},
		setConfigObject : function(obj){Config.config=obj;},
		isInit : function(){return Config.initVar;},
		setInit : function(obj){Config.initVar=obj;}
};

jQuery.extend(Config, ConfigFunctions);