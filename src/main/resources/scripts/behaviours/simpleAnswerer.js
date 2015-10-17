this.behaviours.simpleAnswerer = this.behaviours._default.extnd({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		jsLog(that.id,"registerSuccessful",config.getUserPart()+" |simpleAnswerer");
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var delay;
		jsLog(that.id,"incomingCall",sipRequest);
		if(maybe(80,100)){
			delay = getRandomInt(500,1000);
			jsLog(that.id,"answer in "+delay,callId);
			setTimeout(function(){
				delay = getRandomInt(2500,5000);
				that.ua.acceptCallByCallId(callId);
				jsLog(that.id,"terminate in "+delay,callId);
				setTimeout(function(){
					that.ua.terminateByCallId(callId);
					jsLog(that.id,"hang up",callId);
				},delay);
			},delay);
		}else{
			jsLog(this.id,"refuse=>busy",callId);
			that.ua.busyByCallId(callId);
		}
	}
});