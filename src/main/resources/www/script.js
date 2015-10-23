angular
.module('botUserAgentApp', ['ui.router','ngWebSocket']);

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
.factory('WSStream', ['$websocket',function($websocket) {
	// Open a WebSocket connection
	var dataStream = $websocket(document.location.origin.replace("https:\/\/","wss://").replace("http:\/\/","ws://"));

	var collection = [];

	dataStream.onMessage(function(message) {
		console.log("onMessage",message.data);
		//collection.push(JSON.parse(message.data));
	});

	var methods = {
		collection: collection,
		get: function() {
			dataStream.send('/'/*JSON.stringify({
				action: 'get'
			})*/);
		}
	};

	return methods;
}]);

angular
.module('botUserAgentApp')
.controller('botsController', ['$scope','$http','WSStream',function($scope,$http,WSStream) {
	$scope.bots = [];
	$scope.WSStream=WSStream;

//	WSStream.get();

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