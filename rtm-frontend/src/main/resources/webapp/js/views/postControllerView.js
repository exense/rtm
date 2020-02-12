var PostControllerView = Backbone.View.extend(
		{
			el: '.PostControllerView',
			events: {

				"click #addSelector" : "addSelector",
				"click #clearAll" : "clearAll",
				"click .at" : "addTextFilter",
				"click .an" : "addNumFilter",
				"click .ad" : "addDateFilter",
				"click .rs" : "remSelector",
				"click .rtf" : "remFilter",
				"click .rnf" : "remFilter",
				"click .rdf" : "remFilter",

				"change .sinp": "refreshController",
				"click #sendSearch" : "sendSearch",
				"click #pause" : "pauseEvent",
				"click #resume" : "resumeEvent",
				"keypress" : "hijackEnter",
				"click .defaultKey" : "setDefaultKey"
			},

//			Configurative, will be retrieved from a server configuration service in the next major update
			defaultTextKeys : '',
			defaultNumericalKeys : '',
			defaultDateKeys : '',

			spinner : '',

			getGuiDomain: function(){
				return 'postControllerView';
			},

			getServiceDomain: function(){
				return 'default';
			},

			selectors1 : [],
			selectors2 : [],

			hijackEnter : function(e){
				if(e.keyCode === 13){
					this.trigger('globalSearchEvent');
					e.preventDefault();
				}
			},

			initialize : function(){
				this.activeContext = this.context;
				this.defaultTextKeys = Config.getProperty('defaultTextKeys').split(',');
				this.defaultNumericalKeys=Config.getProperty('defaultNumericalKeys').split(',');
				this.defaultDateKeys=Config.getProperty('defaultDateKeys').split(',');
			},

			setContext : function (context) {
				this.activeContext = context;
			},

			hasData : function () {
				return (this.selectors1.length > 0) || (this.selectors2.length > 0);
			},

			setDefaultKey :function (e) {
				var instId = this.getEventInstanceId(e);
				var splitArray = e.currentTarget.id.split("_");
				var selId = splitArray[0];
				var filId = splitArray[1];
				var value = splitArray[2];
				var instId = this.getEventInstanceId(e);
				this['selectors' + instId][selId].getFilter(filId).setValueGeneric('key',value);
				this.render();
				e.preventDefault();
			},

			addTextFilter: function (e) {
				var instId = this.getEventInstanceId(e);
				this['selectors' + instId][e.currentTarget.id].pushFilter(new TextFilter());
				this.render();
				e.preventDefault();
			},
			addNumFilter: function (e) {
				var instId = this.getEventInstanceId(e);
				this['selectors' + instId][e.currentTarget.id].pushFilter(new NumericalFilter());
				this.render();
				e.preventDefault();
			},
			addDateFilter: function (e) {
				var instId = this.getEventInstanceId(e);
				this['selectors' + instId][e.currentTarget.id].pushFilter(new DateFilter());
				this.render();
				e.preventDefault();
			},
			remSelector: function (e) {
				var instId = this.getEventInstanceId(e);
				this['selectors' + instId].splice(e.currentTarget.id,1);
				this.render();
				e.preventDefault();
			},
			getEventInstanceId: function (e) {
				return e.currentTarget.getAttribute("instid");
			},
			render: function () {

				var that = this;
				jQuery.get(resolveTemplate('postController-template'), function (data) {
					template = _.template(data, {
						controller: {text : 'Add Selector 1'},
						selectors : that.selectors1,
						defaultTextKeys : that.defaultTextKeys,
						defaultNumericalKeys : that.defaultNumericalKeys,
						defaultDateKeys : that.defaultDateKeys,
						instId : 1
					});

					if(that.activeContext === 'Compare'){
						template2 = _.template(data, {
							controller: {text : 'Add Selector 2'},
							selectors : that.selectors2,
							defaultTextKeys : that.defaultTextKeys,
							defaultNumericalKeys : that.defaultNumericalKeys,
							defaultDateKeys : that.defaultDateKeys,
							instId : 2
						});

						that.$el.html(template + template2);
					}else{
						that.$el.html(template);
					}  
				}, 'html')
				.success(function(){
					$('.form_datetime').datetimepicker({
						language:  'fr',
						weekStart: 1,
						todayBtn:  1,
						autoclose: 1,
						todayHighlight: 1,
						startView: 2,
						forceParse: 0,
						showMeridian: 1
					});

					that.resetSpinner();
					that.spinner.stop();
				}) // success
				.fail(function(model, response, options ) {
					displayError('response=' + JSON.stringify(response));
				});
			},

			resetSpinner : function(){
				var opts = {
						top: 0, left: 0, lines: 13, length: 35, width: 15, radius: 18, scale: 0.2, corners: 1, color: 'gray',
						opacity: 0.20, rotate: 0, direction: 1, speed: 0.8, trail: 80, fps: 25, zIndex: 2e9,
						className: 'spinner', shadow: false, hwaccel: false, position: 'relative'
				};
				this.spinner = new Spinner(opts).spin();
				//this.spinner.stop();
				var target = $('#spinner');
				if(target.length > 0){
					target.append(this.spinner.el);
				}
			},
			stopSpinner : function(){
				this.spinner.stop();
				$('#spinner').empty();
			},
			addSelector : function(event){
				var instId = this.getEventInstanceId(event);
				this['selectors' + instId].push(new GuiSelector());
				this.render();
				event.preventDefault();
			},

			clearAll: function(event){
				var instId = this.getEventInstanceId(event);
				this['selectors' + instId] = [];
				this.render();
				event.preventDefault();
			},

			getGuiFragment: function(event){
				if(this.activeContext === 'Compare')
					return { 'selectors1' : this.selectors1, 'selectors2' : this.selectors2};
					else{
						return { 'selectors1' : this.selectors1};
					}
			},
			getServiceFragment: function(event){
				if(this.activeContext === 'Compare'){
					return {
						'selectors1' : this.selectors1,
						'selectors2' : this.selectors2
					};
				}
				else{
					return {
						'selectors1' : this.selectors1
					};
				}
			},

			remFilter : function(e){
				var instId = this.getEventInstanceId(e);
				var id = e.currentTarget.id.split("_");
				var selId = id[0];
				var filterId = id[1];
				this['selectors' + instId][selId].popFilter(filterId,1);
				this.render();
				e.preventDefault();
			},

			hasValidFilters: function(){
				if(this.activeContext === 'Compare')
					return this.hasValidFiltersForSelector(1) && this.hasValidFiltersForSelector(2);
				else
					return this.hasValidFiltersForSelector(1);
			},

			hasValidFiltersForSelector: function(instId){
				var result = false;
				var l = this['selectors' + instId].length;
				if(l < 1)
					return false;
				for(i = 0; i < l; i++){
					var thisSelector = this['selectors' + instId][i];
					var l2 = thisSelector.getFilters().length;
					if(l2 < 1)
						continue;
					for(j=0; j < l2; j++){
						var thisFilter = thisSelector.getFilter(j);
						if(thisFilter.isEmpty())
							return false;
					}
				}
				return true;
			},

			sendSearch: function(e){
				this.spinner.spin();
				this.trigger('globalSearchEvent');
				e.preventDefault();
			},
			pauseEvent: function(e){
				this.trigger('pauseEvent');
				e.preventDefault();
			},
			resumeEvent: function(e){
				this.trigger('resumeEvent');
				e.preventDefault();
			},
			refreshController: function(e){
				var instId = this.getEventInstanceId(e);
				var sel = this['selectors' + instId];

				if(e.currentTarget.className.indexOf("treg") > -1){
					$(".treg").each(function(idx, itemZ){
						if(itemZ.id === e.currentTarget.id)
						{
							var id = e.currentTarget.id.split("_");
							var selId = id[0];
							var filterId = id[1];
							var setVal= '';
							if(itemZ.value === 'regex'){
								setVal = '';
							}else{
								setVal = 'regex';
							}
							sel[selId].getFilter(filterId).setValueGeneric('regex',setVal);
						}
					});
				}

				if(e.currentTarget.className.indexOf("tkey") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('key',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("tval") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('value',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("nkey") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('key',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("nmin") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('minValue',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("nmax") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('maxValue',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("dkey") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('key',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("dmin") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('minDate',e.currentTarget.value);
				}

				if(e.currentTarget.className.indexOf("dmax") > -1){
					var id = e.currentTarget.id.split("_");
					var selId = id[0];
					var filterId = id[1];
					sel[selId].getFilter(filterId).setValueGeneric('maxDate',e.currentTarget.value);
				}
			},

			loadGuiState: function(input){
				this.selectors1 = [];
				this.selectors2 = [];

//				console.log(input);
				this.selectors1 = this.addFunctionality(input, 1);
				if(this.activeContext === 'Compare')
					this.selectors2 = this.addFunctionality(input, 2);
			},

			addFunctionality: function(inputObject, index){
				var input = inputObject['selectors' + index];
				var al = input.length;
				//console.log(al);
				for(i = 0; i < al; i++){
					jQuery.extend(input[i], guiSelectorFunctions);  
					var al2 = input[i].getFilters().length;
					//console.log(al2);
					for(j = 0; j < al2; j++){
						//console.log('it ' + j);
						jQuery.extend(input[i].getFilter(j), filterFunctions);
						//console.log(input[i].getFilter(j));
						//console.log(input[i].getFilter(j).getValueGeneric('key'));
					}
				}

				return input;
			}

		});