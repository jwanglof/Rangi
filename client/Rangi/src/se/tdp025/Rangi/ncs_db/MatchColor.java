package se.tdp025.Rangi.ncs_db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;

/**
 * User: Torbjörn Kvist(torkv393)
 * Date: 2012-12-19
 */
public class MatchColor {

    private static final String TAG = "Rangi_MatchColor";

    public static String matchColor(Context context, int color)   {

        DatabaseHelper myDbHelper = new DatabaseHelper(context);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color >> 0) & 0xFF;

        Color orgColor = new Color(red, green, blue);
        Color closestColor = null;
        double closest = -1;
        int range = 50;
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }

        try {
            //rawQuery("SELECT id, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});
            String sql = "SELECT * FROM ncs_table WHERE red BETWEEN ? AND ? AND (blue BETWEEN ? AND ? AND green BETWEEN ? AND ?)";
            String[] args = new String[]{
                    ""+(red - range), ""+(red + range),
                    ""+(green - range), ""+(green + range),
                    ""+(blue - range), ""+(blue + range),

            };
            myDbHelper.openDataBase();
            SQLiteDatabase db = myDbHelper.getReadableDatabase();
            Cursor c = db.rawQuery(sql, args);
            if (c.moveToFirst()) {

                do {
                    Color testColor = new Color(c.getString(0), c.getString(1),
                            Integer.parseInt(c.getString(2)), Integer.parseInt(c.getString(3)), Integer.parseInt(c.getString(4)));

                    double colorDistance = ColourDistance(orgColor, testColor);

                    if(closest != -1) {
                        if(closest > colorDistance) {
                            closestColor = testColor;
                            closest = colorDistance;
                        }
                    }
                    else {
                        closestColor = testColor;
                        closest = colorDistance;
                    }
                    // Adding contact to list
                } while (c.moveToNext());
            }
            if(closestColor != null) {
                Log.v(TAG, "ColourDistance: " + closest);
                Log.v(TAG, "Hex: " + closestColor.hex);
                return closestColor.ncs;
            }

            myDbHelper.close();

        }catch(SQLException sqle){
            Log.e(TAG, sqle.getMessage());

        }
        return "";
    }

    // http://www.compuphase.com/cmetric.htm
    private static double ColourDistance(Color c1, Color c2) {
        double rmean = ( c1.red + c2.red )/2;
        int r = c1.red - c2.red;
        int g = c1.green - c2.green;
        int b = c1.blue - c2.blue;
        double weightR = 2 + rmean/256;
        double weightG = 4.0;
        double weightB = 2 + (255-rmean)/256;
        return Math.sqrt(weightR*r*r + weightG*g*g + weightB*b*b);
    }
}
