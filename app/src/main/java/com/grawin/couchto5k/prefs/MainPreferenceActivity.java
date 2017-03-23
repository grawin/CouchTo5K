package com.grawin.couchto5k.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.grawin.couchto5k.R;

/**
 * The settings screen activity.
 */
public class MainPreferenceActivity extends PreferenceActivity {

    /**
     * Called upon activity creation.
     * @param savedInstanceState State data.
     */
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
        }
    }
}