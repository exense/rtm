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
				$http.post('/rtm/rest/crud/session', serialized)
				.then(function (response) {
					console.log(response)
				}, function (response) {
					console.log(response)
				});
			};

			$scope.loadDashboard = function(name){
				$http.get('/rtm/rest/crud/session?name=mysession')
				.then(function (response) {
					console.log(response.data.state)
					$scope.dashboards = response.data.state;
					//$scope.$emit('dashboard-change');
					console.log(response.data)
				}, function (response) {
					console.log('response')
					console.log(response)
				});

			};
		}
	};
})