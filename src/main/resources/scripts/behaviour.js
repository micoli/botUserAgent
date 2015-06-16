this.behaviour = Backbone.Model.extend({
	defaults: {
		id: '',
		config:{},
		ua:{}
	},

	initialize: function() {
		print("init ",this.get('id'));
		this.get('ua').register();
	},

	registering : function(args /*sipRequest,config*/){
		print(this.id+" :: registering",args[1].getUserPart());
	},
	registerSuccessful : function(args /*sipResponse,config*/){
		var that = this;
		print(this.id+" :: registerSuccessful",args[1].getUserPart());
		setTimeout(function(){
			that.get('ua').call("sip:101@10.80.0.95");
		},500);
	},
	registerFailed : function(args/*sipResponse,config*/){
		print(this.id+" :: registerFailed",args[1].getUserPart());
	},

	incomingCall : function(args/*sipRequest,provResponse*/){
		print(this.id+" :: incomingCall",args[0]);
	},

	remoteHangup : function(args/*sipRequest*/){
		print(this.id+" :: remoteHangup",args[0]);
	},

	ringing : function(args/*sipResponse*/){
		print(this.id+" :: ringing",args[0]);
	},

	calleePickup : function(args/*sipResponse*/){
		print(this.id+" :: calleePickup",args[0]);
	},
	error : function(sipResponse){
		print(this.id+" :: error",args[0]);
	},

	setInviteSipRequest : function(args/*sipRequest*/){
		print(this.id+" :: setInviteSipRequest",args[0]);
	}

});
