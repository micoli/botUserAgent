this.behaviours._default = Backbone.Model.extend({
	defaults: {
		id		: '',
		config	: null,
		ua		: null
	},

	initialize: function() {
		print("init ",this.get('id'));
		this.get('ua').register();
	},

	tick: function() {
		//print(this.id+" ::tick ");
	},

	registering : function(sipRequest,config){
		print(this.id+" :: registering",config.getUserPart());
	},

	registerSuccessful : function(sipResponse,config){
		print(this.id+" :: registerSuccessful",config.getUserPart());
	},

	registerFailed : function(sipResponse,config,callId){
		print(this.id+" :: registerFailed",config.getUserPart());
	},

	incomingCall : function(sipRequest,provResponse){
		print(this.id+" :: incomingCall",sipRequest);
	},

	remoteHangup : function(sipRequest){
		print(this.id+" :: remoteHangup",sipRequest);
	},

	ringing : function(sipResponse){
		print(this.id+" :: ringing",sipResponse);
	},

	calleePickup : function(sipResponse){
		print(this.id+" :: calleePickup",sipResponse);
	},
	error : function(sipResponse){
		print(this.id+" :: error",sipResponse);
	},

	setInviteSipRequest : function(sipRequest){
		print(this.id+" :: setInviteSipRequest",sipRequest);
	},

	externalCommand : function(method, args){
		print(this.id+" :: externalCommand ",method);
		_.each(args,function(v,k){
			print(">> args ["+k+"] "+v);
		})
		if(method=='call'){
			// 201 call 101@10.80.0.95
			this.get('ua').call("sip:"+args[0]);
		}
	}
});
