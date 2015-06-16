this.behaviour2 = this.behaviour.extend({
	registerSuccessful : function(sipResponse,config){
		print(this.id+" :: registerSuccessful",config.getUserPart()+" behaviour2");
	}
});
