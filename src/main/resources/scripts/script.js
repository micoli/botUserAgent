print("JS >>>> START");
var fun1=function(a){
	_.each(["aa","bb"],function(v){
		print (v);	
	})
	return 1
}

var init=function(a){
	print("JS >>>> init");
	botUserAgent.register();
}

var registering=function(a){
	print("JS >>>> registering",a);
}

var registerSuccessful=function(a){
	print("JS >>>> registerSuccessful",a);
	botUserAgent.call("sip:201@10.80.0.95");
}

var javaLog = function(log){
	//print("debuglog >>>> ",log);
}
var javaNetworkLog = function(log){
	//print("debuglog >>>> ",log);
}
var remoteHangup = function(log){
	//print("debuglog >>>> ",log);
}
var calleePickup = function(log){
	//print("debuglog >>>> ",log);
}