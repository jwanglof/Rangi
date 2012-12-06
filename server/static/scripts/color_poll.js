function poll_colors(callback) {
	// Check for new wallposts
	var request = new XMLHttpRequest();

	// Open a synchronous AJAX-request
	request.open('GET', '/color_queue', false);
	request.send();

	// Handle response
	var response = JSON.parse(request.responseText);
	if(response.success)
		callback(response.colors);

	setTimeout(function() {
		poll_colors(callback);
	}, 1000);
}

addEventListener("message", function(e) {
	var data = e.data;
	if(data.message == "start") {
		poll_colors(function(colors) {
			postMessage({message: "new", colors: colors});
		});
	}
}, false);
