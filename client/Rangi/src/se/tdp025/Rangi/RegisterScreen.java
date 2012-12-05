package se.tdp025.Rangi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.*;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerscreen);
    }

    public void register(View view) {
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        password_repeat = (EditText) findViewById(R.id.password_repeat);

        try {
            url = new URL(Data.serverConnectionIP + "register");
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
                    Toast.makeText(RegisterScreen.this, "Registration successfull. Welcome!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegisterScreen.this, inputJson.get("error").toString(), Toast.LENGTH_SHORT).show();
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
}
