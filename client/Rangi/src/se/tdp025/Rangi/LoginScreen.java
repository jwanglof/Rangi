package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String TAG = "Rangi_LoginScreen";

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
             * TODO
             * When the user hits the back-button when signed in he will be transfered out of the app, not back to the StartScreen!!
             * Thought finish() would fix this but it doesn't seem so.
             */
            try {
                // make sure we close the login screen so the user won't come back when it presses back key
                // Not sure about this either. Do we need to close this?  Or can this be done in StartScreen in some way?
                // DOES NOT WORK!!!
                //finish();

                SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
                boolean user_login = userSettings.getBoolean("CONFIG_USER_LOGIN", false);

                /*
                 * Go to the Main Menu if the user already has signed in
                 * Checks CONFIG_USER_LOGIN in SharedPreferences
                 */
                if (user_login) {
                    // Make sure that the user can't go back to LoginScreen
                    //finish();

                    Intent gotoMainMenu = new Intent(LoginScreen.this, MainMenu.class);
                    //gotoMainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(gotoMainMenu);
                }
                else {
                    /*
                     * Make sure that the user can't go back to LoginScreen
                     * STILL doesn't work!
                     * The problem now is that when the user hits the Back-button he will go to MainMenu
                     */
                    //finish();

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
                        * This will be saved in the app so the user won't have to sign in every time the app is opened
                        * Add TRUE to CONFIG_USER_LOGIN in SharedPreferences
                        * Put the user's USERNAME in CONFIG_USER_USERNAME
                        * Store the user's COOKIE in CONFIG_USER_COOKIE. This cookie is unique for each user and is used when a new color is saved in DB
                        */
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putBoolean("CONFIG_USER_LOGIN", true);
                        editor.putString("CONFIG_USER_USERNAME", username.getText().toString());
                        editor.putString("CONFIG_USER_COOKIE", httpConn.getHeaderField("Set-Cookie"));
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
                                //gotoMainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(gotoMainMenu);
                            }

                        }, 1500); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called
                    }
                    /*
                     * Log in unsuccessfull
                     */
                    else {
                        Toast.makeText(LoginScreen.this, "Login not successfull!", Toast.LENGTH_SHORT).show();

                        // Let the user view the login screen again instead of jumping back to StartScreen
                        Intent loginScreen = new Intent(LoginScreen.this, LoginScreen.class);
                        startActivity(loginScreen);
                    }
                }

            }
            catch (Exception e) {
                Toast.makeText(LoginScreen.this, "Could not connect to the server. Please try again!", Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e) {
            Toast.makeText(LoginScreen.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}