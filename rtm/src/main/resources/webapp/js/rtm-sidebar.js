angular.module('rtm-sidebar', [])
.directive('rtmSidebar', function () {
	return {
		restrict: 'E',
		scope:{
			dashboards: '='
		},
		templateUrl: 'templates/rtm-sidebar.html'+ '?who=rtm-sidebar&anticache=' + getUniqueId(),
		controller: function ($scope, $element, $http) {

			$scope.saveDashboard = function(){
				console.log($scope.dashboards);
				var serialized = angular.toJson({ name : 'mysession', state : $scope.dashboards }); 
				$http.post('/rtm/rest/visualization/session', serialized)
				.then(function (response) {
					console.log(response)
				}, function (response) {
					console.log(response)
				});
			};

			$scope.loadDashboard = function(name){
				$http.get('/rtm/rest/visualization/session?name=mysession')
				.then(function (response) {
					$scope.dashboards.length = 0;
					console.log(response.data.state)
					$.each(response.data.state, function(index, dashboard){
						dashboard.widgets = new IdIndexArray(dashboard.widgets.array);
						$scope.dashboards.push(dashboard);
					});
					$scope.$emit('dashboard-change');
					console.log(response.data)
				}, function (response) {
					console.log('response')
					console.log(response)
				});

			};
		}
	};
})