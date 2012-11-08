import pymongo
import utils

db = pymongo.Connection().rangi

def save_user(user):
	db.users.insert(user)

def username_is_available(username):
	if db.users.find_one({"username": username}):
		return False
	return True

def validate_credentials(username, password):
	user = db.users.find_one({"username": username})
	if not user:
		return False

	hashed_password = utils.hashed_password(password, user["salt"])

	return user["password"] == hashed_password
