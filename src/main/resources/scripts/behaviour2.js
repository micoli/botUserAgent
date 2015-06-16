this.behaviour2 = this.behaviour.extend({
	registerSuccessful : function(args /*sipResponse,config*/){
		print(this.id+" :: registerSuccessful",args[1].getUserPart()+" behaviour2");
	},
});
