/*
 * A complex input contains both the attributes necessary to restore a GUI context (or state) and the service input
 * I considered it is okay to couple these elements seeing as the app strategy is to call a service upon navigation to a new route and hence upon generation of a new Gui state.
 * The service input will hence always be a subset of the controller's input, and decoupling them would mean doing redundant work.
 * Therefore, it will remain like that until something challenges that concept.
 * 
 */
function ServiceInput(){

	this.selectors1 = undefined;
	this.selectors2 = undefined;
	this.serviceParams = {};

	this.getSelectors = function(context){
		var selectors = { 'selectors1' : this.selectors1 }
		
		if(context === 'Compare')
			selectors.selectors2 = this.selectors2;
		return selectors;
	};
	this.getServiceParams = function(){ return this.serviceParams;};

	this.setSelectors = function(selectors, context){
		this.selectors1 = selectors.selectors1;
		if(context === 'Compare')
			this.selectors2 = selectors.selectors2;
	};
	
	this.setServiceParams = function(serviceParams){
		this.serviceParams = serviceParams;
	};
}