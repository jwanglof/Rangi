package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import se.tdp025.Rangi.camera.Camera;
import se.tdp025.Rangi.gallery.BrowseGallery;
import se.tdp025.Rangi.SavedColors;
import se.tdp025.Rangi.settings.Settings;

public class MainMenu extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        /*
         * Pretty sure this isn't necessary
         * This should be done with flags BEFORE the user get access to MainMenu!

        SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
        boolean user_login = userSettings.getBoolean("CONFIG_USER_LOGIN", false);
        if (!user_login)
            finish();
         */
    }

    /*
     * Override the default Back-button event
     * This disabled the Back-button completely when in MainMenu
     * According to the community it's not recommended to it like this but the flag doesn't seem to work,
     * or we're trying to implement it the wrong way...
     */
    @Override
    public void onBackPressed() {
        return;
    }

    public void camera(View view) {
        //Toast.makeText(this, "Camera button", Toast.LENGTH_SHORT).show();
        Intent camera = new Intent(this, Camera.class);
        startActivity(camera);
    }
    public void gallery(View view) {
        //Toast.makeText(this, "Gallery button", Toast.LENGTH_SHORT).show();
        Intent gallery = new Intent(this, BrowseGallery.class);
        startActivity(gallery);
    }
    public void saved_colors(View view) {
        //Toast.makeText(this, "Saved Colors button", Toast.LENGTH_LONG).show();
        Intent saved_colors = new Intent(this, SavedColors.class);
        startActivity(saved_colors);
    }
    public void in_app(View view) {
        Toast.makeText(this, "In_App button", Toast.LENGTH_LONG).show();
        /*Intent gallery = new Intent(this, C.class);
        startActivity(gallery); */
    }
    public void settings(View view) {
        //Toast.makeText(this, "Settings button", Toast.LENGTH_LONG).show();
        Intent settings = new Intent(this, Settings.class);
        startActivity(settings);
    }

    public void logout(View view) {
        // Make sure that the user can't access MainMenu after he signed off
        finish();

        Toast.makeText(this, "You have signed off. Plz come back to us!", Toast.LENGTH_LONG).show();
        SharedPreferences userSettings = getSharedPreferences(Data.PREFS_NAME, 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putBoolean("CONFIG_USER_LOGIN", false);
        editor.putString("CONFIG_USER_USERNAME", "");
        editor.commit();

        // Go to start screen
        Intent ss = new Intent(this, StartScreen.class);
        startActivity(ss);
    }
}