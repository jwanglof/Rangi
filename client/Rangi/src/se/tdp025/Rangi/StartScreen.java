package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startscreen);
    }

    public void login(View view) {
        Intent login = new Intent(this, LoginScreen.class);
        startActivity(login);
    }

    public void register(View view) {
        Intent register = new Intent(this, RegisterScreen.class);
        startActivity(register);
    }

    public void mainmenu(View view) {
        Intent mainmenu = new Intent(this, MainMenu.class);
        startActivity(mainmenu);
    }
}