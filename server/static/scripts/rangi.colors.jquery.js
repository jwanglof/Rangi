function create_color_element(color) {
	// TODO: Add support for representations other than hex

	var box = $(".prototype").clone();

	box.removeClass("prototype");
	box.addClass("bring_in");
	box.attr("data-color-id", color["_id"]);
	box.find(".color_swatch").css("backgroundColor", color["hex"]);
	box.find(".name").val(color["name"]);
	box.find(".hex").text(color["hex"]);
	if(color["hsv"] != undefined)
		box.find(".hsv").html(color["hsv"]);

	if(color["rgb"] != undefined)
		box.find(".rgb").html(color["rgb"]);

	if(color["ncs"] != undefined)
		box.find(".ncs").html(color["ncs"]);

	box.find("img.delete").attr("data-color-id", color["_id"]);

	$("#colors").append(box);
}

function append_color(color) {
	$("#colors").append(create_color_element(color));
}

function remove_color_with_id(color_id) {
	console.log("Removing " + color_id);
	$(".color_box[data-color-id='" + color_id + "']").fadeOut("fast", function() {
		$(this).remove();
	});
}

function apply_diff(diff) {
	var added_colors = diff["added"];
	var removed_color_ids = diff["removed"];

	console.log("Applying diff:");
	console.log(diff);
	for(var i in added_colors) {
		var color = added_colors[i];
		append_color(color);
	}

	for(var i in removed_color_ids) {
		var color_id = removed_color_ids[i];
		remove_color_with_id(color_id);
	}
}

function start_worker() {
	var worker = new Worker(STATIC_ROOT + "color_poll.js");
	worker.addEventListener("message", function(e) {
		if(e.data.message == "updated") {
			// Colors have been updated from another client
			var diff = e.data.diff;
			apply_diff(diff);
		} else if(e.data.message == "debug") {
			console.log(e.data.data);
		}
	});

	var color_ids = $(".color_box").map(function(i, element) {
		return $(element).attr("data-color-id");
	}).get();
	worker.postMessage({"message": "start", "color_ids": color_ids});
}

function save_name(text_field) {
	text_field.blur();

	var new_name = text_field.val();
	var color_id = text_field.parents(".color_box").attr("data-color-id")
	var args = {
		"name": new_name,
		"color_id": color_id
	}

	$.post("/update", args, function(response) {
		console.log("Saved color: " + response.success);
	}, "json");
}

function toggle_swatch(swatch) {
	var new_height = "50px";
	if(swatch.css("height") == new_height) {
		new_height = "140px";
	}
	swatch.stop().animate({"height": new_height}, "fast", "easeOutCubic");
}

function delete_color(color_id) {
	if(color_id == undefined) return;

	$.post("/delete", {"color_id": color_id}, function(response) {
		if(response.success) {
			console.log("Deleted " + color_id);
			$(".color_box[data-color-id='" + color_id + "']").fadeOut("fast", function() { $(this).remove(); });
		} else {
			console.log("Error when deleting " + color_id);
		}
	});
	
}

$(document).ready(function() {
	start_worker();

	$("#colors").on("click", ".color_swatch", function() {
		toggle_swatch($(this));
	});

	$(document).on("change", ".name", function() {
		save_name($(this));
	});

	$(document).on("click", "img.delete", function() {
		delete_color($(this).attr("data-color-id"));
	});
});
