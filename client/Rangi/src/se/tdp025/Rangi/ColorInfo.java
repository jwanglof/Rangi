package se.tdp025.Rangi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import se.tdp025.Rangi.settings.Settings;

public class ColorInfo extends Activity {
	
	private static final String TAG = "Rangi_ColorInfo";
	private Context context;
    private int colorCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.color_info);
        context = this;
        //Get the chosen color
        Intent intent = getIntent();
        colorCode = intent.getIntExtra("color-code", 0);
        boolean showSave = intent.getBooleanExtra("show-save", false);
        findViewById(R.id.save_color_button).setVisibility(showSave ? View.VISIBLE : View.INVISIBLE);
        if(showSave)
            checkIfColorExist();

        
        Log.d(TAG, "Chosen color code:" + colorCode);
  
        //Set titleBar to chosen color
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.ColorInfo_titleBar);
        titleBar.setBackgroundColor(colorCode);
        
    	//Color format
        //TextView colorView = (TextView) findViewById(R.id.color_data);
    	//colorView.setText(Integer.toString(color));
    	
    	//Hex format
    	String hex = String.format("#%06X", (0xFFFFFF & colorCode));
    	final TextView hexView = (TextView) findViewById(R.id.hex_data);
    	hexView.setText(hex);
    	
    	hexView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG, "You LONG CLICKED the Hex value!");
		        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		        clipboard.setText(hexView.getText());
		        if(clipboard.hasText() == true) {
			        Toast.makeText(context, "Copied " + hexView.getText() + " to clipboard", Toast.LENGTH_SHORT).show();
		        }
		        return false;
			}    		    		
    	});

    	//HSV format
    	float[] hsv = new float[3];
    	Color.colorToHSV(colorCode, hsv);
    	
    	StringBuilder sbHSV = new StringBuilder();
    	sbHSV.append(Math.round(hsv[0]) + "\u00B0, ");
    	sbHSV.append(Math.round(hsv[1] * 100) + "\u0025, ");
    	sbHSV.append(Math.round(hsv[2] * 100) + "\u0025");
    	    	
    	final TextView hsvView = (TextView) findViewById(R.id.hsv_data);
    	hsvView.setText(sbHSV.toString());
    	
    	hsvView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG, "You LONG CLICKED the HSV value!");
		        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		        clipboard.setText(hsvView.getText());
		        if(clipboard.hasText() == true) {
			        Toast.makeText(context, "Copied " + hsvView.getText() + " to clipboard", Toast.LENGTH_SHORT).show();
		        }
		        return false;
			}    		    		
    	});
    	
    	//RGB format
    	int red = (colorCode >> 16) & 0xFF;
    	int green = (colorCode >> 8) & 0xFF;
    	int blue = (colorCode >> 0) & 0xFF;
    	ArrayList<Integer> rgb = new ArrayList<Integer>();
    	rgb.add(red);
    	rgb.add(green);
    	rgb.add(blue);
    	
    	//Convert RGB to String
    	StringBuilder sbRGB = new StringBuilder();
    	int size = rgb.size();
    	boolean separatorRGB = false;
    	for(int i = 0; i < size; i++) {
    		if(separatorRGB){
    			sbRGB.append(", ");
    		}
    		separatorRGB = true;
    		sbRGB.append(rgb.get(i));
    	}
    	
    	final TextView rgbView = (TextView) findViewById(R.id.rgb_data);
    	rgbView.setText(sbRGB.toString());
    	
    	rgbView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG, "You LONG CLICKED the RGB value!");
		        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		        clipboard.setText(rgbView.getText());
		        if(clipboard.hasText() == true) {
			        Toast.makeText(context, "Copied " + rgbView.getText() + " to clipboard", Toast.LENGTH_SHORT).show();
		        }
		        return false;
			}    		    		
    	});
	}

    private void checkIfColorExist() {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void saveColor(View view) {
        // Save
        Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show();
        findViewById(R.id.save_color_button).setVisibility(View.INVISIBLE);
        saveColor(Data.SHARED_COLORS, colorCode);
    }


    public boolean saveColor(String arrayName, int color) {
        SharedPreferences prefs = context.getSharedPreferences("RANGI", 0);
        SharedPreferences.Editor editor = prefs.edit();
        Integer[] colorArray = loadColors(arrayName, context);
        ArrayList<Integer> colorList =  new ArrayList<Integer>(Arrays.asList(colorArray));
        colorList.add(color);

        colorArray = colorList.toArray(new Integer[colorList.size()]);

        editor.putInt(arrayName +"_size", colorArray.length);
        for(int i=0;i<colorArray.length;i++)
            editor.putInt(arrayName + "_" + i, colorArray[i]);
        return editor.commit();
    }



    public Integer[] loadColors(String name, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("RANGI", 0);
        int size = prefs.getInt(name + "_size", 0);
        Integer array[] = new Integer[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getInt(name + "_" + i, 0);
        return array;
    }
}
