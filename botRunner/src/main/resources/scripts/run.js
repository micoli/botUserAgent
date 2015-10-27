load(workingDirectory+'/environment.js');
/**
uses promises

better logger (log4j)
better logger (log4j)
better logger (log4j)
better logger (log4j)
better logger (log4j)
better logger (log4j)
**/
/*setTimeout(function(){
	print("TIMEOUT 3000");
},3000);*/

var initBot = function (id,config,userAgent){
	botLog(id,'initBot',config.behaviour);
	this.bots['_'+config.getId()] = new this.behaviours[config.behaviour](config.getId(),config,userAgent);
	//console.log(this.bots,id,config.behaviour,{"toto":"aaa{{color_red}}colored{{reset}}abbbb"},'test{{color_red}}abcd{{bold_on}}efcg{{reset}}test');
};

var botCb = function (id,method,args){
	this.bots['_'+id][method].apply(this.bots['_'+id],args);
};