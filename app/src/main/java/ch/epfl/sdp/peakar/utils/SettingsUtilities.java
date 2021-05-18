package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public final class SettingsUtilities {

    /**
     * Get language code from language
     *
     * @param language language
     * @return language code (2 letters)
     */
    public static String getLanguageCode(String language){
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
    public static void setLocale(Context context, String lang) {
        Locale myLocale = new Locale(lang);
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
