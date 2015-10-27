/*
#!/bin/bash
wget https://maven.java.net/content/repositories/public/com/oracle/libavatar-js-macosx-x64/0.10.32-SNAPSHOT/libavatar-js-macosx-x64-0.10.32-20150324.075942-84.dylib -O libavatar-js.dylib
wget https://maven.java.net/content/repositories/public/com/oracle/libavatar-js-linux-x64/0.10.32-SNAPSHOT/libavatar-js-linux-x64-0.10.32-20150322.063147-102.so -O libavatar-js.so
wget https://maven.java.net/content/repositories/public/com/oracle/libavatar-js-win-x64/0.10.32-SNAPSHOT/libavatar-js-win-x64-0.10.32-20141126.224700-36.dll -O libavatar-js.dll
wget https://maven.java.net/content/repositories/public/com/oracle/avatar-js/0.10.32-SNAPSHOT/avatar-js-0.10.32-20150322.063156-103.jar -O avatar-js.jar
*/

	console.log("workingDirectory");
	var workingDirectory='/Users/o.michaud/Documents/workspace/botUserAgent/src/main/resources/scripts';
	//load(workingDirectory+'/runtime.js');
	load(workingDirectory+'/libs/context.ext.js');
	window=this;
	load(workingDirectory+'/libs/q.js');
	print (7);
	var p = new Promise(function (success, reject){
		http.request(new JSONObject({
			"url"		: "http://localhost/",
			"method"	: "GET",
			"callback"	: new Packages.org.micoli.http.Callback({
				"onSuccess" : function (response,code,obj){
					console.info("onSuccess",code,obj,response);
					success(response);
				},
				"onError" : function (code,obj){
					console.error("onError",code,obj);
					reject(code);
				}
			})
		}));
	});

	p.then(function(response){
		console.log("HTML response",response);
	});

	var incomingCall = function(callId){
		var that = this;
		try{
			var p1 = function(val) {
				var deferred = Q.defer();
				console.log ("ee");
				return deferred.promise;
			};

			p1.then(function(value) {
				print ("success "+value); // Success!
			}, function(reason) {
				console.log ("error "+reason); // Error!
			})['catch'](function(e){
				console.log("ee");
			});
			console.log("jj");
		}catch(e){
			console.log("error");
			for(var u in e){
				e.dumpStack();
				console.log ("zz"+u);
				if(e.hasOwnProperty(u)){
					console.log (u+' '+e[u]);
				}

			}

		}

		return;


		var p = new Promise(function(resolve, reject) {
			resolve("Success!");
		});
		var p1 = new Promise(function(resolve, reject) {
			resolve("Success P1!");
		});

		p
		.then(p1)
		.then(function(value) {
			console.log ("success "+value); // Success!
		}, function(reason) {
			console.log ("error "+reason); // Error!
		});

		return;
		var p2 = new Promise(function (success){
			console.log ("ee2");
			success("ee3")
		})
		//p.then(p2);

		p.then(function(){
			return new Promise(function (success){
				botLog(that.id,"incomingCall "+callId+" will answer in "+delay,sipRequest);
				setTimeout(function(){
					resolve(callId);
				}, delay);
				var delay = getRandomInt(2000,4000);
			});
		},function(){
			console.log("rrr1");
		}).then(function(val){
			console.log(val);
		},function(){
			console.log("rrr2");
		});/*.then(function(callid) {
			var delay = getRandomInt(4000,10000);
			botLog(that.id,"incomingCall "+callId+" will be hangup in "+delay,sipRequest);
			setTimeout(function(){
				botLog(that.id,"terminateByCallId");
			},delay);
		}).error(function(){
			botLog(that.id,"error ",callId);
		});*/
	}
	incomingCall(11)
