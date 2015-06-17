this.behaviour2 = this.behaviour.extend({
	registerSuccessful : function(sipResponse,config){
		print(this.id+" :: registerSuccessful",config.getUserPart()+" behaviour2");
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
	},

});
