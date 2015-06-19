this.behaviours.answerHangup = this.behaviours._default.extend({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		print(this.id+" :: registerSuccessful",config.getUserPart()+" answerHangup");
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		print(that.id+" :: incomingCall",callId);
		setTimeout(function(){
		print(that.id+" :: acceptCallByCallId",callId);
			that.get('ua').acceptCallByCallId(callId);
			setTimeout(function(){
				print(that.id+" :: terminateByCallId");
				that.get('ua').terminateByCallId(callId);
			},5000);
		},1000);
	}
});