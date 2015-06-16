load('src/main/resources/scripts/underscore-min.js');
load('src/main/resources/scripts/backbone-min.js');


var Platform = Java.type("javafx.application.Platform");
var Timer    = Java.type("java.util.Timer");

function setTimerRequest(handler, delay, interval, args) {
	handler = handler || function() {};
	delay = delay || 0;
	interval = interval || 0;

	var applyHandler = function() handler.apply(this, args);
	var runLater = function() Platform.runLater(applyHandler);

	var timer = new Timer("setTimerRequest", true);

	if (interval > 0) {
		timer.schedule(runLater, delay, interval);
	} else {
		timer.schedule(runLater, delay);
	}

	return timer;
}

function clearTimerRequest(timer) {
	timer.cancel();
}

function setInterval() {
	var args = Array.prototype.slice.call(arguments);
	var handler = args.shift();
	var ms = args.shift();

	return setTimerRequest(handler, ms, ms, args);
}

function clearInterval(timer) {
	clearTimerRequest(timer);
}

function setTimeout() {
	var args = Array.prototype.slice.call(arguments);
	var handler = args.shift();
	var ms = args.shift();

	return setTimerRequest(handler, ms, 0, args);
}

function clearTimeout(timer) {
	clearTimerRequest(timer);
}

function setImmediate() {
	var args = Array.prototype.slice.call(arguments);
	var handler = args.shift();

	return setTimerRequest(handler, 0, 0, args);
}

function clearImmediate(timer) {
	clearTimerRequest(timer);
}

print("loaded");