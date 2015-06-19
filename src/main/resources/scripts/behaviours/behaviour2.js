this.behaviours.behaviour2 = this.behaviours._default.extend({
	registerSuccessful : function(sipResponse,config){
		var that = this;
		print(this.id+" :: registerSuccessful",config.getUserPart()+" behaviour2");
		setTimeout(function(){
			//that.get('ua').call("sip:101@10.80.0.95");
		},500);
	},

	remoteHangup : function(sipRequest){
		var that = this;
		//LE LOURD
		//sipRequest = JSON.parse(sipRequest);
		print(this.id+" :: remoteHangup",sipRequest);
		//var m = sipRequest.sipHeaders.From.match(/sip\:([0-9]*)/);
		var m = sipRequest.match(/"from":"<sip:([0-9]*)\@(.*)"/);
		// 201 call 101@10.80.0.95
		if(m){
			setTimeout(function(){
				that.get('ua').call("sip:"+m[1]+"@10.80.0.95");
			},500);
		}
	}
});
