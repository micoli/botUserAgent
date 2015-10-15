this.behaviours.answerHangup = this.behaviours._default.extend({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		jsLog(that.id,"registerSuccessful",config.getUserPart()," |answerHangup",sipResponse);
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var lng1 = getRandomInt(2000,4000);
		jsLog(that.id,"incomingCall "+callId+" will answer in "+lng1,sipRequest);
		setTimeout(function(){
			var lng = getRandomInt(4000,1000);
			jsLog(that.id,"acceptCallByCallId " + callId + ", will hang in " + lng);
			that.get('ua').acceptCallByCallId(callId);
			setTimeout(function(){
				jsLog(that.id,"terminateByCallId");
				that.get('ua').terminateByCallId(callId);
			},lng);
		},lng1);
	}
});