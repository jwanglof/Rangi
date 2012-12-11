function create_color_element(color) {
	// TODO: Add support for representations other than hex

	var color_repr = color["hex"];

	var box = $(document.createElement("div")).addClass("color_box")
	var container = $(document.createElement("div")).addClass("color_container");
	var swatch = $(document.createElement("div")).addClass("color_swatch");
	var name = $(document.createElement("span")).addClass("name");
	var value = $(document.createElement("span"));

	swatch.css("backgroundColor", color_repr["value"]);
	name.text(color["name"]);
	value.text(color_repr["value"]);

	container.append(swatch).append(name).append("<br />").append(value);
	box.append(container);
	$("#colors").append(box);
	box.addClass("bring_in");
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

function save_name(text_field) {
	text_field.blur();

	var new_name = text_field.val();
	var color_id = text_field.parent(".color_box").attr("data-color-id")
	var args = {
		"name": new_name,
		"color_id": color_id
	}

	$.post("/update", args, function(response) {
		console.log("Saved color: " + response.success);
	}, "json");
}

function toggle_swatch(swatch) {
	var new_height = "80px";
	if(swatch.css("height") == new_height) {
		new_height = "140px";
	}
	swatch.stop().animate({"height": new_height}, "fast", "easeOutCubic");
}

function delete_color(color_id) {
	if(color_id == undefined) return;

	$.post("/delete", {"color_id": color_id}).success(function(response) {
		$(".color_box[data-color-id='" + color_id + "']").fadeOut("fast", function() { $(this).remove(); });
	}).error(function(response) {
		console.log("Error");
	});
}

$(document).ready(function() {
	start_worker();

	$("#colors").on("click", ".color_swatch", function() {
		toggle_swatch($(this));
	});

	$(".name").change(function() {
		save_name($(this));
	});

	$(".color_box").on("click", "img.delete", function() {
		delete_color($(this).attr("data-color-id"));
	});
});
