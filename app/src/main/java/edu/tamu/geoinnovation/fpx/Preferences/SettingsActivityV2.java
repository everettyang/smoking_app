package edu.tamu.geoinnovation.fpx.Preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.CheckBox;

import edu.tamu.geoinnovation.fpx.R;

/**
 * Created by atharmon on 3/15/2016.
 */
public class SettingsActivityV2 extends Activity  {
    public static final String TAG = "SettingsActivity";
    private final String appPreferences = "preferences.fpx.tamu.edu";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static String[] tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        String sensors = extras.getString("sensors");
//        Log.d(TAG, sensors);
        String trimmed = sensors.trim();
        tokens = trimmed.split(",");
        
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        editor = sharedPreferences.edit();


    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.settings);

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
            if (SettingsActivityV2.tokens != null) {
                int size = SettingsActivityV2.tokens.length;
                String[] toks = SettingsActivityV2.tokens;
                for (int i = 0; i < size; i++) {
                    CheckBoxPreference box = new CheckBoxPreference(screen.getContext());
                    box.setTitle(toks[i]);
                    box.setKey(toks[i]);
                    box.setChecked(false);
                    screen.addPreference(box);
                }
            }

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "" + sharedPreferences.getBoolean(key, false));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }



}
