package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

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
     */
    public static void updateLanguage(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    /**
     * Gets the locale currently set
     * @param context context of the application
     * @return Locale that is set in the settings
     */
    private static Locale getSetLocale(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String language =  sharedPreferences.getString(context.getResources().getString(R.string.language_key), DEFAULT_LANGUAGE);
        return new Locale(getLanguageCode(language));
    }

    /**
     * Checks for the currently used locale and the locale set in the settings and refreshes the
     * activity if the current locale is not equal to the locale in the settings
     *
     * @param context context of application
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void checkForLanguage(Context context){
        //Only check the first two letters to only check for the language
        String localeSettings = getSetLocale(context).stripExtensions().toString().substring(0, 2);
        String currentLocale = context.getResources().getConfiguration().getLocales().get(0).toString().substring(0, 2);
        if(!localeSettings.equals(currentLocale)){
            updateLanguage(context);
        }
    }
}