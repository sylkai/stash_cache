package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Splash screen to display on initial bootup while the stash is loading and sorted in the background.
 */

public class StashSplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new PreloadData().execute();
    }

    private class PreloadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            StashData.get(getApplicationContext());

            return null;
        }

        protected void onPostExecute(Void result) {
            Intent i = new Intent(StashSplashScreen.this, StashOverviewActivity.class);
            startActivity(i);

            finish();
        }
    }

}
