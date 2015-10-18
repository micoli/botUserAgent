(function(){
	var separator="\t";

	var _parseAnsi = function(str){
		return str.replace(/{{\s*[\w\.]+\s*}}/g,function(match) {
			var tag = match.substr(2,match.length-4);
			return (rstyle.hasOwnProperty(tag))?('\033'+rstyle[tag]):match;
		});
	};

	var _simple = function() {
		java.lang.System.out.println((_.map(arguments,_reprs)).join(separator));
	};

	var _out = function(str){
		print(str);
	};

	var _log = function() {
		_out((_.map(arguments,_reprc)).join(separator));
	};

	var _info = function() {
		_out(_parseAnsi(_color("cyan","INFO "		))+(_.map(arguments,_reprc)).join(separator));
	};

	var _debug = function() {
		_out(_parseAnsi(_color("magenta","DEBUG "	))+(_.map(arguments,_reprc)).join(separator));
	};

	var _error = function() {
		_out(_parseAnsi(_color("red","ERROR "		))+(_.map(arguments,_reprc)).join(separator));
	};

	var _color = function(color,str){
		return "{{color_"+color+"}}"+str+"{{reset}}";
	};

	var _reprc=function(o){
		return _repr(o,true);
	}
	var _reprs=function(o){
		return repr(o,false);
	}

	var _repr = function(o,doAnsi){
		var cache = [];
		if(doAnsi && typeof o === 'string'){
			return _parseAnsi(''+o);
		}
		var r = JSON.stringify(o, function(key, value) {
			if (typeof value === 'object' && value !== null) {
				if (cache.indexOf(value) !== -1) {
					return '[circular]';
				}
				cache.push(value);
			}
			if (typeof value === 'string'){
				//return  ""+_parseAnsi(_color("green",value));
			}

			return value;
		});
		cache = null;
		return r;
	}

	var styles={
		'[0m' : 'reset',
		'[1m' : 'bold_on',
		'[3m' : 'italics_on',
		'[4m' : 'underline_on',
		'[7m' : 'inverse_on',
		'[9m' : 'strikethrough_on',
		'[22m' : 'bold_off',
		'[23m' : 'italics_off',
		'[24m' : 'underline_off',
		'[27m' : 'inverse_off',
		'[29m' : 'strikethrough_off',
		'[30m' : 'color_black',
		'[31m' : 'color_red',
		'[32m' : 'color_green',
		'[33m' : 'color_yellow',
		'[34m' : 'color_blue',
		'[35m' : 'color_magenta',
		'[36m' : 'color_cyan',
		'[37m' : 'color_white',
		'[39m' : 'color_default',
		'[40m' : 'background_black',
		'[41m' : 'background_red',
		'[42m' : 'background_green',
		'[43m' : 'background_yellow',
		'[44m' : 'background_blue',
		'[45m' : 'background_magenta',
		'[46m' : 'background_cyan',
		'[47m' : 'background_white',
		'[49m' : 'background_default'
	};
	var rstyle=_.invert(styles);

	console = {
		log			: _log,
		info		: _info,
		debug		: _debug,
		error		: _error,
		parseAnsi	: _parseAnsi
	};

	var funcs = [
		"assert", "cd", "clear", "count", "countReset",
		"debug", "dir", "dirxml", "error", "exception",
		"groupEnd", "info", "log", "markTimeline", "profile",
		"select", "table", "time", "timeEnd", "timeStamp",
		"timelineEnd", "trace", "warn","timeline",
		"profileEnd", "group", "groupCollapsed",
	];
	for(var k in funcs){
		if(!console.hasOwnProperty(funcs[k])){
			console[funcs[k]]=function(){};
		}
	}
})();