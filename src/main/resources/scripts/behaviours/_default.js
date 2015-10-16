this.behaviours._default = Backbone.Model.extend({
	defaults: {
		id		: '',
		config	: null,
		ua		: null
	},

	initialize: function() {
		jsLog(this.get('id'), "init");
		this.preInit()
		this.get('ua').register();
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
		jsLog(this.id,"externalCommand ",method);
		_.each(args,function(v,k){
			jsLog(this.id,">> args ["+k+"] "+v);
		})
		if(method=='call'){
			// 201 call 101@10.80.0.95
			this.get('ua').call("sip:"+args[0]);
		}
	}
});
