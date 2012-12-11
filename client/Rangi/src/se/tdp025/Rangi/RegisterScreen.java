package se.tdp025.Rangi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterScreen extends Activity {
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText password_repeat;

    URL url;
    URLConnection urlConn;
    HttpURLConnection httpConn;
    DataOutputStream printout;
    DataInputStream input;
    String content;
    String str;
    org.json.JSONObject inputJson;
    String result = "";

    /**
     * Called when the activity is first created.
     */
    //@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerscreen);
    }

    public void register(View view) {
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        password_repeat = (EditText) findViewById(R.id.password_repeat);

        /*
         * Make sure that the username doesn't contain any spaces
         */
        Pattern p = Pattern.compile("\\s");
        Matcher m = p.matcher(username.getText().toString());
        if (!m.find()) {
            try {
                url = new URL(Data.SERVER_ADDRESS + "register");
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
                content = "username=" + URLEncoder.encode(username.getText().toString()) +
                        "&password=" + URLEncoder.encode(password.getText().toString()) +
                        "&password_repeat=" + URLEncoder.encode(password_repeat.getText().toString()) +
                        "&email=" + URLEncoder.encode(email.getText().toString());
                printout.writeBytes(content);
                printout.flush();
                printout.close();

                // Get response data
                input = new DataInputStream(urlConn.getInputStream());

                while (null != (str = input.readLine())) {
                    result += str;
                }

                try {
                    inputJson = new org.json.JSONObject(result);

                    if (inputJson.getBoolean("success")) {
                        // Make sure that the user can't go back to RegisterScreen
                        //finish();

                        Toast.makeText(RegisterScreen.this, "Registration successfull. Hang tight and you'll be sent to the Main Menu!", Toast.LENGTH_SHORT).show();

                        /*
                        * User session IN APP
                        * Add TRUE to CONFIG_USER_LOGIN in SharedPreferences
                        * This will be saved in the app so the user won't have to sign in every time the app is opened
                        */
                        SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
                        boolean user_login = userSettings.getBoolean("CONFIG_USER_LOGIN", false);
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putString("CONFIG_USER_USERNAME", username.getText().toString());
                        editor.putBoolean("CONFIG_USER_LOGIN", true);
                        editor.commit();

                        Handler handler = new Handler();
                        // run a thread after 2 seconds to start the Main Menu
                        handler.postDelayed(new Runnable() {

                            public void run() {
                                // Go to the Main Menu
                                Intent gotoMainMenu = new Intent(RegisterScreen.this, MainMenu.class);
                                //gotoMainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(gotoMainMenu);
                            }

                        }, 2000); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called
                    }
                    else {
                        Toast.makeText(RegisterScreen.this, inputJson.get("error").toString(), Toast.LENGTH_SHORT).show();

                        // Let the user view the register screen again instead of jumping back to StartScreen
                        Intent registerScreen = new Intent(RegisterScreen.this, RegisterScreen.class);
                        startActivity(registerScreen);
                    }

                }
                catch (Exception e) {
                    System.out.println(e);
                }

                input.close();
            }
            catch (Exception e) {
                Toast.makeText(RegisterScreen.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(RegisterScreen.this, "Your username contains spaces. Please remove these and try to register again.", Toast.LENGTH_SHORT).show();
        }

    }
}
