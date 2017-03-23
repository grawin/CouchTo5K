package com.grawin.couchto5k.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.grawin.couchto5k.R;

/**
 * The settings screen activity.
 */
public class MainPreferenceActivity extends PreferenceActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    /**
     * The preferences fragment that loads the XML preferences file.
     */
    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            /*
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();


            // CHANGE 1: load saved values to set the summaries
            prefs.registerOnSharedPreferenceChangeListener(this);
            prefs.onSharedPreferenceChanged(prefs, "pref_notif_sound");
            onSharedPreferenceChanged(prefs, "pref_notif_sound");
            onSharedPreferenceChanged(prefs, "pref_notif_sound");

            // CHANGE 2: register shared prefs listener in onResume
            prefs.registerOnSharedPreferenceChangeListener(this);
            */
        }

        /*
        @Override
        public void onPause() {
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences(this);

        }
        */
    }

}