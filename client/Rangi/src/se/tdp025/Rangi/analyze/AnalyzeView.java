package se.tdp025.Rangi.analyze;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import se.tdp025.Rangi.R;
import se.tdp025.Rangi.analyze.CropImage.Util;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class AnalyzeView extends Activity {

    private static final String TAG = "Rangi_analyze";
    private HashMap<Integer, Integer> pixelHashCount;
    private ColorAdapter colorAdapter;
    private final Handler handler = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pixelHashCount = new HashMap<Integer, Integer>();

        setContentView(R.layout.test_view);

        Uri imageUri = (Uri) this.getIntent().getExtras().get("cropped-image-uri");

        ImageView imageView = (ImageView) findViewById(R.id.testingImageView);
        final Bitmap croppedBitmap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("image-byteArray"), 0, getIntent().getByteArrayExtra("image-byteArray").length);
        imageView.setImageBitmap(croppedBitmap);


        final ProgressDialog dialog = ProgressDialog.show(this, null, "Analyze pixels", true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;

                Log.d(TAG, "Bitmap height: " + croppedBitmap.getHeight());
                Log.d(TAG, "Bitmap width: " + croppedBitmap.getWidth());

                for(int i = 0; i < croppedBitmap.getHeight(); i++)
                    for(int j = 0; j < croppedBitmap.getWidth(); j++) {

                        if(croppedBitmap.getWidth() < j)
                            Log.d(TAG, "j: " + j);
                        int pixel_index = croppedBitmap.getPixel(j, i);
                        if(pixel_index != 0) {
                            if(pixelHashCount.containsKey(pixel_index)) {
                                int pixel_i = pixelHashCount.get(pixel_index);
                                pixel_i++;
                                pixelHashCount.put(pixel_index, pixel_i);
                            } else{
                                pixelHashCount.put(pixel_index, 1);
                            }
                            count++;
                        }
                    }


                Comparator<int[]> comparator = new integerComparator();
                PriorityQueue<int[]> queue = new PriorityQueue<int[]>(10, comparator);


                for (int key : pixelHashCount.keySet()) {
                    int[] values = new int[] {key, pixelHashCount.get(key)};


                    if(queue.size() < 10)
                        queue.add(values);
                    else if(values[1] > queue.peek()[1]) {
                        queue.poll();
                        queue.add(values);

                    }
                }


                Log.d(TAG, "Count: "  + count);
                Log.d(TAG, "Array count: " + pixelHashCount.size());
                Log.d(TAG, "Queue size: " + queue.size());




                ArrayList<Integer> colorArray = new ArrayList<Integer>();
                BigInteger avgColor = BigInteger.valueOf(0);
                int avgColorCount = 0;
                Iterator it = queue.iterator();
                while(it.hasNext())
                {
                    int[] iValue= (int[]) it.next();
                    BigInteger multi = BigInteger.valueOf(iValue[0]).multiply(BigInteger.valueOf(iValue[1]));
                    avgColor = avgColor.add(multi);
                    avgColorCount += iValue[1];
                    Log.d(TAG, "Color pixel :" + iValue[0] + " | Count: " + iValue[1]);
                    colorArray.add(iValue[0]);
                }

                final int avgColorF = avgColor.divide(BigInteger.valueOf(avgColorCount)).intValue();
                Log.d(TAG, "Average Color: " + avgColor.toString());
                dialog.dismiss();
                final ArrayList<Integer> colorArrayF = colorArray;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        populateView(avgColorF, colorArrayF);
                    }
                });
            }

            class integerComparator implements Comparator<int[]> {
                @Override
                public int compare(int[] x, int[] y) {
                    if (x[1] < y[1])
                    {
                        return -1;
                    }
                    if (x[1] > y[1])
                    {
                        return 1;
                    }
                    return 0;
                }
            }
        }).start();
    }


    public void populateView(final int avgColor, ArrayList<Integer> colors) {

        ImageView imageView = (ImageView)findViewById(R.id.avg_image);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(100, 30, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(avgColor);
        imageView.setImageBitmap(bmp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Avg color: " + avgColor);
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


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