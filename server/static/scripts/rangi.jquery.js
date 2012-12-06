var colors = [
	"#63816a",
	"#aa8a58",
	"#e99147",
	"#eeb362",
	"#f4d176"
];

function draw_swatches(swatch_width, swatch_height) {
	var canvas_elm = $("#canvas");
	var canvas = $("#canvas")[0];
	if(canvas.getContext) {
		var ctx = canvas.getContext("2d");
		var num_x = Math.ceil(canvas_elm.width()/swatch_width);
		var num_y = Math.ceil(canvas_elm.height()/swatch_height);

		var i = 0;
		var index = 0;
		while(i < num_y) {
			var j = 0;
			while(j < num_x) {
				var index = Math.round(Math.random() * (colors.length - 1));
				ctx.fillStyle = colors[index];
				
				ctx.fillRect(j * swatch_width, i * swatch_height, swatch_width, swatch_height);

				j++;
			}
			i++;
		}

		return num_x * num_y;
	}

	return 0;
}

image_data = undefined;
function save_canvas(canvas) {
	var ctx = canvas.getContext('2d');
	image_data = ctx.getImageData(0, 0, canvas.width, canvas.height);
}

function restore_canvas(canvas) {
	var ctx = canvas.getContext('2d');
	ctx.putImageData(image_data, 0, 0);
}

function fill_width(swatch_width, swatch_height, x_offset) {
	var canvas = $("#canvas");
	var canvas_elm = canvas[0];

	// Resize the canvas
	var c = Math.ceil($(window).width()/swatch_width) + 1;
	var new_width = c * swatch_width;
	save_canvas(canvas_elm);
	canvas.attr("width", new_width);
	restore_canvas(canvas_elm);

	// Draw the new column(s)
	var delta = (new_width - x_offset)/swatch_width;
	var y_count = Math.ceil(canvas.height()/swatch_height);
	var ctx = canvas_elm.getContext('2d');
	for(var i = 0; i < delta; i++) {
			for(var j = 0; j < y_count; j++) {
				var index = Math.round(Math.random() * (colors.length - 1));
				ctx.fillStyle = colors[index];
				ctx.fillRect(x_offset + i * swatch_width, j * swatch_height, swatch_width, swatch_height);
			}
	}

	return delta;
}

function fill_height(swatch_width, swatch_height, y_offset) {
	var canvas = $("#canvas");
	var canvas_elm = canvas[0];

	// Resize the canvas
	var c = Math.ceil($(window).height()/swatch_height) + 1;
	var new_height = c * swatch_height;
	save_canvas(canvas_elm);
	canvas.attr("height", new_height);
	restore_canvas(canvas_elm);

	// Draw the new row(s)
	var column_delta = (new_height - y_offset)/swatch_height;
	var x_count = Math.ceil(canvas.width()/swatch_width);
	var ctx = canvas_elm.getContext('2d');
	for(var i = 0; i < column_delta; i++) {
			for(var j = 0; j < x_count; j++) {
				var index = Math.round(Math.random() * (colors.length - 1));
				ctx.fillStyle = colors[index];
				ctx.fillRect(j * swatch_width, y_offset + i * swatch_height, swatch_width, swatch_height);
			}
	}

	return column_delta;
}

function login() {
	var params = $("form[action='/login']").serialize();
	$.post('/login', params, function(response) {
		if(response.success)
			console.log("Logged in");
		else
			$("#alert").slideDown("fast");
	}, 'json');
}

$(document).ready(function() {
	var w = 45.0;
	var h = 45.0;
	
	var x_count = Math.ceil($(window).width()/w);
	var y_count = Math.ceil($(window).height()/h);
	var canvas_width = x_count * w;
	var canvas_height = y_count * w;
	$("#canvas").attr("width", canvas_width);
	$("#canvas").attr("height", canvas_height);
	draw_swatches(w, h);

	$(window).resize(function() {
		var window_width = $(window).width();
		var window_height = $(window).height();

		if(window_width > x_count * w - w/2.0) {
			x_count += fill_width(w, h, x_count * w);
		}

		if(window_height > y_count * h - h/2.0) {
			y_count += fill_height(w, h, y_count * h);
		}
	});

	$("form[action='/login']").submit(function() {
		login();
		return false;
	});

	$(".close").click(function() {
		$("#" + $(this).attr("data-dismiss")).hide();
	});
});
