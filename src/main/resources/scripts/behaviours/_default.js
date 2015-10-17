this.behaviours._default = window.Class.extnd({
	id		: '',
	config	: null,
	ua		: null,

	init: function(id,config,ua) {
		print (id,config,ua);
		var that = this;

		that.id		= id;
		that.ua		= ua;
		that.config	= config;
		jsLog(that.id, "init");
		that.preInit()
		that.ua.register();
	},

	preInit: function() {
	},

	tick: function() {
		//print(this.id+" ::tick ");
	},

	registering : function(sipRequest,config){
		jsLog(this.id,"registering",config.getUserPart());
	},

	registerSuccessful : function(sipResponse,config){
		jsLog(this.id,"registerSuccessful",config.getUserPart());
	},

	registerFailed : function(sipResponse,config,callId){
		jsLog(this.id,"registerFailed",config.getUserPart());
	},

	incomingCall : function(sipRequest,provResponse){
		jsLog(this.id,"incomingCall",sipRequest);
	},

	remoteHangup : function(sipRequest,callId){
		jsLog(this.id,"remoteHangup",callId);
	},

	ringing : function(sipResponse){
		jsLog(this.id,"ringing",sipResponse);
	},

	calleePickup : function(sipResponse){
		jsLog(this.id,"calleePickup",sipResponse);
	},

	error : function(sipResponse){
		jsLog(this.id,"error",sipResponse);
	},

	setInviteSipRequest : function(sipRequest){
		jsLog(this.id,"setInviteSipRequest",sipRequest);
	},

	externalCommand : function(method, args){
		var that = this;
		jsLog(that.id,"externalCommand ",method);
		if(method=='call'){
			// 201 call 101@10.80.0.95
			that.ua.call("sip:"+args[0]);
		}
	}
});
