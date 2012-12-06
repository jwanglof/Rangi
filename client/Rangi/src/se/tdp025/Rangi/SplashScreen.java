package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        Handler handler = new Handler();

        // run a thread after 2 seconds to start the home screen
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
                Toast.makeText(SplashScreen.this, String.valueOf(user_login), Toast.LENGTH_SHORT).show();

                if (user_login) {
                    Intent intent = new Intent(SplashScreen.this, MainMenu.class);
                    SplashScreen.this.startActivity(intent);
                }
                else {
                    // start the home screen
                    Intent intent = new Intent(SplashScreen.this, StartScreen.class);
                    SplashScreen.this.startActivity(intent);
                }

            }

        }, 2000); // time in milliseconds (1 second = 1000 milliseconds) until the run() method will be called
    }
}