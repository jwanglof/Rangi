package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class LoginScreen extends Activity {
    private EditText username;
    private EditText password;

    URL url;
    URLConnection urlConn;
    HttpURLConnection httpConn;
    DataOutputStream printout;
    DataInputStream input;
    String content;
    String str;
    String result = "";
    org.json.JSONObject inputJson;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
    }

    public void login(View view) {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        try {
            /*
             * FIX!
             * When the user hits the back-button when signed in he will be transfered out of the app, not back to the StartScreen!!
             * Thought finish() would fix this but it doesn't seem so.
             * Do we need this in Register too?
             */
            try {
                SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
                boolean user_login = userSettings.getBoolean("CONFIG_USER_LOGIN", false);

                /*
                 * Go to the Main Menu if the user already has signed in
                 * Checks CONFIG_USER_LOGIN in SharedPreferences
                 */
                if (user_login) {
                    Intent gotoMainMenu = new Intent(LoginScreen.this, MainMenu.class);
                    startActivity(gotoMainMenu);
                    finish();
                }
                else {
                    url = new URL(Data.SERVER_ADDRESS + "login");
                    // URL Connection Channel
                    urlConn = url.openConnection();
                    httpConn = (HttpURLConnection) urlConn;
                    // Activate input data
                    urlConn.setDoInput(true);
                    // Activate output data
                    urlConn.setDoOutput(true);
                    // Turn of caching
                    urlConn.setUseCaches(false);
                    // Content type
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    printout = new DataOutputStream(urlConn.getOutputStream());
                    content = "password=" + URLEncoder.encode(password.getText().toString()) +
                            "&username=" + URLEncoder.encode(username.getText().toString());
                    printout.writeBytes(content);
                    printout.flush();
                    printout.close();

                    // Get response data
                    input = new DataInputStream(urlConn.getInputStream());

                    while (null != (str = input.readLine())) {
                        result += str;
                    }

                    // Get the JSON object from the server
                    inputJson = new org.json.JSONObject(result);

                    /*
                     * Log in successfull
                     */
                    if (inputJson.getBoolean("success")) {
                        /*
                        * User session IN APP
                        * Add TRUE to CONFIG_USER_LOGIN in SharedPreferences
                        * This will be saved in the app so the user won't have to sign in every time the app is opened
                        */
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putBoolean("CONFIG_USER_LOGIN", true);
                        editor.putString("CONFIG_USER_USERNAME", username.getText().toString());
                        editor.commit();

                        Toast.makeText(LoginScreen.this, "Login successfull", Toast.LENGTH_SHORT).show();

                        /*
                         * Add a delay before the user sees the Main Menu
                         * Is this neccessary?
                         */
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            public void run() {
                                // Go to the Main Menu
                                Intent gotoMainMenu = new Intent(LoginScreen.this, MainMenu.class);
                                startActivity(gotoMainMenu);
                            }

                        }, 1500); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called

                        // make sure we close the login screen so the user won't come back when it presses back key
                        // Not sure about this either. Do we need to close this?
                        finish();
                    }
                    /*
                     * Log in unsuccessfull
                     */
                    else {
                        Toast.makeText(LoginScreen.this, "Login not successfull!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            catch (Exception e) {
                System.out.println(e);
            }

        }
        catch (Exception e) {
            Toast.makeText(LoginScreen.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}