package se.tdp025.Rangi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Data {
    public static final String SERVER_ADDRESS = "http://hemma.klumpen.se:5000/";
    public static final String SHARED_COLORS = "RANGI_COLORS";

    /*
     * Used for user sessions
     * Will be used for log ins and registrations
     */
    public static final String PREFS_NAME = "RANGI";

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }
}