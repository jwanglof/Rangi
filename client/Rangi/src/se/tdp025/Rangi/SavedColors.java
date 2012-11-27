package se.tdp025.Rangi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import se.tdp025.Rangi.analyze.ColorAdapter;

import java.util.ArrayList;

public class SavedColors extends Activity {

    private ColorAdapter colorAdapter;
    private Context context;

    private static final String TAG = "Rangi_SavedColors";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_colors);

        context = this;

        ArrayList<Integer> colorArray = new ArrayList<Integer>();
        colorArray.add(-11507079);
        colorArray.add(-287386);
        colorArray.add(-2713499);
        colorArray.add(-9851);
        colorArray.add(-5079465);
        colorArray.add(-12102049);
        colorArray.add(-6970967);
        colorArray.add(-6315142);
        colorArray.add(-2894412);
        colorArray.add(-2236474);
        colorArray.add(-4210277);
        colorArray.add(-10587008);
        colorArray.add(-2035983);
        colorArray.add(-2892570);
        colorArray.add(-3223376);

        colorArray.add(-141947);
        colorArray.add(-5538728);
        colorArray.add(-1);         // White
        colorArray.add(-16777216);  // Black

        ListView listView = (ListView)findViewById(R.id.list);
        colorAdapter = new ColorAdapter(this, colorArray);
        listView.setAdapter(colorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Color of adapter:" + colorAdapter.getItem(i));
                Intent intent = new Intent(SavedColors.this, ColorInfo.class);
                intent.putExtra("color-code", Integer.parseInt(colorAdapter.getItem(i).toString()));
                SavedColors.this.startActivity(intent);           	
            }
        });
    }
}