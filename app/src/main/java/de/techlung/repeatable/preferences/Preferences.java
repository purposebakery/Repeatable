package de.techlung.repeatable.preferences;

import android.content.Context;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;

public class Preferences {

    public static void init(Context context) {

        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

}
