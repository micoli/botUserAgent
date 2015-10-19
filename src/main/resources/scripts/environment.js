//load("nashorn:mozilla_compat.js");
load(workingDirectory+'/libs/underscore-min.js');
load(workingDirectory+'/libs/console.js');
load(workingDirectory+'/libs/context.ext.js');
load(workingDirectory+'/libs/promises.js');
load(workingDirectory+'/libs/extnd.js');

JSONObject = Packages.org.json.simple.JSONObject;
CommandArgs = org.micoli.commandRunner.CommandArgs;
this.bots = {};

this.behaviours = {};

var initBot	= function (id,config){};

var botCb	= function (id,method,args){};

var botLog	= function(){
	console.log (
		"{{color_magenta}}"+arguments[0]+"{{reset}}",
		"{{color_cyan}}["+arguments[1]+"]{{reset}}",
		(arguments.length==2?'':arguments[2])
	);
};

var javaLog	= function(log){
	console.log ("{{color_yellow}}javaLog{{reset}}",log);
};

var javaNetworkLog = function(log){
	console.log ("{{color_green}}javaNetworkLog{{reset}}",log);
};