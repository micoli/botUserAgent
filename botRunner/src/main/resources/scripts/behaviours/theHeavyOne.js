this.behaviours.theHeavyOne = this.behaviours._default.extnd({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		botLog(that.id,"registerSuccessful",config.getUserPart()," |behaviour2");
		setTimeout(function(){
			//that.ua.call("sip:101@10.80.0.95");
		},500);
	},

	remoteHangup : function(sipRequest){
		var that = this;
		//theHeavyOne
		//sipRequest = JSON.parse(sipRequest);
		botLog(that.id,"remoteHangup",sipRequest);
		//var m = sipRequest.sipHeaders.From.match(/sip\:([0-9]*)/);
		var m = sipRequest.match(/"from":"<sip:([0-9]*)\@(.*)"/);
		// 201 call 101@10.80.0.95
		if(m){
			setTimeout(function(){
				that.ua.call("sip:"+m[1]+"@10.80.0.95");
			},500);
		}
	}
});