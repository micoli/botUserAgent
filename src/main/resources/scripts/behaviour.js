print("JS >>>> START");

/*setTimeout(function(){
	print("TIMEOUT 3000");
},3000);*/

var initBot = function (id,config,userAgent){
	print('initBot : ',id,config.behaviour);
	this.bots.add(new this[config.behaviour]({id : config.getId(),config:config,ua:userAgent}) );
}

var botCb = function (id,method,args){
	print("botCb",id,method);
	//this.bots.get(id)[method](args);
}

//var model = new agentModel({});