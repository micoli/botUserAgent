load(workingDirectory+'/libs/underscore-min.js');
load(workingDirectory+'/libs/context.ext.js');
load(workingDirectory+'/libs/promises.js');
load(workingDirectory+'/libs/extnd.js');
JSONObject = Packages.org.json.simple.JSONObject;
this.bots = {};
this.behaviours = {};

var initBot	= function (id,config){};

var botCb	= function (id,method,args){};

var jsLog	= function(){
	//print.apply(null,['jsLog'].concat(arguments.slice()));
	//print.apply(null,arguments);
	print ("\033[" + 31 + "m"+arguments[0] + " \033[" + 36 + "m ["+arguments[1] + "] \033[" + 37 + "m"+ (arguments.length==2?'':arguments[2]));
};

var javaLog	= function(log){
	print ("javaLog "+log);
};

var javaNetworkLog = function(log){
	print ("javaNetworkLog "+log);
};