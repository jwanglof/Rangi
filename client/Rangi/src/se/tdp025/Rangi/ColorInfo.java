package se.tdp025.Rangi;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ColorInfo extends Activity {
	
	private static final String TAG = "Rangi_ColorInfo";
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.color_info);
        context = this;
        //Get the chosen color
        Intent intent = getIntent();
    	int color = intent.getIntExtra("color-code", 0);
        
        Log.d(TAG, "Chosen color code:" + color);
  
        //Set titleBar to chosen color
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.ColorInfo_titleBar);
        titleBar.setBackgroundColor(color);
        
    	//Color format
        //TextView colorView = (TextView) findViewById(R.id.color_data);
    	//colorView.setText(Integer.toString(color));
    	
    	//Hex format
    	String hex = String.format("#%06X", (0xFFFFFF & color));
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
    	Color.colorToHSV(color, hsv);
    	
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
}
