this.behaviours.answerHangup = this.behaviours._default.extend({
	postInit: function() {
		var that = this;
		jsLog(that.id, "postInit");
		that.audioFile = new java.io.File("/tmp/" + that.id + ".raw");
		// wget -qO- "http://mary.dfki.de:59125/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&INPUT_TEXT=poste%20six%20zero%20zero%20un.&OUTPUT_TEXT=&effect_Volume_selected=&effect_Volume_parameters=amount%3A2.0%3B&effect_Volume_default=Default&effect_Volume_help=Help&effect_TractScaler_selected=&effect_TractScaler_parameters=amount%3A1.5%3B&effect_TractScaler_default=Default&effect_TractScaler_help=Help&effect_F0Scale_selected=&effect_F0Scale_parameters=f0Scale%3A2.0%3B&effect_F0Scale_default=Default&effect_F0Scale_help=Help&effect_F0Add_selected=&effect_F0Add_parameters=f0Add%3A50.0%3B&effect_F0Add_default=Default&effect_F0Add_help=Help&effect_Rate_selected=&effect_Rate_parameters=durScale%3A1.5%3B&effect_Rate_default=Default&effect_Rate_help=Help&effect_Robot_selected=&effect_Robot_parameters=amount%3A100.0%3B&effect_Robot_default=Default&effect_Robot_help=Help&effect_Whisper_selected=&effect_Whisper_parameters=amount%3A100.0%3B&effect_Whisper_default=Default&effect_Whisper_help=Help&effect_Stadium_selected=&effect_Stadium_parameters=amount%3A100.0&effect_Stadium_default=Default&effect_Stadium_help=Help&effect_Chorus_selected=&effect_Chorus_parameters=delay1%3A466%3Bamp1%3A0.54%3Bdelay2%3A600%3Bamp2%3A-0.10%3Bdelay3%3A250%3Bamp3%3A0.30&effect_Chorus_default=Default&effect_Chorus_help=Help&effect_FIRFilter_selected=&effect_FIRFilter_parameters=type%3A3%3Bfc1%3A500.0%3Bfc2%3A2000.0&effect_FIRFilter_default=Default&effect_FIRFilter_help=Help&effect_JetPilot_selected=&effect_JetPilot_parameters=&effect_JetPilot_default=Default&effect_JetPilot_help=Help&HELP_TEXT=&exampleTexts=&VOICE_SELECTIONS=upmc-pierre-hsmm%20fr%20male%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=fr&VOICE=upmc-pierre-hsmm&AUDIO=WAVE_FILE" |  sox - -b 16 -c 1 -r 8k -t RAW /tmp/6000.raw

		//print (that.audioFile.getAbsolutePath() + "\n");
		//print (that.audioFile.lastModified() + "\n");
		//print (that.audioFile.exists() + "\n");
	},

	registerSuccessful : function(sipResponse,config){
		var that = this;
		jsLog(that.id,"registerSuccessful",config.getUserPart()," |answerHangup",sipResponse);
	},

	incomingCall : function(sipRequest,provResponse,callId){
		var that = this;
		var lng1 = getRandomInt(2000,4000);
		that.get('ua').setAnswerFile(that.audioFile)
		jsLog(that.id,"incomingCall "+callId+" will answer in "+lng1,sipRequest);
		setTimeout(function(){
			var lng = getRandomInt(4000,10000);
			jsLog(that.id,"acceptCallByCallId " + callId + ", will hang in " + lng);
			that.get('ua').acceptCallByCallId(callId);
			setTimeout(function(){
				jsLog(that.id,"terminateByCallId");
				that.get('ua').terminateByCallId(callId);
			},lng);
		},lng1);
	}
});