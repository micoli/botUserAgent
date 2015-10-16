this.behaviours.answerHangup = this.behaviours._default.extend({
	preInit: function() {
		var that = this;
		var aNbr=['zero','un','deux','trois','quatres','cinq','six','sept','huit','neuf'];
		jsLog(that.id, "preInit");
		that.audioFile="/tmp/" + that.id + ".raw";
		if(!(new java.io.File(that.audioFile)).exists()){
			var txt = "poste "+that.id+".";
			for (var i in aNbr){
				txt = txt.replace(new RegExp(i, 'g')," "+aNbr[i]);
			}
			txt = txt.replace(/ /g,'%20');
			var bin = "nohup "+workingDirectory+"/behaviours/tts.sh "+that.audioFile+"  \"" +txt+ "\" ";
			try{
				that.get('ua').exec(bin);
			}catch(e){
				print (e);
			}
		}
	},

	registerSuccessful : function(sipResponse,config){
		var that = this;
		jsLog(that.id,"registerSuccessful",config.getUserPart()," |answerHangup",sipResponse);
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var lng1 = getRandomInt(2000,4000);
		that.get('ua').setAnswerFile(that.audioFile)
		jsLog(that.id,"incomingCall "+callId+" will answer in "+lng1,sipRequest);
		setTimeout(function(){
			var lng = getRandomInt(4000,10000);
			jsLog(that.id,"acceptCallByCallId " + callId + ", will hang in " + lng);
			that.get('ua').acceptCallByCallId(callId);
			setTimeout(function(){
				jsLog(that.id,"terminateByCallId");
				that.get('ua').terminateByCallId(callId);
			},lng);
		},lng1);
	}
});
