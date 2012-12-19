package se.tdp025.Rangi.json;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tdp025.Rangi.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class JSON {

    /*
    {"colors" :
	    [
		    {"hex" : "#ffffff", "name" : "Test", "android-color" : -122301}
	    ]
    }


     */

    private static final String TAG = "Rangi_JSON";

    /***
     * Parse a string to a JSONObject
     */
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

    /***
     * Parse a JSON from a URL
     */
    public static JSONObject parseFromURL(URL url)
    {

        return null;
    }

    /***
     * Check if the color exist in the saved JSON
     */
    public static String checkIfColorExist(int color, Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        // Get JSON string from SharedPreferences
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

    /***
     * Save a new color the saved JSON object
     */
    public static JSONObject saveToJson(String name, int color, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        SharedPreferences.Editor editor = prefs.edit();
        // Get JSON string from SharedPreferences
        String jsonString = prefs.getString(Data.SHARED_COLORS, "{'colors' : []}");
        JSONObject json = parse(jsonString);
        JSONObject newColor = null;
        try {

            JSONArray colorsArray = json.getJSONArray("colors");
            //Find if the color already exist in the JSONArray
            for(int i = 0; i < colorsArray.length(); i++)
            {
                if(colorsArray.getJSONObject(i).getInt("android-color") == color)
                {
                    return null;
                }
            }

            // The new color, here we can add the hex etc.
            newColor = new JSONObject();
            newColor.put("name", name);
            newColor.put("android-color", color);
            newColor.put("_id", color);

            colorsArray.put(newColor);

            json.put("colors", colorsArray);
            editor.putString(Data.SHARED_COLORS, jsonToString(json));
            editor.commit();
            return newColor;
        } catch (JSONException e) {
            Log.e(TAG, "saveToJson: ");
            Log.getStackTraceString(e);
        }

        return null;
    }


    public static void sendJsonToURL(URL url, JSONObject json, Context context)
    {

        try {
            Log.d(TAG, "Save to Server | Content: " + json.toString());

            URLConnection urlConn = url.openConnection();
            SharedPreferences userSettings = context.getSharedPreferences(Data.PREFS_NAME, 0);
            String cookie = userSettings.getString("CONFIG_USER_COOKIE", "");

            // Activate input data
            urlConn.setDoInput(true);
            // Activate output data
            urlConn.setDoOutput(true);
            // Turn of caching
            urlConn.setUseCaches(false);

            urlConn.setRequestProperty("Cookie", cookie);
            urlConn.setRequestProperty("Content-Type", "application/json");
            DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());

            printout.writeBytes(json.toString());
            printout.flush();
            printout.close();

            DataInputStream input = new DataInputStream(urlConn.getInputStream());
            input.close();

            Log.d(TAG, "sendJsonToURL: Cookie: " + cookie);

            /*String str = null;
            Log.v(TAG, "Response: ");
            while (null != (str = input.readLine())) {
                Log.v(TAG, str);
            } */

        } catch (Exception e) {
            Log.e(TAG, "sendJsonToURL: " + e);
            for(int i = 0; i < e.getStackTrace().length; i++)
                Log.e(TAG, e.getStackTrace()[i].toString());
        }

    }

    /***
     * JSONObject to a string
     */
    public static String jsonToString(JSONObject jsonObject) {
        return jsonObject.toString();
    }

    /*
     * Delete a color from the database
     */
    private static boolean deleteFromURL(int androidColor, Context context) {
        try {
            SharedPreferences userSettings = context.getSharedPreferences(Data.PREFS_NAME, 0);
            String cookie = userSettings.getString("CONFIG_USER_COOKIE", "");

            URL url = new URL(Data.SERVER_ADDRESS + "delete");
            // URL Connection Channel
            URLConnection urlConn2 = url.openConnection();
            // Activate input data
            urlConn2.setDoInput(true);
            // Activate output data
            urlConn2.setDoOutput(true);
            // Turn of caching
            urlConn2.setUseCaches(false);
            // Content type
            urlConn2.setRequestProperty("Cookie", cookie);
            urlConn2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            DataOutputStream printout2 = new DataOutputStream(urlConn2.getOutputStream());
            String content = "color_id=" + URLEncoder.encode(Integer.toString(androidColor));
            printout2.writeBytes(content);
            printout2.flush();
            printout2.close();

            DataInputStream input2 = new DataInputStream(urlConn2.getInputStream());

            Log.d(TAG, "deleteFromURL: androidColor: " + Integer.toString(androidColor));
            Log.d(TAG, "deleteFromURL: Cookie: " + cookie);
            Log.d(TAG, "deleteFromURL: Color deleted");

            String str = null;
            Log.v(TAG, "Response: ");
            while (null != (str = input2.readLine())) {
                Log.v(TAG, str);
            }

            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "deleteFromURL: Error - " + e);
            return false;
        }
    }

    /***
     * Delete color from the stored JSON
     */
    public static void deleteFromJSON(int color, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Data.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        String jsonString = prefs.getString(Data.SHARED_COLORS, "{'colors' : []}");
        JSONObject json = parse(jsonString);
        try {
            Log.d(TAG, "deleteFromJSON: Accepted");
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

            deleteFromURL(color, context);

            json.put("colors", newColorArray);
            editor.putString(Data.SHARED_COLORS, jsonToString(json));
            editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "deleteFromJSON: Neglected, " + e);
            Log.getStackTraceString(e);

        }
    }
}
