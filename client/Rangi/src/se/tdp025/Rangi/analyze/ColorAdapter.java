package se.tdp025.Rangi.analyze;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import se.tdp025.Rangi.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: Torbjörn Kvist(torkv393)
 * Date: 2012-11-02
 */
public class ColorAdapter extends BaseAdapter {

    private static final String TAG = "Rangi_ColorAdapter";
    private Activity activity;
    private ArrayList<Integer> data;
    private static LayoutInflater inflater = null;

    public ColorAdapter(Activity a, ArrayList<Integer> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.v(TAG, "getView");
        view = inflater.inflate(R.layout.list_row_color, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.list_image);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(100, 30, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(data.get(i));

        imageView.setImageBitmap(bmp);
        return view;
    }
}
