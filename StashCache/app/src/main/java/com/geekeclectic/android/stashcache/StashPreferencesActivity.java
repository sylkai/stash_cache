package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.BaseAdapter;

/**
 * Activity for the preferences screen (since attempting to use a preference fragment was not compatible
 * with the support fragment manager required for viewpager).
 */
public class StashPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_NEW_SKEIN_FOR_EACH = "new_skein_for_each";
    public static final String KEY_BORDER_SETTING = "pref_border_width";
    public static final String KEY_COUNT_SETTING = "pref_default_count";
    public static final String KEY_OVER_SETTING = "pref_default_over";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // trying to do this as put forward in the Google documents resulted in uneven refreshes
        // thanks StackOverflow: http://stackoverflow.com/questions/24373653/setsummary-is-not-refreshed-immediately
        BaseAdapter baseAdapter = (BaseAdapter) getPreferenceScreen().getRootAdapter();
        baseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
