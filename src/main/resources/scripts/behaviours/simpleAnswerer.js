this.behaviours.simpleAnswerer = this.behaviours._default.extend({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		print(this.id+" :: registerSuccessful",config.getUserPart()+" simpleAnswerer");
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var delay;
		print(that.id+" :: incomingCall",sipRequest);
		if(maybe(80,100)){
			delay = getRandomInt(500,1000);
			print(that.id+" :: answer",delay,callId);
			setTimeout(function(){
				delay = getRandomInt(2500,5000);
				that.get('ua').acceptCallByCallId(callId);
				print(that.id+" :: terminate",delay,callId);
				setTimeout(function(){
					that.get('ua').hat.get('ua').terminateByCallId(callId);
				},delay);
			},delay);
		}else{
			print(this.id+" :: refuse=>busy",callId);
			that.get('ua').busyByCallId(callId);
		}
	}
});