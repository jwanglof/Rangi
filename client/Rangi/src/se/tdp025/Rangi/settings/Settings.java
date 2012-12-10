package se.tdp025.Rangi.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import se.tdp025.Rangi.R;

public class Settings extends PreferenceActivity{
    private static final String OPT_NUMBER_OF_COLORS = "number_of_colors";
    private static final int OPT_NUMBER_OF_COLORS_DEF = 6;

    private static final String OPT_NUMBER_OF_SAVED = "number_of_saved";
    private static final int OPT_NUMBER_OF_SAVED_DEF = 5;

    private static final String OPT_SYNC_SERVER = "sync_server";
    private static final boolean OPT_SYNC_SERVER_DEF = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    public static int getNumberOfColors(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(OPT_NUMBER_OF_COLORS,OPT_NUMBER_OF_COLORS_DEF);
    }

    public static int getOptNumberOfSaved(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(OPT_NUMBER_OF_SAVED, OPT_NUMBER_OF_SAVED_DEF);
    }

    public static boolean  getSyncServer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SYNC_SERVER, OPT_SYNC_SERVER_DEF);
    }
}
