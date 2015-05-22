var httpGet = function(theUrl) {
	var xmlHttp = null;
	xmlHttp = new XMLHttpRequest();
	xmlHttp.open("GET", theUrl, false);
	xmlHttp.send(null);
	return xmlHttp.responseText;
};

var getPollingUrl = function() {
	var path = document.getElementById('path').value;
	var nrOfChars = document.getElementById('nrOfChars').value;
	var realPath = httpGet("WebTailServlet?path=" + path
			+ "&nrOfChars=" + nrOfChars);
	return "WebTailServlet?path=" + realPath;
};

var getDownloadUrl = function() {
	var path = document.getElementById('path').value;
	return "WebTailServlet?path=" + path + "&download=true";
};

var eventSource;

var goToWaitingState = function() {
	document.getElementById('btnStart').disabled = false;
	document.getElementById('path').disabled = false;
	document.getElementById('nrOfChars').disabled = false;
	document.getElementById('btnStop').disabled = true;
	document.getElementById('btnClear').disabled = false;
};

var goToRunningState = function() {
	pathChanged();
	document.getElementById('btnStart').disabled = true;
	document.getElementById('path').disabled = true;
	document.getElementById('nrOfChars').disabled = true;
	document.getElementById('btnStop').disabled = false;
	document.getElementById('btnClear').disabled = true;
};

var init = function() {
	document.getElementById('downloadlink').href = getDownloadUrl();
	goToWaitingState();
};

var start = function() {
	var textarea = document.getElementById('mainarea');
	eventSource = new EventSource(getPollingUrl());
	eventSource.onmessage = function(event) {
		textarea.value += event.data + "\n";
		textarea.scrollTop = textarea.scrollHeight;
	};
	goToRunningState();
};

var stop = function() {
	// eventSource.close();
	eventSource = null;
	goToWaitingState();
};

var clearLog = function() {
	document.getElementById('mainarea').readonly = false;
	document.getElementById('mainarea').value = "";
	document.getElementById('mainarea').readonly = true;
};

var pathChanged = function() {
	document.getElementById('downloadlink').href = getDownloadUrl();
};