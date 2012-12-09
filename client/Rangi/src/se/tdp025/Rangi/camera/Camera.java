package se.tdp025.Rangi.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import se.tdp025.Rangi.analyze.CropImage.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera extends Activity {

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private static final String TAG = "Rangi_Camera";


    /***
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void onDestroy() {
        super.onDestroy();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri image = fileUri;
                if(data != null)
                    image = (Uri)data.getData();
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        image, Toast.LENGTH_LONG).show();
                Intent analyze = new Intent(this, CropImage.class);
                analyze.putExtra("image-uri", image);
                analyze.putExtra("scale", true);
                startActivity(analyze);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Camera Canceled.");
                finish();
            } else {
                // Image capture failed, advise user
            }
        }
    }

    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /***
     *  Create a File for saving an image or video
     */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Rangi");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "RANGI_IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }
}
