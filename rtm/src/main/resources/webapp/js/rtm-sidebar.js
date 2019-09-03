angular.module('rtm-sidebar', [])

    .directive('rtmSidebar', function () {
        return {
            restric: 'E',
            scope: {
            },
            templateUrl: 'templates/rtm-sidebar.html',
            controller: function ($scope, $element) {
            }
        };
    });