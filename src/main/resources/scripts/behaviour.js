print("JS >>>> START");

setTimeout(function(){
	print("TIMEOUT 3000");
},3000);



var javaLog = function(log){
	//print("debuglog >>>> ",log);
}

var javaNetworkLog = function(log){
	//print("javaNetworkLog >>>> ",log);
}

var init = function (config){
}

var botCb = function (id,method,args){
	print(id+"::"+method);
}


var model = new agentModel({});