from flask import Flask
from flask import request, abort, jsonify, session
import database as db
import os

import utils

app = Flask(__name__)
app.secret_key = b'\xdc\xf3\xa0\\\xd9\xc8\xa8o87\x19\xc2\xdf\x88\x8f\xbf"-\x0f\x15\xe9l4l'

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
		"logged_in": False
	}

	db.save_user(user)

	return jsonify({"success": True})

@app.route("/login", methods = ["POST"])
def login():
	username = request.form["username"].strip()
	password = request.form["password"].strip()
	
	if db.validate_credentials(username, password):
		session["username"] = username
		return jsonify({"success": True})

	return jsonify({"success": False})

if __name__ == "__main__":
	app.debug = True
	app.run()
