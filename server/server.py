from flask import Flask
from flask import request, abort, jsonify, session, render_template, redirect, url_for
import database as db
import os

import utils

app = Flask(__name__)
app.secret_key = b'\xdc\xf3\xa0\\\xd9\xc8\xa8o87\x19\xc2\xdf\x88\x8f\xbf"-\x0f\x15\xe9l4l'


# ==================== Utilities ==================== 
def failed():
	return jsonify({"success": False})

def succeeded():
	return jsonify({"success": True})

def logged_in():
	if not session.get("id"): return False
	user = db.find_user({"_id": session["id"]})
	return not user == None

def logged_in_user():
	if not logged_in(): return None
	return db.find_user({"_id": session["id"]})

# ==================== API ==========================

color_queue = {}

@app.route("/", methods = ["GET"])
def index():
	if session.get("id", False):
		user = db.find_user({"_id": session["id"]})
		# Clear the color queue
		color_queue[session["id"]] = []
		return render_template('colors.html', user=user)
	else:
		return render_template('index.html')

@app.route("/register", methods = ["POST"])
def register():
	# Can't register another user if you're already logged in
	if session.get("username", False): 
		return jsonify({"success": False, "error": "Logged in."})

	# Make sure all fields are filled in
	if not utils.valid_registration_input(request.form):
		return jsonify({
			"success": False,
			"error": "Invalid fields."
		})
	
	username = request.form["username"].lower()
	password = request.form["password"]
	password_repeat = request.form["password_repeat"]
	email = request.form["email"]

	# We don't want people to register with
	# an already taken username
	if not db.username_is_available(username):
		return jsonify({
			"success": False,
			"error": "Username already taken."
		})

	print "Registered", username

	salt = os.urandom(32).encode('hex')
	user = {
		"username": username,
		"password": utils.hashed_password(password, salt),
		"salt": salt,
		"email": email,
		"logged_in": False,
		"colors": []
	}

	db.save_user(user)

	return jsonify({"success": True})

@app.route("/login", methods = ["POST"])
def login():
	username = request.form.get("username", "").strip().lower()
	password = request.form.get("password", "").strip()
	user = db.validate_credentials(username, password)	

	if not user == None:
		session["id"] = user["_id"]
		return jsonify({"success": True})

	return jsonify({"success": False})

@app.route("/logout", methods = ["GET"])
def logout():
	if not logged_in():
		return redirect(url_for("index"))

	session.pop('id', None)
	return redirect(url_for("index"))

@app.route("/save", methods = ["POST"])
def save_color():
	if not logged_in():
		return failed()

	# Make sure at least the hex representation
	# was sent
	color = request.json
	if not color.get("hex", False):
		return failed()

	if not color.get("name"):
		color["name"] = "Unnamed"

	# Add the color
	user = logged_in_user()
	if not db.add_color(user, color):
		return failed()

	print "Saved", color, "to", user["username"]
	# Queue the color so we can send
	# it to the client
	#if not color_queue.get(session["id"]):
	#	color_queue[session["id"]] = []
	#color_queue[session["id"]].append(color)

	return succeeded()

@app.route("/colors", methods = ["GET"])
def colors():
	""" Returns the logged in users colors """

	if not logged_in():
		return failed()
	
	user = logged_in_user()
	return jsonify({"success": True, "colors": user.colors})

@app.route("/color_queue", methods = ["GET"])
def poll_queue():
	if not logged_in():
		return failed()

	if not color_queue.get(session["id"]):
		return failed()

	colors = color_queue[session["id"]]
	color_queue[session["id"]] = []

	response = jsonify({"success": True, "colors": colors})
	print {"success": True, "colors": colors}
	return response

@app.route("/diff", methods = ["POST"])
def diff_colors():
	if not logged_in():
		return failed()

	if not request.json:
		return failed()

	fetched_color_ids = request.json["colors"]
	all_colors = db.colors_for_user(session["id"])

	def has_color(color_list, color_id):
		filtered_list = filter(lambda c: c.get("_id") == color_id, color_list)
		return len(filtered_list) > 0

	diff = {
		"added": [c for c in all_colors if c.get("_id") not in fetched_color_ids],
		"removed": [c_id for c_id in fetched_color_ids if not has_color(all_colors, c_id)]
	}

	response = {
		"success": True,
		"diff": diff
	}

	return jsonify(response)

@app.route("/update", methods = ["POST"])
def update_color():
	if not logged_in():
		return failed()

	color_id = request.form["color_id"]
	new_name = request.form["name"]
	db.update_color_name(session["id"], color_id, new_name)
	
	return succeeded()

@app.route("/delete", methods = ["POST"])
def delete_color():
	if not logged_in():
		return failed()

	color_id = request.form["color_id"]
	db.delete_color(session["id"], color_id)

	return succeeded()

if __name__ == "__main__":
	app.debug = True
	app.run(host="0.0.0.0", threaded=True)
