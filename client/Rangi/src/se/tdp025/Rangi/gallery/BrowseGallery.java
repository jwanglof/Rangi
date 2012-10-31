package se.tdp025.Rangi.gallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import se.tdp025.Rangi.analyze.CropImage.CropImage;

/**
 * User: Torbjörn Kvist(torkv393)
 * Date: 2012-10-06
 */
public class BrowseGallery extends Activity {

    private static final int SELECT_PICTURE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Intent intent = new Intent(this, CropImage.class);
                intent.putExtra("image-uri", selectedImageUri);
                intent.putExtra("scale", true);
                startActivity(intent);
                finish();
            }
        }
        else {
            finish();
        }
    }


}