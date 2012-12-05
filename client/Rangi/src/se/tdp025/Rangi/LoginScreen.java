package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
            url = new URL(Data.serverConnectionIP + "login");
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

            try {
                inputJson = new org.json.JSONObject(result);

                if (inputJson.getBoolean("success")) {
                    Toast.makeText(LoginScreen.this, "Login successfull.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginScreen.this, "Login not successfull!", Toast.LENGTH_SHORT).show();
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