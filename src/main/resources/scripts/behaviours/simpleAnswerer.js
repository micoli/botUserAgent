this.behaviours.simpleAnswerer = this.behaviours._default.extend({
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
				that.get('ua').acceptCallByCallId(callId);
				jsLog(that.id,"terminate in "+delay,callId);
				setTimeout(function(){
					that.get('ua').hat.get('ua').terminateByCallId(callId);
					jsLog(that.id,"hang up",callId);
				},delay);
			},delay);
		}else{
			jsLog(this.id,"refuse=>busy",callId);
			that.get('ua').busyByCallId(callId);
		}
	}
});