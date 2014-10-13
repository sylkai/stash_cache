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
 * Provided a stash, this will either read in (load, done when application is not in memory) or
 * write stash to disk (done frequently for backup) as a series of JSON objects/arrays.  Creation
 * of an object from its JSON representation or writing an object toJSON associated with
 * each class; this deals with the overall stash.
 *
 * Pattern data must be read in last in order to create links between Pattern and Fabric (if any)
 * and Pattern and Threads (if any).  Relies on having access to threadMap and fabricMap to build
 * connections.
 */
public class StashCacheJSONSerializer {

    private Context mContext;
    private String mFilename;

    public StashCacheJSONSerializer(Context c, String f) {
        // appContext and filename provided by StashData
        mContext = c;
        mFilename = f;
    }

    public void loadStash(StashData stashData) throws IOException, JSONException {
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

            stashData.setThreadData(fillThreadData(array.getJSONArray(0)));
            stashData.setFabricData(fillFabricData(array.getJSONArray(1)));
            stashData.setPatternData(fillPatternData(array.getJSONArray(2), stashData.getThreadData(), stashData.getFabricData()));
        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
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

        // create thread object from each JSON object in the array and add it to the map
        for (int i = 0; i < array.length(); i++) {
            StashThread thread = new StashThread(array.getJSONObject(i));
            threadMap.put(thread.getKey(), thread);
        }

        return threadMap;
    }

    private HashMap<String, StashFabric> fillFabricData(JSONArray array) throws JSONException {
        HashMap<String, StashFabric> fabricMap = new HashMap<String, StashFabric>();

        // create fabric object from each JSON object in the array and add it to the map
        for (int i = 0; i < array.length(); i++) {
            StashFabric fabric = new StashFabric(array.getJSONObject(i));
            fabricMap.put(fabric.getKey(), fabric);
        }

        return fabricMap;
    }

    private ArrayList<StashPattern> fillPatternData(JSONArray array, HashMap<String, StashThread> threadMap, HashMap<String, StashFabric> fabricMap) throws JSONException {
        ArrayList<StashPattern> patternList = new ArrayList<StashPattern>();

        // create pattern object from each JSON object in the array (using threadMap and fabricMap
        // to create linkages) and add it to the list
        for (int i = 0; i < array.length(); i++) {
            StashPattern pattern = new StashPattern(array.getJSONObject(i), threadMap, fabricMap);
            patternList.add(pattern);
        }

        return patternList;
    }

    private JSONArray writeThreadData(HashMap<String, StashThread> threadMap) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the threadMap values, convert each to JSON object and add to the array
        for (StashThread thread : threadMap.values()) {
            array.put(thread.toJSON());
        }

        return array;
    }

    private JSONArray writeFabricData(HashMap<String, StashFabric> fabricMap) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the fabricMap values, convert each to JSON object and add to the array
        for (StashFabric fabric: fabricMap.values()) {
            array.put(fabric.toJSON());
        }

        return array;
    }

    private JSONArray writePatternData(ArrayList<StashPattern> patternList) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the patternList, convert each to JSON object and add to the array
        for (StashPattern pattern : patternList) {
            array.put(pattern.toJSON());
        }

        return array;
    }

}
