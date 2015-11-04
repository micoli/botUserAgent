
this.behaviours.answerHangup = this.behaviours._default.extnd({
	preInit: function() {
		//syncExec.exec("/bin/ls /tmp/");;
		var that = this;
		var aNbr=['zero','un','deux','trois','quatres','cinq','six','sept','huit','neuf'];
	},

	registerSuccessful : function(sipResponse,config){
		var that = this;
		botLog(that.id,"registerSuccessful",config.getUserPart()," |answerHangup",sipResponse);
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var lng1 = getRandomInt(2000,4000);
		var coloredCallid = "{{color_yellow}}"+callId+"{{reset}}";
		//that.ua.setAnswerFile(that.audioFile);
		that.ua.execute("setVoice",new CommandArgs({voice:"upmc-jessica"}));

		var activeCall = that.ua.getActiveCall();
		if(activeCall){
			var lng = getRandomInt(500,2000);
			botLog(that.id,"incomingCall",coloredCallid,"will refused (busy) in "+lng);
			setTimeout(function(){
				that.ua.busyByCallId(callId);
			});
		}else{
			botLog(that.id,"incomingCall",coloredCallid,"will answer in "+lng1,sipRequest);//,JSON.parse(sipRequest));
			setTimeout(function(){
				var activeCall = that.ua.getActiveCall();
				if(activeCall){
					botLog(that.id,"acceptCallByCallId",coloredCallid, "cancelled, already in conversation");
				}else{
					var lng = getRandomInt(4000,10000);
					botLog(that.id,"acceptCallByCallId ",coloredCallid , "will hang in " + lng);
					that.ua.acceptCallByCallId(callId);
					try{
						console.log('error1');
						that.ua.execute("sayWords",new CommandArgs({"callId":callId,"words":"bonjour monsieur, madame. poste "+that.id}));
						console.log('error2');
					}catch(e){
						console.log('error3');
						console.log(e);
					}

					setTimeout(function(){
						botLog(that.id,"terminateByCallId",coloredCallid);
						that.ua.terminateByCallId(callId);
					},lng);
				}
			},lng1);
		}
	}

	/*promiseIncomingCall : function(sipRequest,provResponse,callId){
		var that = this;

		(new Promise(function (success){
			that.ua.setAnswerFile(that.audioFile)
			success();
		})).then(function(resolve, reject){
			var delay = getRandomInt(2000,4000);
			botLog(that.id,"incomingCall "+callId+" will answer in "+delay,sipRequest);
			setTimeout(function(){
				that.ua.acceptCallByCallId(callId);
				resolve(callId);
			}, delay);
		}).then(function(callid) {
			var delay = getRandomInt(4000,10000);
			botLog(that.id,"incomingCall "+callId+" will be hangup in "+delay,sipRequest);
			setTimeout(function(){
				botLog(that.id,"terminateByCallId");
				that.ua.terminateByCallId(callId);
			},delay);
		}).error(function(){
			botLog(that.id,"error ",callId);
		});
	}*/
});
