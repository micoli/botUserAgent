print("JS >>>> START");

var init=function(a){
	print("JS >>>> init");
	botUserAgent.register();
}

var registering = function(sipRequest,config){
	print("JS >>>> registering",config.getUserPart());
}
var registerSuccessful = function(sipResponse,config){
	print("JS >>>> registerSuccessful",config.getUserPart());
	//botUserAgent.call("sip:201@10.80.0.95");
}
var registerFailed = function(sipResponse,config){
	print("JS >>>> registerFailed",config.getUserPart());
}

var incomingCall = function(sipRequest,provResponse){
}

var remoteHangup = function(sipRequest){
}

var ringing = function(sipResponse){
}

var calleePickup = function(sipResponse){
}

var error = function(sipResponse){
}

var setInviteSipRequest = function(sipRequest){
}

var javaLog = function(log){
	//print("debuglog >>>> ",log);
}

var javaNetworkLog = function(log){
	//print("javaNetworkLog >>>> ",log);
}
