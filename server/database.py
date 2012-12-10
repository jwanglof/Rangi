import pymongo
import utils

from bson.objectid import ObjectId

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
		return None

	hashed_password = utils.hashed_password(password, user["salt"])

	if user["password"] == hashed_password: return user
	return None

def find_user(query):
	return db.users.find_one(query)

def add_color(user, color):
	if not user: return False
	if not color: return False

	color["_id"] = str(ObjectId())
	db.users.update(user, {"$push": {"colors": color}})

	return True

def update_color_name(user_id, color_id, new_name):
	query = {"_id": user_id, "colors._id": color_id}
	updated_doc = {"$set": {"colors.$.name": new_name}}
	db.users.update(query, updated_doc)

	# TODO: Error handling
