load(workingDirectory+'/runtime.js');

/**
uses promises
change backbone
better logger (log4j
**/
/*setTimeout(function(){
	print("TIMEOUT 3000");
},3000);*/

var initBot = function (id,config,userAgent){
	jsLog(id,'initBot : ',config.behaviour);
	this.bots['_'+config.getId()] = new this.behaviours[config.behaviour](config.getId(),config,userAgent);
}

var botCb = function (id,method,args){
	//print("botCb",id,method,id,bots,this.bots['_'+id]);
	this.bots['_'+id][method].apply(this.bots['_'+id],args);
}
var p = new Promise(function (success, reject){
	http.request(new JSONObject({
		"url"		: "http://localhost/",
		"method"	: "GET",
		"callback"	: new Packages.org.micoli.http.Callback({
			"onSuccess" : function (response,code,obj){
				print ("onSuccess "+code+' '+response);
				print (obj);
				print (JSON.parse(obj));
				success(response);
			},
			"onError" : function (code,obj){
				print ("onError "+code);
				print (obj);
				print (JSON.parse(obj));
				reject(code)
			}
		})
	}));
});
p.then(function(response){
	print("ezezez"+response);
});