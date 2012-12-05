package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Intent;
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
        Toast.makeText(this, "Settings button", Toast.LENGTH_LONG).show();
        Intent settings = new Intent(this, Settings.class);
        startActivity(settings);
    }
}