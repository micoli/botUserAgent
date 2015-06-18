load(workingDirectory+'/underscore-min.js');
load(workingDirectory+'/backbone-min.js');

this.bots = {};
var initBot	= function (id,config){};
var botCb	= function (id,method,args){};
var javaLog	= function(log){};
var javaNetworkLog = function(log){};

(function(context) {
'use strict';

	var Timer = Java.type('java.util.Timer');
	var Phaser = Java.type('java.util.concurrent.Phaser');
	var TimeUnit = Java.type('java.util.concurrent.TimeUnit');
	//var AsyncHttpClient = Java.type('com.ning.http.client.AsyncHttpClient');

	var timer = new Timer('jsEventLoop', false);
	var phaser = new Phaser();

	var onTaskFinished = function() {
		phaser.arriveAndDeregister();
	};

	context.setTimeout = function(fn, millis /* [, args...] */) {
		var args = [].slice.call(arguments, 2, arguments.length);

		var phase = phaser.register();
		var canceled = false;
		timer.schedule(function() {
			if (canceled) {
				return;
			}

			try {
				fn.apply(context, args);
			} catch (e) {
				print(e);
			} finally {
				onTaskFinished();
			}
		}, millis);

		return function() {
			onTaskFinished();
			canceled = true;
		};
	};

	context.clearTimeout = function(cancel) {
		cancel();
	};

	context.setInterval = function(fn, delay /* [, args...] */) {
		var args = [].slice.call(arguments, 2, arguments.length);

		var cancel = null;

		var loop = function() {
			cancel = context.setTimeout(loop, delay);
			fn.apply(context, args);
		};

		cancel = context.setTimeout(loop, delay);
		return function() {
			cancel();
		};
	};

	context.clearInterval = function(cancel) {
		cancel();
	};

	context.main = function(fn, waitTimeMillis) {
		if (!waitTimeMillis) {
			waitTimeMillis = 60 * 1000;
		}

		if (phaser.isTerminated()) {
			phaser = new Phaser();
		}

		// we register the main(...) function with the phaser so that we
		// can be notified of all cases. If we wouldn't do this, we would have a
		// race condition as `fn` could be finished before we call `await(...)`
		// on the phaser.
		phaser.register();
		setTimeout(fn, 0);

		// timeout is handled via TimeoutException. This is good enough for us.
		phaser.awaitAdvanceInterruptibly(phaser.arrive(),
		waitTimeMillis,
		TimeUnit.MILLISECONDS);

		// a new phase will have started, so we need to arrive and deregister
		// to make sure that following executions of main(...) will work as well.
		phaser.arriveAndDeregister();
	};

	context.shutdown = function() {
		timer.cancel();
		phaser.forceTermination();
	};

	context.maybe = function (prct,max){
		var rn = Math.random() * max;
		return prct>rn;
	}

	context.getRandomInt = function (min,max){
		return Math.floor(min + (Math.random() * (max - min)));
	}

	// print(this.http().get('http://www.google.com/?q=test').data);
	context.http = function() {
		function asResponse(con){
			return {
				data : read(con.inputStream),
				statusCode : con.responseCode
			};
		}

		function write(outputStream, data){
			var wr = new java.io.DataOutputStream(outputStream);
			wr.writeBytes(data);
			wr.flush();
			wr.close();
		}

		function read(inputStream){
			var inReader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
			var inputLine;
			var response = new java.lang.StringBuffer();

			while ((inputLine = inReader.readLine()) != null) {
				response.append(inputLine);
			}
			inReader.close();
			return response.toString();
		}
		return {
			get : function(theUrl){
				var con = new java.net.URL(theUrl).openConnection();
				con.requestMethod = "GET";

				return asResponse(con);
			},

			post : function (theUrl, data, contentType){
				var con = new java.net.URL(theUrl).openConnection();
				contentType = contentType || "application/json";

				con.requestMethod = "POST";
				con.setRequestProperty("Content-Type", contentType);

				con.doOutput=true;
				write(con.outputStream, data);

				return asResponse(con);
			}
		}
	};
})(this);