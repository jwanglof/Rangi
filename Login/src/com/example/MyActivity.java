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
    private EditText email;
    private EditText password;

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

    public void login(View view) {
        email = (EditText) findViewById(R.id.email_address);
        password = (EditText) findViewById(R.id.password);

        if (!email.getText().toString().equalsIgnoreCase("")) {
            if (!password.getText().toString().equalsIgnoreCase("")) {
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
                    content = "password=" + URLEncoder.encode(password.getText().toString()) +
                            "&email=" + URLEncoder.encode(email.getText().toString());
                    printout.writeBytes(content);
                    printout.flush();
                    printout.close();

                    // Get response data
                    input = new DataInputStream(urlConn.getInputStream());

                } catch (MalformedURLException e) {
                    Toast.makeText(MyActivity.this, "Sumthi' we' wron'...", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MyActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void cancel(View view) {
        Toast.makeText(MyActivity.this, "This button is inactive.", Toast.LENGTH_SHORT).show();
    }
}
