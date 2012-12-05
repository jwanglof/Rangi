from flask import Flask
from flask import request, abort, jsonify, session, render_template
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

# ==================== API ==================== 

@app.route("/", methods = ["GET"])
def index():
	if session.get("id", False):
		user = db.find_user({"_id": session["id"]})
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
	
	username = request.form["username"]
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
	username = request.form["username"].strip()
	password = request.form["password"].strip()
	user = db.validate_credentials(username, password)	

	if not user == None:
		session["id"] = user["_id"]
		return jsonify({"success": True})

	return jsonify({"success": False})

@app.route("/save", methods = ["POST"])
def save_color():
	# Check that the user is logged in
	user = db.find_user({"_id": session["id"]})
	if not user:
		print "Not logged in"
		return failed()

	# Make sure at least the hex representation
	# was sent
	color = request.json
	if not color.get("hex", False):
		print "No hex"
		return failed()

	# Add the color
	if not db.add_color(user, color):
		print "Failed to save color"
		return failed()

	return succeeded()

if __name__ == "__main__":
	app.debug = True
	app.run(host="0.0.0.0", threaded=True)
