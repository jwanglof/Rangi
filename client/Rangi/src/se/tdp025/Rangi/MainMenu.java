package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import se.tdp025.Rangi.camera.Camera;
import se.tdp025.Rangi.gallery.BrowseGallery;
import se.tdp025.Rangi.settings.Settings;

public class MainMenu extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
    }

    /*
     * Overrides the back-button so it acts like the home-button
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        Toast.makeText(this, "Get Premium button", Toast.LENGTH_LONG).show();
        /*Intent gallery = new Intent(this, C.class);
        startActivity(gallery); */
    }
    public void settings(View view) {
        //Toast.makeText(this, "Settings button", Toast.LENGTH_LONG).show();
        Intent settings = new Intent(this, Settings.class);
        startActivity(settings);
    }

    public void logout(View view) {
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