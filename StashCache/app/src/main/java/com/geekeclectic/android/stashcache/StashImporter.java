package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by sylk on 1/22/2015.
 */
public class StashImporter {

    private static String mFilename;
    public static final String TAG = "StashImporter";

    public StashImporter() {
        mFilename = "stash_input.txt";
    }

    public void importStash(Context context) throws IOException {
        StashData stash = StashData.get(context);

        AssetManager am = context.getAssets();
        BufferedReader reader = null;

        try {
            // open and read the file into a StringBuilder
            InputStream in = am.open(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));

            Log.d(TAG, "Reader successfully opened.");


        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
