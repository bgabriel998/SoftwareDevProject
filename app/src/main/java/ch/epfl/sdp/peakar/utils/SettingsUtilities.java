package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

import ch.epfl.sdp.peakar.R;

/**
 *  Utility class for the Settings Activity, contains utility methods for the settings like the language
 */
public final class SettingsUtilities {

    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * Updates the language depending on the preferences
     *
     * @param context context of the application
     * @param sharedPreferences shared preferences
     */
    public static void updateLanguage(Context context, SharedPreferences sharedPreferences){
        String language =  sharedPreferences.getString(context.getResources().getString(R.string.language_key), DEFAULT_LANGUAGE);
        SettingsUtilities.setLocale(context, SettingsUtilities.getLanguageCode(language));
    }

    /**
     * Get language code from language
     *
     * @param language language
     * @return language code (2 letters)
     */
    private static String getLanguageCode(String language){
        switch (language){
            case "german":
                return "de";
            case "italian":
                return "it";
            case "french":
                return "fr";
            case "swedish":
                return "sv";
            case "english":
            default:
                return "en";
        }
    }

    /**
     * Change application language by changing global configuration
     * This function reloads the activity to see the change directly
     * To avoid the activity-change animation, this function also
     * overrides the transition
     * @param lang language code
     */
    private static void setLocale(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        //update locale configuration with new language
        res.updateConfiguration(conf, dm);
        Activity activity = (Activity) context;
        activity.finish();
        //Override transition and restart activity
        activity.overridePendingTransition(0, 0);
        activity.startActivity(activity.getIntent());
        activity.overridePendingTransition(0, 0);
    }
}
