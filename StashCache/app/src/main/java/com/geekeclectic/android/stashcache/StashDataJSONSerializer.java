package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Provided a stash, this will either read in (load, done when application is not in memory) or
 * write stash to disk (done frequently for backup) as a series of JSON objects/arrays.  Creation
 * of an object from its JSON representation or writing an object toJSON associated with
 * each class; this deals with the overall stash.
 *
 * Pattern data must be read in last in order to create links between Pattern and Fabric (if any)
 * and Pattern and Threads/Embellishments (if any).  Relies on having access to threadMap and
 * fabricMap to build connections.
 */
public class StashDataJSONSerializer {

    private Context mContext;
    private String mFilename;
    private String mBackupFile;

    public StashDataJSONSerializer(Context c, String f) {
        // appContext and filename provided by StashData
        mContext = c;
        mFilename = f;
        mBackupFile = "backup_" + mFilename;

    }

    public void loadStash(StashData stashData) throws IOException, JSONException, ParseException {
        BufferedReader reader = null;

        try {
            String openFile;
            InputStream in = null;

            try {
                in = mContext.openFileInput(mFilename);
                openFile = mFilename;
            } catch (FileNotFoundException e) {
                openFile = mBackupFile;
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            in = mContext.openFileInput(openFile);

            //read the file into a StringBuilder
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                // omit line breaks as irrelevant
                jsonString.append(line);
            }

            // parse the JSON using the Tokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            fillThreadData(array.getJSONArray(StashConstants.THREAD_JSON_ARRAY), stashData);
            fillFabricData(array.getJSONArray(StashConstants.FABRIC_JSON_ARRAY), stashData);
            fillEmbellishmentData(array.getJSONArray(StashConstants.EMBELLISHMENT_JSON_ARRAY), stashData);
            fillPatternData(array.getJSONArray(StashConstants.PATTERN_JSON_ARRAY), stashData);
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
        array.put(writeThreadData(stash.getThreadList(), stash));
        array.put(writeFabricData(stash.getFabricList(), stash));
        array.put(writeEmbellishmentData(stash.getEmbellishmentList(), stash));
        array.put(writePatternData(stash.getPatternData()));

        // write the file to disk
        Writer writer = null;
        try {
            String dir = mContext.getFilesDir().getAbsolutePath();
            // rename the previous save to backup
            File backup = new File(dir, mBackupFile);
            File old_save = new File(dir, mFilename);

            // rename does not overwrite if the file exists, so delete the previous backup
            if (backup.exists()) {
                backup.delete();
            }

            old_save.renameTo(backup);

            // write out the new file
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void fillThreadData(JSONArray array, StashData stashData) throws JSONException {
        HashMap<UUID, StashThread> threadMap = new HashMap<UUID, StashThread>();
        ArrayList<UUID> threadList = new ArrayList<UUID>();
        ArrayList<UUID> stashThreadList = new ArrayList<UUID>();

        // create thread object from each JSON object in the array and add it to the map
        for (int i = 0; i < array.length(); i++) {
            StashThread thread = new StashThread(array.getJSONObject(i), mContext);
            threadMap.put(thread.getId(), thread);
            threadList.add(thread.getId());

            if (thread.isOwned()) {
                stashThreadList.add(thread.getId());
            }
        }

        stashData.setThreadData(threadMap, threadList, stashThreadList);
    }

    private void fillFabricData(JSONArray array, StashData stashData) throws JSONException, ParseException {
        HashMap<UUID, StashFabric> fabricMap = new HashMap<UUID, StashFabric>();
        ArrayList<UUID> fabricList = new ArrayList<UUID>();
        ArrayList<UUID> stashFabricList = new ArrayList<UUID>();

        // create fabric object from each JSON object in the array and add it to the map
        for (int i = 0; i < array.length(); i++) {
            StashFabric fabric = new StashFabric(array.getJSONObject(i), mContext);
            fabricMap.put(fabric.getId(), fabric);
            fabricList.add(fabric.getId());
            if (!fabric.isFinished()) {
                stashFabricList.add(fabric.getId());
            }
        }

        stashData.setFabricData(fabricMap, fabricList, stashFabricList);
    }

    private void fillEmbellishmentData(JSONArray array, StashData stashData) throws JSONException {
        HashMap<UUID, StashEmbellishment> embellishmentMap = new HashMap<UUID, StashEmbellishment>();
        ArrayList<UUID> embellishmentList = new ArrayList<UUID>();
        ArrayList<UUID> embellishmentStashList = new ArrayList<UUID>();

        // create thread object from each JSON object in the array and add it to the map
        for (int i = 0; i < array.length(); i++) {
            StashEmbellishment embellishment = new StashEmbellishment(array.getJSONObject(i), mContext);
            embellishmentMap.put(embellishment.getId(), embellishment);
            embellishmentList.add(embellishment.getId());

            if (embellishment.isOwned()) {
                embellishmentStashList.add(embellishment.getId());
            }
        }

        stashData.setEmbellishmentData(embellishmentMap, embellishmentList, embellishmentStashList);
    }

    private void fillPatternData(JSONArray array, StashData stash) throws JSONException {
        ArrayList<StashPattern> patternList = new ArrayList<StashPattern>();
        ArrayList<StashPattern> stashPatternList = new ArrayList<StashPattern>();

        // create pattern object from each JSON object in the array (using threadMap and fabricMap
        // to create linkages) and add it to the list
        for (int i = 0; i < array.length(); i++) {
            StashPattern pattern = new StashPattern(array.getJSONObject(i), stash, mContext);
            patternList.add(pattern);

            if (pattern.inStash()) {
                stashPatternList.add(pattern);
            }
        }

        stash.setPatternData(patternList, stashPatternList);
    }

    private JSONArray writeThreadData(ArrayList<UUID> threadList, StashData stash) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the threadlist, convert each to JSON object and add to the array
        for (UUID threadId : threadList) {
            StashThread thread = stash.getThread(threadId);
            array.put(thread.toJSON());
        }

        return array;
    }

    private JSONArray writeFabricData(ArrayList<UUID> fabricList, StashData stash) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the fabricList, convert each to JSON object and add to the array
        for (UUID fabricId: fabricList) {
            StashFabric fabric = stash.getFabric(fabricId);
            array.put(fabric.toJSON());
        }

        return array;
    }

    private JSONArray writeEmbellishmentData(ArrayList<UUID> embellishmentList, StashData stash) throws JSONException {
        JSONArray array = new JSONArray();

        // iterate through the embellishmentList, convert each to JSON object and add to the array
        for (UUID embellishmentId : embellishmentList) {
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
            array.put(embellishment.toJSON());
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
