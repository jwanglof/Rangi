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
        // Make sure that the user can't go back to LoginScreen
        //finish();
        Intent login = new Intent(this, LoginScreen.class);
        startActivity(login);
    }

    public void register(View view) {
        // Make sure that the user can't go back to RegisterScreen
        //finish();
        Intent register = new Intent(this, RegisterScreen.class);
        startActivity(register);
    }

    /*
     * REMOVE WHEN IN PRODUCTION!
     */
    public void mainmenu(View view) {
        //finish();
        Intent mainmenu = new Intent(this, MainMenu.class);
        startActivity(mainmenu);
    }
}