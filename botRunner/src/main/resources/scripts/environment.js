//load("nashorn:mozilla_compat.js");
window=global;
load(workingDirectory+'/libs/underscore-min.js');
load(workingDirectory+'/libs/console.js');
load(workingDirectory+'/libs/context.ext.js');
load(workingDirectory+'/libs/promises.js');
load(workingDirectory+'/libs/extnd.js');

JSONObject = Packages.org.json.simple.JSONObject;
CommandArgs = org.micoli.api.commandRunner.CommandArgs;
this.bots = {};

this.behaviours = {};

var initBot	= function (id,config){};

var botCb	= function (id,method,args){};

var botLog	= function(){
	var args = Array.prototype.slice.call(arguments);
	console.log.apply(null,["{{color_magenta}}"+args[0]+"{{reset}}","{{color_cyan}}["+args[1]+"]{{reset}}"].concat(args.length==2?null:args.slice(2)));
};

var javaLog	= function(log){
	console.log ("{{color_yellow}}javaLog{{reset}}",log);
};

var javaNetworkLog = function(log){
	console.log ("{{color_green}}javaNetworkLog{{reset}}",log);
};