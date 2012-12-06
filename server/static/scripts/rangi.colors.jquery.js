function create_color_element(color) {
	// TODO: Add support for representations other than hex

	var color_repr = color["hex"];

	var container = $(document.createElement("div")).addClass("color_container");
	var swatch = $(document.createElement("div")).addClass("color_swatch");
	var name = $(document.createElement("span")).addClass("name");
	var value = $(document.createElement("span"));

	container.css("display", "none");
	swatch.css("backgroundColor", color_repr["value"]);
	name.text(color["name"]);
	value.text(color_repr["value"]);

	container.append(swatch).append(name).append("<br />").append(value);

	$("#colors").append(container);
	container.fadeIn("slow");
}

function start_worker() {
	var worker = new Worker(STATIC_ROOT + "color_poll.js");
	worker.addEventListener("message", function(e) {
		if(e.data.message == "new") {
			// Color have been saved from the
			// Android client
			var colors = e.data.colors;
			for(var i in colors) {
				$("#colors").append(create_color_element(colors[i]));
			}
		}
	});

	worker.postMessage({"message": "start"});
}

$(document).ready(function() {
	// Set the background color of all swatches
	$(".color_swatch").each(function(i, elm) {
		$(elm).css("backgroundColor", $(elm).attr("data-hex"));
	});

	start_worker();
});
