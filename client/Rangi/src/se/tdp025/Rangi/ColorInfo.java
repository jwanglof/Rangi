package se.tdp025.Rangi;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorInfo extends Activity {
	
	private static final String TAG = "Rangi_ColorInfo";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.color_info);
        
        //Get the chosen color
        Intent intent = getIntent();
    	int color = intent.getIntExtra("color-code", 0);
        
        Log.d(TAG, "Chosen color code:" + color);
  
        //Set titleBar to chosen color
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.ColorInfo_titleBar);
        titleBar.setBackgroundColor(color);
        
    	//Color format
        TextView colorView = (TextView) findViewById(R.id.color_data);
    	colorView.setText(Integer.toString(color));
    	
    	//Hex format
    	String hex = String.format("#%06X", (0xFFFFFF & color));
    	TextView hexView = (TextView) findViewById(R.id.hex_data);
    	hexView.setText(hex);
    	
    	//HSV format
    	float[] hsv = new float[3];
    	Color.colorToHSV(color, hsv);
    	
    	StringBuilder sbHSV = new StringBuilder();
    	sbHSV.append(Math.round(hsv[0]) + "\u00B0, ");
    	sbHSV.append(Math.round(hsv[1] * 100) + "\u0025, ");
    	sbHSV.append(Math.round(hsv[2] * 100) + "\u0025");
    	    	
    	TextView hsvView = (TextView) findViewById(R.id.hsv_data);
    	hsvView.setText(sbHSV.toString());
    	
    	//RGB format
    	int red = (color >> 16) & 0xFF;
    	int green = (color >> 8) & 0xFF;
    	int blue = (color >> 0) & 0xFF;
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
    	
    	TextView rgbView = (TextView) findViewById(R.id.rgb_data);
    	rgbView.setText(sbRGB.toString());
	}
}
