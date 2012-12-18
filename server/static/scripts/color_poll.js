function apply_diff(color_ids, diff) {
	var result = color_ids;

	// Add all colors marked as
	// added in the diff to the result
	var i = 0;
	var added_colors = diff["added"];
	for(i in added_colors) {
		var added_color = added_colors[i];
		result.push(added_color["_id"]);
	}

	// Remove any colors marked as removed
	var removed_colors = diff["removed"];
	for(i in removed_colors) {
		var removed_color_id = removed_colors[i];
		var index = result.indexOf(removed_color_id);
		
		if(index == -1)
			continue;

		result.splice(index, 1);
	}

	return result;
}

function poll_colors(color_ids, callback) {
	// Check for new wallposts
	var request = new XMLHttpRequest();

	// Open a synchronous AJAX-request
	request.open('POST', '/diff', false);
	request.setRequestHeader("Content-Type", "application/json");
	var body = {"colors": color_ids};
	request.send(JSON.stringify(body));

	// Handle response
	var response = JSON.parse(request.responseText);
	if(response.success) {
		var diff = response.diff;
		if(diff["added"].length > 0 || diff["removed"].length > 0) {
			var result = apply_diff(color_ids, diff);
			callback(diff);

			setTimeout(function() {
				poll_colors(result, callback);
			}, 1000);
			return;
		}
	}

	setTimeout(function() {
		poll_colors(color_ids, callback);
	}, 1000);
}

addEventListener("message", function(e) {
	var data = e.data;
	if(data.message == "start") {
		poll_colors(data.color_ids, function(diff) {
			postMessage({message: "updated", diff: diff});
		});
	}
}, false);
