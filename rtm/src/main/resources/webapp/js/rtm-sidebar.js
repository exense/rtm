angular.module('rtm-sidebar', [])
.directive('rtmSidebar', function () {
	return {
		restric: 'E',
		scope: {
		},
		templateUrl: 'templates/rtm-sidebar.html'+ '?who=rtm-sidebar&anticache=' + getUniqueId(),
		controller: function ($scope, $element, wmservice, $http) {

			$scope.saveDashboard = function(){
				console.log(JSON.stringify({ name : 'mydashboard', state : wmservice.dashboards}));
				$http.post('/rtm/rest/visualization/session',
						JSON.stringify({ name : 'mydashboard', state : wmservice.dashboards}))
				.then(function (response) {
					console.log('cool')
					console.log(response)
				}, function (response) {
					console.log('fakk')
					console.log(response)
				});
			};
			
			$scope.loadDashboard = function(name){
				$http.get('/rtm/rest/visualization/session?name=mydashboard')
				.then(function (response) {
					console.log(response.data.state)
					wmservice.dashboards = response.data.state;
					$scope.$emit('dashboard-change');
				}, function (response) {
					console.log('fakk')
					console.log(response)
				});

			};
		}
	};
})