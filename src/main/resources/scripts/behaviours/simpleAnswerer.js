this.behaviours.simpleAnswerer = this.behaviours._default.extnd({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		botLog(that.id,"registerSuccessful",config.getUserPart()+" |simpleAnswerer");
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var delay;
		botLog(that.id,"incomingCall",sipRequest);
		if(maybe(80,100)){
			delay = getRandomInt(500,1000);
			botLog(that.id,"answer in "+delay,callId);
			setTimeout(function(){
				delay = getRandomInt(2500,5000);
				that.ua.acceptCallByCallId(callId);
				botLog(that.id,"terminate in "+delay,callId);
				setTimeout(function(){
					that.ua.terminateByCallId(callId);
					botLog(that.id,"hang up",callId);
				},delay);
			},delay);
		}else{
			botLog(this.id,"refuse=>busy",callId);
			that.ua.busyByCallId(callId);
		}
	}
});