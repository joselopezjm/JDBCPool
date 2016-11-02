var app = angular.module('testapp.controllers.main', []);

app.controller('MainCtrl', function($scope, MainService){
	$scope.doUp = function(){
		var direction = "up";
		alert(direction);
		MainService.getEntryG().then(function success(data){
		$scope.response = data;
	}, function error (data){
		console.log("Error!")
	});
	};
 	
 	$scope.doLeft = function(){
 		var direction = "left";
		MainService.getEntryG(direction).then(function success(data){
		$scope.response = data;
	}, function error (data){
		console.log("Error!")
	});
	};

	$scope.doRight = function(){
		var direction = "right";
		MainService.getEntryG().then(function success(data){
		$scope.response = data;
	}, function error (data){
		console.log("Error!")
	});
	};

	$scope.doDown = function(){
		var direction = "down";
		MainService.getEntryG().then(function success(data){
		$scope.response = data;
	}, function error (data){
		console.log("Error!")
	});
	};
	
})