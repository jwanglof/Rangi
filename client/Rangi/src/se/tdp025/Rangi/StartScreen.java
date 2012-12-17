package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class StartScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startscreen);
    }

    public void login(View view) {
        if (Data.isNetworkConnected(this)) {
            Intent login = new Intent(this, LoginScreen.class);
            startActivity(login);
        }
        else
            Toast.makeText(this, "You don't have an internet connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
    }

    public void register(View view) {
        if (Data.isNetworkConnected(this)) {
            Intent register = new Intent(this, RegisterScreen.class);
            startActivity(register);
        }
        else
            Toast.makeText(this, "You don't have an internet connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
    }

    /*
     * REMOVE WHEN IN PRODUCTION!
     */
    public void mainmenu(View view) {
        Intent mainmenu = new Intent(this, MainMenu.class);
        startActivity(mainmenu);
    }
}