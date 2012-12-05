package se.tdp025.Rangi.analyze;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.jabistudio.androidjhlabs.filter.BlockFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;
import se.tdp025.Rangi.ColorInfo;
import se.tdp025.Rangi.R;
import se.tdp025.Rangi.settings.Settings;

import java.util.*;

public class AnalyzeView extends Activity {

    private static final String TAG = "Rangi_analyze";
    private ColorAdapter colorAdapter;
    private int[] colors;
    private Context context;
    private Bitmap image;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.result_view);

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

                if(block % 2 == 0) {
                    blockSizeW = width / 2;
                    blockSizeH = height / (block / 2);
                }
                else  {
                    blockSizeW = width / block;
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

                final ArrayList<Integer> colorArrayF = colorArray;
                AnalyzeView.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                Intent intent = new Intent(context, ColorInfo.class);
                intent.putExtra("color-code", Integer.parseInt(colorAdapter.getItem(i).toString()));
                startActivity(intent);
            }
        });

    }
}