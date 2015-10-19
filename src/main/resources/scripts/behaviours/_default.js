this.behaviours._default = window.Class.extnd({
	id		: '',
	config	: null,
	ua		: null,

	init: function(id,config,ua) {
		var that = this;
		that.id		= id;
		that.ua		= ua;
		that.config	= config;
		botLog(that.id, "init");
		console.log(that.ua.ping(new CommandArgs(new JSONObject({
			"val1":1
		}))));
		that.preInit();
		that.ua.register();
	},

	preInit: function() {
	},

	tick: function() {
		//print(this.id+" ::tick ");
	},

	registering : function(sipRequest,config){
		botLog(this.id,"registering",config.getUserPart());
	},

	registerSuccessful : function(sipResponse,config){
		botLog(this.id,"registerSuccessful",config.getUserPart());
	},

	registerFailed : function(sipResponse,config,callId){
		botLog(this.id,"registerFailed",config.getUserPart());
	},

	incomingCall : function(sipRequest,provResponse){
		botLog(this.id,"incomingCall",sipRequest);
	},

	remoteHangup : function(sipRequest,callId){
		botLog(this.id,"remoteHangup",callId);
	},

	ringing : function(sipResponse){
		botLog(this.id,"ringing",sipResponse);
	},

	calleePickup : function(sipResponse){
		botLog(this.id,"calleePickup",sipResponse);
	},

	error : function(sipResponse){
		botLog(this.id,"error",sipResponse);
	},

	setInviteSipRequest : function(sipRequest){
		botLog(this.id,"setInviteSipRequest",sipRequest);
	},

	externalCommand : function(method, args){
		var that = this;
		botLog(that.id,"externalCommand ",method);
		if(method=='call'){
			// 201 call 101@10.80.0.95
			that.ua.call("sip:"+args[0]);
		}
	}
});
