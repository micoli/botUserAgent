print("JS >>>> START");

/*setTimeout(function(){
	print("TIMEOUT 3000");
},3000);*/

var initBot = function (id,config,userAgent){
	print('initBot : ',id,config.behaviour);
	this.bots['_'+config.getId()] = new this[config.behaviour]({id : config.getId(),config:config,ua:userAgent});
}

var botCb = function (id,method,args){
	//print("botCb",id,method,id,bots,this.bots['_'+id]);
	this.bots['_'+id][method](args);
}
