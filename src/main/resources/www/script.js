angular
.module('botUserAgentApp', ['ui.router']);

angular
.module('botUserAgentApp')
.config(function($stateProvider, $urlRouterProvider,$httpProvider) {

	$urlRouterProvider.otherwise('/');

	$stateProvider
	.state('home', {
		url			: '/',
		templateUrl	: 'partial-home.html',
		controller	: 'botsController'
	})
	.state('about', {
		url			: '/about',
		templateUrl	: 'partial-about.html'
	});
});

angular
.module('botUserAgentApp')
.controller('botsController', ['$scope','$http',function($scope,$http) {
	$scope.bots = [];

	$scope.refresh = function(){
		$http
		.get('/cmd/list')
		.success(function (data, status) {
			$scope.bots = data;
		});
	};

	$scope.call = function(callData){
		$scope.callResult = {};
		$scope.callUrl = '/cmd/bot?action=call&from='+callData.from+'&to='+callData.to;
		$http
		.get($scope.callUrl)
		.success(function (data, status) {
			$scope.callResult = data;
		});
	};
	$scope.refresh();
}]);