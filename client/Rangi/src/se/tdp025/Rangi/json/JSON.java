package se.tdp025.Rangi.json;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tdp025.Rangi.Data;

import java.net.URL;

public class JSON {

    /*
    {"colors" :
	    [
		    {"hex" : "#ffffff", "name" : "Test", "android-color" : -122301}
	    ]
    }


     */

    private static final String TAG = "Rangi_JSON";

    public static JSONObject parse(String json)
    {
        try {
            JSONObject parsed = new JSONObject(json);
            return parsed;
        } catch (JSONException e) {
            Log.e(TAG, "parse: ");
            Log.getStackTraceString(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static JSONObject parseFromURL(URL url)
    {

        return null;
    }

    public static String checkIfColorExist(int color, Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        String jsonString = prefs.getString(Data.SHARED_COLORS, "{'colors' : []}");
        JSONObject json = parse(jsonString);
        try {

            JSONArray colorsArray = json.getJSONArray("colors");
            //Find if the color already exist in the JSONArray
            for(int i = 0; i < colorsArray.length(); i++) {
                if(colorsArray.getJSONObject(i).getInt("android-color") == color) {
                    return colorsArray.getJSONObject(i).getString("name");
                }
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "checkIfColorExist: ");
            Log.getStackTraceString(e);
        }
        return null;
    }

    public static boolean saveToJson(String name, int color, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        SharedPreferences.Editor editor = prefs.edit();
        String jsonString = prefs.getString(Data.SHARED_COLORS, "{'colors' : []}");
        JSONObject json = parse(jsonString);
        try {

            JSONArray colorsArray = json.getJSONArray("colors");
            //Find if the color already exist in the JSONArray
            for(int i = 0; i < colorsArray.length(); i++)
            {
                if(colorsArray.getJSONObject(i).getInt("android-color") == color)
                {
                    return false;
                }
            }

            JSONObject newColor = new JSONObject();
            newColor.put("name", name);
            newColor.put("android-color", color);

            colorsArray.put(newColor);

            json.put("colors", colorsArray);
            editor.putString(Data.SHARED_COLORS, jsonToString(json));
            return editor.commit();
        } catch (JSONException e) {
            Log.e(TAG, "saveToJson: ");
            Log.getStackTraceString(e);
        }

        return false;
    }

    public static String jsonToString(JSONObject jsonObject) {
       return jsonObject.toString();
    }

    public static void deleteFromJSON(int color, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        SharedPreferences.Editor editor = prefs.edit();
        String jsonString = prefs.getString(Data.SHARED_COLORS, "{'colors' : []}");
        JSONObject json = parse(jsonString);
        try {

            JSONArray colorsArray = json.getJSONArray("colors");
            JSONArray newColorArray = new JSONArray();
            //Find if the color already exist in the JSONArray
            for(int i = 0; i < colorsArray.length(); i++)
            {
                // Rebuild array and skip matching color
                if(!(colorsArray.getJSONObject(i).getInt("android-color") == color))
                {
                    newColorArray.put(colorsArray.getJSONObject(i));
                }
            }

            json.put("colors", newColorArray);
            editor.putString(Data.SHARED_COLORS, jsonToString(json));
            editor.commit();
        } catch (JSONException e) {
            Log.e(TAG, "deleteFromJSON: ");
            Log.getStackTraceString(e);
        }

    }
}
