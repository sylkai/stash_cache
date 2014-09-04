package com.geekeclectic.android.stashcache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashCacheJSONSerializer {

    private Context mContext;
    private String mFilename;

    public StashCacheJSONSerializer(Context c, String f) {
        mContext = c;
        mFilename = f;
    }

    public StashData loadStash() throws IOException, JSONException {
        StashData stash = StashData.get(mContext);
        BufferedReader reader = null;

        try {
            // open and read the file into a StringBuilder
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                // omit line breaks as irrelevant
                jsonString.append(line);
            }

            // parse the JSON using the Tokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            stash.setThreadData(fillThreadData(array.getJSONArray(0)));
            stash.setFabricData(fillFabricData(array.getJSONArray(1)));
            stash.setPatternData(fillPatternData(array.getJSONArray(2), stash.getThreadData(), stash.getFabricData()));
        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return stash;
    }

    public void saveStash(StashData stash) throws JSONException, IOException {
        // build the arrays in JSON
        JSONArray array = new JSONArray();
        array.put(writeThreadData(stash.getThreadData()));
        array.put(writeFabricData(stash.getFabricData()));
        array.put(writePatternData(stash.getPatternData()));

        // write the file to disk
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private HashMap<String, StashThread> fillThreadData(JSONArray array) throws JSONException {
        HashMap<String, StashThread> threadMap = new HashMap<String, StashThread>();

        for (int i = 0; i < array.length(); i++) {
            StashThread thread = new StashThread(array.getJSONObject(i));
            threadMap.put(thread.toString(), thread);
        }

        return threadMap;
    }

    private HashMap<String, StashFabric> fillFabricData(JSONArray array) throws JSONException {
        HashMap<String, StashFabric> fabricMap = new HashMap<String, StashFabric>();

        for (int i = 0; i < array.length(); i++) {
            StashFabric fabric = new StashFabric(array.getJSONObject(i));
            fabricMap.put(fabric.getId(), fabric);
        }

        return fabricMap;
    }

    private ArrayList<StashPattern> fillPatternData(JSONArray array, HashMap<String, StashThread> threadMap, HashMap<String, StashFabric> fabricMap) throws JSONException {
        ArrayList<StashPattern> patternList = new ArrayList<StashPattern>();

        for (int i = 0; i < array.length(); i++) {
            StashPattern pattern = new StashPattern(array.getJSONObject(i), threadMap, fabricMap);
            patternList.add(pattern);
        }

        return patternList;
    }

    private JSONArray writeThreadData(HashMap<String, StashThread> threadMap) throws JSONException {
        JSONArray array = new JSONArray();

        for (StashThread thread : threadMap.values()) {
            array.put(thread.toJSON());
        }

        return array;
    }

    private JSONArray writeFabricData(HashMap<String, StashFabric> fabricMap) throws JSONException {
        JSONArray array = new JSONArray();

        for (StashFabric fabric: fabricMap.values()) {
            array.put(fabric.toJSON());
        }

        return array;
    }

    private JSONArray writePatternData(ArrayList<StashPattern> patternList) throws JSONException {
        JSONArray array = new JSONArray();

        for (StashPattern pattern : patternList) {
            array.put(pattern.toJSON());
        }

        return array;
    }

}
