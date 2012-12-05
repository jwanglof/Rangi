Filters = {};
Filters.filterImage = function(filter, image_data, var_args) {
	var args = [image_data];
	for (var i=2; i<arguments.length; i++) {
		args.push(arguments[i]);
	}
	return filter.apply(null, args);
};

Filters.grayscale = function(image_data, args) {
	var d = image_data.data;
	for (var i=0; i<d.length; i+=4) {
		var r = d[i];
		var g = d[i+1];
		var b = d[i+2];
		// CIE luminance for the RGB
		// The human eye is bad at seeing red and blue, so we de-emphasize them.
		var v = 0.2126*r + 0.7152*g + 0.0722*b;
		d[i] = d[i+1] = d[i+2] = v;
	}
	return image_data;
};
