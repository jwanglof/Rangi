package se.tdp025.Rangi.analyze;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.jabistudio.androidjhlabs.filter.BlockFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;
import se.tdp025.Rangi.R;
import se.tdp025.Rangi.analyze.CropImage.Util;
import se.tdp025.Rangi.settings.Settings;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class AnalyzeView extends Activity {

    private static final String TAG = "Rangi_analyze";
    private ColorAdapter colorAdapter;
    private int[] colors;
    private Context context;

    private ImageView imageView;
    private Bitmap image;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.test_view);

        //Uri imageUri = (Uri) this.getIntent().getExtras().get("cropped-image-uri");

        imageView = (ImageView) findViewById(R.id.testingImageView);
        image = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("image-byteArray"), 0, getIntent().getByteArrayExtra("image-byteArray").length);

        final ProgressDialog dialog = ProgressDialog.show(this, null, "Analyze pixels", true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = new BitmapDrawable(getResources(),image);
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                colors = AndroidUtils.drawableToIntArray(drawable);

                BlockFilter filter = new BlockFilter();

                int block = Settings.getNumberOfColors(context);

                int blockSizeW = width;
                int blockSizeH = height;

                // 2, 4, 6, 9
                if(height > width) {
                    if(block % 10 == 0) {
                        blockSizeW = width / (block / 5);
                        blockSizeH = height / (block / 2);
                    }
                    else if(block % 9 == 0) {
                        blockSizeW = width / (block / 3);
                        blockSizeH = height / (block / 3);
                    }
                    else if(block % 6 == 0) {
                        blockSizeW = width / (block / 3);
                        blockSizeH = height / (block / 2);
                    }
                    else if(block % 4 == 0 && block != 4) {
                        blockSizeW = width / (block / 4);
                        blockSizeH = height / (block / 2);
                    }
                    else if(block % 2 == 0) {
                        blockSizeW = width / (block / 2);
                        blockSizeH = height / (block / 2);
                    }
                    else {
                        if(height > width)
                            blockSizeW = width / block;
                        else
                            blockSizeH = height / block;
                    }
                }
                else {
                    if(block % 10 == 0) {
                        blockSizeW = width / (block / 2);
                        blockSizeH = height / (block / 5);
                    }
                    else if(block % 9 == 0) {
                        blockSizeW = width / (block / 3);
                        blockSizeH = height / (block / 3);
                    }
                    else if(block % 6 == 0) {
                        blockSizeW = width / (block / 2);
                        blockSizeH = height / (block / 3);
                    }
                    else if(block % 4 == 0 && block != 4) {
                        blockSizeW = width / (block / 2);
                        blockSizeH = height / (block / 4);
                    }
                    else if(block % 2 == 0) {
                        blockSizeW = width / (block / 2);
                        blockSizeH = height / (block / 2);
                    }
                    else {
                        if(height > width)
                            blockSizeW = width / block;
                        else
                            blockSizeH = height / block;
                    }
                }

                filter.setBlockSize(blockSizeH, blockSizeW);

                colors = filter.filter2(colors, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                if(image != null){
                    image.recycle();
                    image = null;
                }

                image = Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
                ArrayList<Integer> colorArray = new ArrayList<Integer>();
                int x = 0;
                for(int i = 0; i < width / blockSizeW; i++) {
                    int tempX = x + (blockSizeW / 2);
                    int y = 0;
                    for(int j = 0; j < height / blockSizeH; j++) {
                        int tempY = y + (blockSizeH / 2);
                        colorArray.add(image.getPixel(tempX, tempY));
                        y += blockSizeH;
                    }
                    x += blockSizeW;
                }


                Log.v(TAG, "Height: " + height);
                Log.v(TAG, "Width: " + width);
                Log.v(TAG, "BlockSizeH: " + blockSizeH);
                Log.v(TAG, "BlockSizeW: " + blockSizeW);

                Log.d(TAG, "AnalyzeView-Colors: " + colors.length);

                final ArrayList<Integer> colorArrayF = colorArray;
                AnalyzeView.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(image);
                        populateView(colorArrayF);
                        dialog.dismiss();
                    }
                });

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if(image != null){
            image.recycle();
            image = null;
        }
        super.onDestroy();
    }

    public void populateView( ArrayList<Integer> colors) {

        ListView listView = (ListView)findViewById(R.id.list);
        colorAdapter = new ColorAdapter(this, colors);
        listView.setAdapter(colorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Color of adapter:" + colorAdapter.getItem(i));
            }
        });

    }
}