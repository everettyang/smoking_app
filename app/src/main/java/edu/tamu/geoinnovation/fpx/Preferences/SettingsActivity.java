package edu.tamu.geoinnovation.fpx.Preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.tamu.geoinnovation.fpx.R;

/**
 * Created by atharmon on 3/15/2016.
 */
public class SettingsActivity extends Activity  {
    public static final String TAG = "SettingsActivity";
    private final String appPreferences = "preferences.fpx.tamu.edu";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        String sensors = extras.getString("sensors");
//        Log.d(TAG, sensors);
        String trimmed = sensors.trim();
        String[] tokens = trimmed.split(",");


        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

//        sharedPreferences = getSharedPreferences(appPreferences, MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean val = sharedPreferences.getBoolean("preference_gps_checkbox", false);
//        Log.d(TAG,  "" + val);
        editor = sharedPreferences.edit();
//        try {
//            String joe = sharedPreferences.getString("preference_gps_sampling_rate", "not there");
//            Log.d(TAG, joe);
//        } catch (Exception e) {
//            Log.d(TAG, e.getMessage());
//        }

    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            Log.d(TAG, key);
            // GPS, LIGHT, PROX, ACLR, SOUND, WIFI, PHONE
            switch (key) {

                case "preference_gps_checkbox":
                    CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(key);
//                    editor.putString(key, )
                    break;
                case "preference_gps_sampling_rate":
                    ListPreference listPreference = (ListPreference) findPreference(key);
                    listPreference.setSummary(listPreference.getEntry());
                    String value = listPreference.getValue();
                    Log.d(TAG, sharedPreferences.getString(key, "GPS NOT FOUND"));
                    break;

                case "preference_light_checkbox":
                    CheckBoxPreference checkBoxLight = (CheckBoxPreference) findPreference(key);
                    break;

                case "preference_light_sampling_rate":
                    ListPreference lightPreference = (ListPreference) findPreference(key);
                    lightPreference.setSummary(lightPreference.getEntry());
                    String lightValue = lightPreference.getValue();
                    Log.d(TAG, sharedPreferences.getString(key, "LIGHT NOT FOUND"));
                    break;

                case "preference_prox_checkbox":
                    CheckBoxPreference checkBoxProx = (CheckBoxPreference) findPreference(key);
                    break;

                case "preference_prox_sampling_rate":
                    ListPreference proxPreference = (ListPreference) findPreference(key);
                    proxPreference.setSummary(proxPreference.getEntry());
                    String proxValue = proxPreference.getValue();
                    Log.d(TAG, sharedPreferences.getString(key, "PROX NOT FOUND"));
                    break;

                case "preference_acrl_checkbox":
                    CheckBoxPreference checkBoxAcrl = (CheckBoxPreference) findPreference(key);
                    break;

                case "preference_acrl_sampling_rate":
                    ListPreference acrlPreference = (ListPreference) findPreference(key);
                    acrlPreference.setSummary(acrlPreference.getEntry());
                    String acrlValue = acrlPreference.getValue();
                    Log.d(TAG, sharedPreferences.getString(key, "ACRL NOT FOUND"));
                    break;
            }


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
