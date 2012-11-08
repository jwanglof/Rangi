import database as db
import hashlib

def valid_registration_input(form):
	user = {}
	for k in form:
		user[k] = form[k].strip()

	required = [
		"username",
		"password",
		"password_repeat",
		"email"
	]

	if not all(map(lambda k: k in user, required)): return False

	if len(user["username"]) == 0: return False 
	if len(user["password"]) == 0: return False
	if len(user["email"]) == 0: return False

	if user["password"] != user["password_repeat"]: return False
	if not all(map(lambda c: c in user["email"], "@.")): return False

	return True

def hashed_password(password, salt):
	return hashlib.sha256(password + salt).hexdigest()
