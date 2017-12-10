package sme.oelmann.sme_serial_monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import sme.oelmann.sme_serial_monitor.helpers.PortUtil;

public class SettingsActivity extends PreferenceActivity {

    public static final boolean performClick = false;
    public static final String kPORTS = "ports", kBAUDRATE = "comBaudrate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefFragment pf = new PrefFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, pf).commit();
    }

    public static class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private ListPreference lpPorts, lpBaudrate;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            PortUtil portUtil = new PortUtil(getActivity());

            lpPorts = (ListPreference) findPreference(kPORTS);
            lpBaudrate = (ListPreference) findPreference(kBAUDRATE);

            lpBaudrate.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(kBAUDRATE, "38400"));
            if (portUtil.getPorts().length > 1) {
                lpPorts.setEntries(portUtil.getPorts());
                lpPorts.setEntryValues(portUtil.getPorts());
            }
            lpPorts.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(kPORTS, getString(R.string.default_port)));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key){
                case kBAUDRATE:
                    lpBaudrate.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(kBAUDRATE, "38400"));
                    break;
                case kPORTS:
                    lpPorts.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(kPORTS, getString(R.string.default_port)));
                    break;
            }
        }
    }
}
