package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.*;

public class MyActivity extends Activity {
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void register(View view) {
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);
        password_repeat = (EditText) findViewById(R.id.password_repeat);

        if (!username.getText().toString().equalsIgnoreCase("")) {
            if (!email.getText().toString().equalsIgnoreCase("")) {
                if (!password.getText().toString().equalsIgnoreCase("")) {
                    if (password.getText().toString().equals(password_repeat.getText().toString())) {
                        try {
                            url = new URL("http://192.168.56.102:5000/register");
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
                                // It has to be a better way to check for this since there is a JSON-string
                                // json-simple seems sweet. Need more research though.
                                if (str.contains("true"))
                                    // Go back to the home screen after 1-2 secs?
                                    Toast.makeText(MyActivity.this, "You are now registered!", Toast.LENGTH_SHORT).show();
                                else if (str.contains("false"))
                                    Toast.makeText(MyActivity.this, "Something went wrong with the registration. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                            input.close();
                        } catch (MalformedURLException e) {
                            Toast.makeText(MyActivity.this, "Sumthi' we' wron'...", Toast.LENGTH_SHORT).show();
                        } catch (IOException i) {
                            Toast.makeText(MyActivity.this, i.toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(MyActivity.this, "Your passwords does not match. Please re-write them", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MyActivity.this, "You have to choose a password", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MyActivity.this, "You have to insert an e-mail", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(MyActivity.this, "You have to insert a username", Toast.LENGTH_SHORT).show();
    }

    public void cancel(View view) {
        Toast.makeText(MyActivity.this, "This button is inactive.", Toast.LENGTH_SHORT).show();
    }
}
