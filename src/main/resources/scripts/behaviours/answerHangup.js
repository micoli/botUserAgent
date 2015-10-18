this.behaviours.answerHangup = this.behaviours._default.extnd({
	preInit: function() {
		var that = this;
		var aNbr=['zero','un','deux','trois','quatres','cinq','six','sept','huit','neuf'];
		botLog(that.id, "preInit");
		that.audioFile="/tmp/" + that.id + ".raw";
		if(!(new java.io.File(that.audioFile)).exists()){
			var txt = "poste "+that.id+".";
			for (var i in aNbr){
				txt = txt.replace(new RegExp(i, 'g')," "+aNbr[i]);
			}
			txt = txt.replace(/ /g,'%20');
			var bin = "nohup "+workingDirectory+"/behaviours/tts.sh "+that.audioFile+"  \"" +txt+ "\" ";
			try{
				that.ua.exec(bin);
			}catch(e){
				print (e);
			}
		}
	},

	registerSuccessful : function(sipResponse,config){
		var that = this;
		botLog(that.id,"registerSuccessful",config.getUserPart()," |answerHangup",sipResponse);
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var lng1 = getRandomInt(2000,4000);
		that.ua.setAnswerFile(that.audioFile)
		botLog(that.id,"incomingCall "+callId+" will answer in "+lng1,JSON.parse(sipRequest));
		setTimeout(function(){
			var lng = getRandomInt(4000,10000);
			botLog(that.id,"acceptCallByCallId " + callId + ", will hang in " + lng);
			that.ua.acceptCallByCallId(callId);
			setTimeout(function(){
				botLog(that.id,"terminateByCallId");
				that.ua.terminateByCallId(callId);
			},lng);
		},lng1);
	},

	promiseIncomingCall : function(sipRequest,provResponse,callId){
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
	}
});