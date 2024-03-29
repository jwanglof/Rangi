package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashScreen extends Activity {
    static private int splashDuration = 2000; // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        if (!Data.isNetworkConnected(SplashScreen.this)) {
            Toast.makeText(SplashScreen.this, "You don't have an internet connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
            splashDuration = 3500; // Default time for Toast.LENGT_LONG
        }

        Handler handler = new Handler();

        // run a thread after splashDuration seconds to start the home screen
        handler.postDelayed(new Runnable() {
            public void run() {
                // make sure we close the splash screen so the user won't come back when it presses back key
                finish();

                /*
                 * Shows if the user is signed in or not in the app
                 * Will be removed when in production!
                 */
                SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
                boolean user_login = userSettings.getBoolean("CONFIG_USER_LOGIN", false);
                //Toast.makeText(SplashScreen.this, String.valueOf(user_login), Toast.LENGTH_SHORT).show();


                if (user_login) {
                    Toast.makeText(SplashScreen.this, "Welcome back, " + userSettings.getString("CONFIG_USER_USERNAME", ""), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SplashScreen.this, MainMenu.class);
                    SplashScreen.this.startActivity(intent);
                }
                else {
                    // Start the StartScreen
                    Intent intent = new Intent(SplashScreen.this, StartScreen.class);
                    SplashScreen.this.startActivity(intent);
                }
            }
        }, splashDuration);
    }
}