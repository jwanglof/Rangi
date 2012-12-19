package se.tdp025.Rangi;

import java.net.URL;
import java.util.ArrayList;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import se.tdp025.Rangi.json.JSON;

public class ColorInfo extends Activity {

    private static final String TAG = "Rangi_ColorInfo";
    private static final int SAVE_DIALOG = 10;
    private static final int DELETE_DIALOG = 11;

    private Context context;
    private int colorCode;
    private String hex;
    private StringBuilder sbHSV;
    private StringBuilder sbRGB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_info);
        context = this;
        //Get the chosen color
        Intent intent = getIntent();
        colorCode = intent.getIntExtra("color-code", 0);
        boolean showSave = intent.getBooleanExtra("show-save", false);

        if(showSave)
            showSave = !checkIfColorExist();
        else
            showHiddenContent();
        findViewById(R.id.save_color_button).setVisibility(showSave ? View.VISIBLE : View.GONE);

        Log.d(TAG, "Chosen color code:" + colorCode);

        //Set titleBar to chosen color
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.ColorInfo_titleBar);
        titleBar.setBackgroundColor(colorCode);

        //Color format
        //TextView colorView = (TextView) findViewById(R.id.color_data);
        //colorView.setText(Integer.toString(color));

        //Hex format
        hex = String.format("#%06X", (0xFFFFFF & colorCode));
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

        sbHSV = new StringBuilder();
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
        sbRGB = new StringBuilder();
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

    /***
     * Check if color exist in the current context
     */
    private boolean checkIfColorExist() {
        String name = JSON.checkIfColorExist(colorCode, context);
        if(name != null)
        {
            findViewById(R.id.delete_color_button).setVisibility(View.VISIBLE);
            TextView tx = (TextView)findViewById(R.id.color_name);
            tx.setText(name);
            tx.setVisibility(View.VISIBLE);
        }
        return name != null;
    }

    private void showHiddenContent(){
        checkIfColorExist();
    }

    /***
     * Save color, called when the Saved Button is clicked
     */
    public void saveColor(View view) {
        // Show save dialog
        showDialog(SAVE_DIALOG);
    }

    /***
     * Delete color, called when the Delete Button is clicked
     */
    public void deleteColor(View view)  {
        showDialog(DELETE_DIALOG);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = null;
        AlertDialog dialog = null;
        switch (id) {
            case SAVE_DIALOG:
                // Create our AlterDialog
                builder = new AlertDialog.Builder(this);
                builder.setMessage("Save color");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                input.setHint("Enter color name");
                builder.setView(input);
                builder.setCancelable(true);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show();
                        // Hide save button
                        findViewById(R.id.save_color_button).setVisibility(View.GONE);
                        // Save color
                        saveColor(input.getText().toString(), colorCode);
                        // Display delete button and color name
                        showHiddenContent();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close
                    }
                });
                dialog = builder.create();
                dialog.show();
                break;
            case DELETE_DIALOG:
                // Create our AlterDialog
                builder = new AlertDialog.Builder(this);
                builder.setMessage("Delete color");
                builder.setCancelable(true);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Delete", Toast.LENGTH_LONG).show();
                        // Delete color
                        deleteColor(colorCode);
                        // Exit activity
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close
                    }
                });
                dialog = builder.create();
                dialog.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    /***
     * Delete color, JSON
     */
    private void deleteColor(int color) {
        JSON.deleteFromJSON(color, context);
    }

    /***
     * Save color, JSON
     */
    public void saveColor(String name, int color) {
        if(name.length() < 1)
            name = "Undefined";

        JSONObject json = JSON.saveToJson(name, color, context);

        try {
            json.put("hex", hex);
            json.put("hsv", "hsv(" + sbHSV.toString().replaceAll("°", "&deg;") + ")");
            json.put("rgb", "rgb(" + sbRGB.toString() + ")");
            URL url = new URL(Data.SERVER_ADDRESS + "save");
            JSON.sendJsonToURL(url, json, context);
        } catch (Exception e) {
            Log.e(TAG, "saveColor: " + e);
            for(int i = 0; i < e.getStackTrace().length; i++)
                Log.e(TAG, e.getStackTrace()[i].toString());
        }
    }
}
