package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

    private static final String TAG = "Rangi_RegisterScreen";

    /*URL url;
    URLConnection urlConn;
    HttpURLConnection httpConn;
    DataOutputStream printout;
    DataInputStream input;*/
    String content;
    String str;
    org.json.JSONObject inputJson;
    String result = "";

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerscreen);
    }

    public void register(View view) {
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        password_repeat = (EditText) findViewById(R.id.password_repeat);

        //if (checkSpaces(username.getText().toString())) {
        Pattern p = Pattern.compile("\\s");
        Matcher m = p.matcher(username.getText().toString());
        if (!m.find()) {
            if (checkEmail(email.getText().toString())) {
                try {
                    URL url = new URL(Data.SERVER_ADDRESS + "register");
                    // URL Connection Channel
                    URLConnection urlConn = url.openConnection();
                    HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                    // Activate input data
                    urlConn.setDoInput(true);
                    // Activate output data
                    urlConn.setDoOutput(true);
                    // Turn of caching
                    urlConn.setUseCaches(false);
                    // Content type
                    urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());

                    content = "username=" + URLEncoder.encode(username.getText().toString()) +
                            "&password=" + URLEncoder.encode(password.getText().toString()) +
                            "&password_repeat=" + URLEncoder.encode(password_repeat.getText().toString()) +
                            "&email=" + URLEncoder.encode(email.getText().toString());
                    printout.writeBytes(content);
                    printout.flush();
                    printout.close();

                    // Get response data
                    DataInputStream input = new DataInputStream(urlConn.getInputStream());

                    while (null != (str = input.readLine())) {
                        result += str;
                    }



                    try {
                        inputJson = new org.json.JSONObject(result);

                        if (inputJson.getBoolean("success")) {
                            Toast.makeText(RegisterScreen.this, "Registration successfull. Hang tight and you'll be sent to the Main Menu!", Toast.LENGTH_SHORT).show();




                            URL url2 = new URL(Data.SERVER_ADDRESS + "login");
                            // URL Connection Channel
                            URLConnection urlConn2 = url2.openConnection();
                            HttpURLConnection httpConn2 = (HttpURLConnection) urlConn2;
                            // Activate input data
                            urlConn2.setDoInput(true);
                            // Activate output data
                            urlConn2.setDoOutput(true);
                            // Turn of caching
                            urlConn2.setUseCaches(false);
                            // Content type
                            urlConn2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            DataOutputStream printout2 = new DataOutputStream(urlConn2.getOutputStream());
                            content = "password=" + URLEncoder.encode(password.getText().toString()) +
                                    "&username=" + URLEncoder.encode(username.getText().toString());
                            printout2.writeBytes(content);
                            printout2.flush();
                            printout2.close();

                            // Get response data
                            DataInputStream input2 = new DataInputStream(urlConn2.getInputStream());

                            String str2 = "";
                            while (null != (str2 = input2.readLine())) {
                                Log.d(TAG, str2);
                            }



                            input2.close();

                                    /*
                            * User session IN APP
                            * Add TRUE to CONFIG_USER_LOGIN in SharedPreferences
                            * This will be saved in the app so the user won't have to sign in every time the app is opened
                            */
                            SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
                            SharedPreferences.Editor editor = userSettings.edit();
                            editor.putBoolean("CONFIG_USER_LOGIN", true);
                            editor.putString("CONFIG_USER_USERNAME", username.getText().toString());
                            editor.putString("CONFIG_USER_COOKIE", httpConn2.getHeaderField("Set-Cookie"));
                            editor.commit();

                            Log.d(TAG, "Hej" + httpConn2.getHeaderField("Set-Cookie"));

                            Handler handler = new Handler();
                            // run a thread after 2 seconds to start the Main Menu
                            handler.postDelayed(new Runnable() {

                                public void run() {
                                    // Go to the Main Menu
                                    Intent gotoMainMenu = new Intent(RegisterScreen.this, MainMenu.class);
                                    startActivity(gotoMainMenu);
                                }

                            }, 2000); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called
                        }
                        else if (inputJson.get("error").toString().equals("Invalid fields.")) {
                        /*
                         * If the user doesn't fill all the fields
                         * The fields will contain the value it previously had
                         */
                            Toast.makeText(RegisterScreen.this, "All fields are mandatory.", Toast.LENGTH_SHORT).show();

                            EditText editUsernameField = (EditText) findViewById(R.id.username);
                            editUsernameField.setText(username.getText().toString(), TextView.BufferType.EDITABLE);

                            EditText editEmailField = (EditText) findViewById(R.id.email_address);
                            editEmailField.setText(email.getText().toString(), TextView.BufferType.EDITABLE);
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
                Toast.makeText(RegisterScreen.this, "Not a valid e-mail address.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(RegisterScreen.this, "Your username contains spaces. Please remove these and try again.", Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Make sure that the email is valid
     */
    private boolean checkEmail(String email) {
        Pattern p = Patterns.EMAIL_ADDRESS;
        return p.matcher(email).matches();
    }

    /*
     * Make sure that the username doesn't contain any spaces
     * Does. Not. Work....
     */
    private boolean checkSpaces(String username) {

        /*Pattern p = Pattern.compile("\\s");
        Matcher m = p.matcher(username.getText().toString());*/

        Pattern p = Pattern.compile("\\s");
        return p.matcher(username).matches();
    }
}
