package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sylk on 1/22/2015.
 */
public class StashImporter {

    private static InputStream in;
    private static int DEFAULT = 1;
    private static HashMap<String, ArrayList<UUID>> threadMap;
    private static HashMap<String, ArrayList<UUID>> embellishmentMap;
    private boolean fileFormattedCorrectly;
    private boolean allNumbersFormatted;
    public static final String TAG = "StashImporter";

    public StashImporter(InputStream input) {
        // mFilename = "stash_input.txt";
        in = input;
        threadMap = new HashMap<String, ArrayList<UUID>>();
        embellishmentMap = new HashMap<String, ArrayList<UUID>>();
        fileFormattedCorrectly = true;
        allNumbersFormatted = true;
    }

    public int importStash(Context context) throws IOException {
        StashData stash = StashData.get(context);
        populateMaps(stash);

        BufferedReader reader = null;

        try {
            // open and read the file into a StringBuilder
            // InputStream in = am.open(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));

            Log.d(TAG, "Reader successfully opened.");

            String line = "";

            // read in thread data; end of thread block is marked by *** and will break loop
            while ((line = reader.readLine()) != null && !line.equals("***")) {
                // store source and type information
                String source = line;
                String type = reader.readLine();
                if (checkToContinue(type)) {
                    break;
                }

                // iterate through to the end of the source/type block and create a new thread for each id
                while ((line = reader.readLine()) != null) {
                    if (line.equals("***") || line.equals("---")) {
                        break;
                    }

                    String id = line;
                    String key;
                    if (id.contains(" ")) {
                        key = id.split("\\s")[0];
                    } else {
                        key = id;
                    }
                    incrementOrAddThread(source, type, id, key, stash);
                }

                if (line.equals("***")) {
                    break;
                }
            }

            // read in fabric data; end of the block is marked by *** and will break loop
            while ((line = reader.readLine()) != null && !line.equals("***")) {
                // skip a line if the current line marks the fabric delimiter ---
                if (line.equals("---")) {
                    line = reader.readLine();
                    if (checkToContinue(line)) {
                        break;
                    }
                }

                String source = line;
                String type = reader.readLine();
                if (checkToContinue(type)) {
                    break;
                }

                String color = reader.readLine();
                if (checkToContinue(color)) {
                    break;
                }

                line = reader.readLine();
                int count;
                Double height;
                Double width;

                if (checkToContinue(line)) {
                    break;
                } else {
                    try {
                        count = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        count = 0;
                        allNumbersFormatted = false;
                    }
                }

                line = reader.readLine();
                if (checkToContinue(line)) {
                    break;
                } else {
                    try {
                        height = Double.parseDouble(line);
                    } catch (NumberFormatException e) {
                        height = 0.0;
                        allNumbersFormatted = false;
                    }
                }

                line = reader.readLine();
                if (checkToContinue(line)) {
                    break;
                } else {
                    try {
                        width = Double.parseDouble(line);
                    } catch (NumberFormatException e) {
                        width = 0.0;
                        allNumbersFormatted = false;
                    }
                }

                createNewFabric(source, type, color, count, height, width, stash);
            }

            // read in embellishment data; end of the block is marked by *** and will break loop
            while ((line = reader.readLine()) != null && !line.equals("***")) {
                // store source and type information
                String source = line;
                String type = reader.readLine();
                if (checkToContinue(type)) {
                    break;
                }

                // iterate through to the end of the source/type block and create a new embellishment for each id
                while ((line = reader.readLine()) != null) {
                    if (line.equals("***") || line.equals("---")) {
                        break;
                    }

                    String id = line;
                    incrementOrAddEmbellishment(source, type, id, stash);
                }

                if (line.equals("***")) {
                    break;
                }
            }

            // read in pattern data; each pattern is separated by ---
            // if fabric provided in pattern (under pattern info, separated by *), create new fabric
            // check for existing threads/embellishments and create new if missing
            while ((line = reader.readLine()) != null) {
                // read in pattern information
                String title = line;
                String source = reader.readLine();
                if (checkToContinue(source)) {
                    break;
                }

                int width;
                int height;

                line = reader.readLine();
                if (checkToContinue(line)) {
                    break;
                } else {
                    try {
                        width = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        width = 0;
                        allNumbersFormatted = false;
                    }
                }

                line = reader.readLine();
                if (checkToContinue(line)) {
                    break;
                } else {
                    try {
                        height = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        height = 0;
                        allNumbersFormatted = false;
                    }
                }

                // create pattern
                StashPattern pattern = new StashPattern();
                pattern.setPatternName(title);
                pattern.setSource(source);
                pattern.setHeight(height);
                pattern.setWidth(width);
                stash.addPattern(pattern);

                // move forward one line to skip the first *
                reader.readLine();

                // if fabric is entered, create a new fabric
                if (!(line = reader.readLine()).equals("*")) {
                    source = line;
                    String type = reader.readLine();
                    if (checkToContinue(type)) {
                        break;
                    }

                    String color = reader.readLine();
                    if (checkToContinue(color)) {
                        break;
                    }

                    int count;
                    Double fabric_height;
                    Double fabric_width;

                    line = reader.readLine();
                    if (checkToContinue(line)) {
                        break;
                    } else {
                        try {
                            count = Integer.parseInt(line);
                        } catch (NumberFormatException e) {
                            count = 0;
                            allNumbersFormatted = false;
                        }
                    }

                    line = reader.readLine();
                    if (checkToContinue(line)) {
                        break;
                    } else {
                        try {
                            fabric_height = Double.parseDouble(line);
                        } catch (NumberFormatException e) {
                            fabric_height = 0.0;
                            allNumbersFormatted = false;
                        }
                    }

                    line = reader.readLine();
                    if (checkToContinue(line)) {
                        break;
                    } else {
                        try {
                            fabric_width = Double.parseDouble(line);
                        } catch (NumberFormatException e) {
                            fabric_width = 0.0;
                            allNumbersFormatted = false;
                        }
                    }

                    StashFabric fabric = createNewFabric(source, type, color, count, fabric_height, fabric_width, stash);

                    pattern.setFabric(fabric);
                    fabric.setUsedFor(pattern);
                    reader.readLine();
                }

                // if thread information is entered, check for thread in stash already and add it if not present
                while ((line = reader.readLine()) != null) {
                    source = line;
                    String type = reader.readLine();
                    if (checkToContinue(type)) {
                        break;
                    }

                    while ((line = reader.readLine()) != null) {
                        if (line.equals("*") || line.equals("-")) {
                            break;
                        }

                        String id = line;
                        String key;
                        if (id.contains(" ")) {
                            key = id.split("\\s")[0];
                        } else {
                            key = id;
                        }
                        StashThread thread = findOrAddThread(source, type, id, key, stash);

                        thread.usedInPattern(pattern);
                        pattern.addThread(thread);
                    }

                    if (line == null || line.equals("*")) {
                        break;
                    }
                }

                // if embellishment information is entered, check for embellishment in stash already and add it if not present
                while ((line = reader.readLine()) != null) {
                    source = line;
                    String type = reader.readLine();
                    if (checkToContinue(type)) {
                        break;
                    }

                    while ((line = reader.readLine()) != null && !line.equals("-")) {
                        if (line.equals("*") || line.equals("---")) {
                            break;
                        }

                        String id = line;
                        StashEmbellishment embellishment = findOrAddEmbellishment(source, type, id, stash);

                        embellishment.usedInPattern(pattern);
                        pattern.addEmbellishment(embellishment);
                    }

                    if (line == null || line.equals("---")) {
                        break;
                    }
                }

            }

        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (fileFormattedCorrectly && allNumbersFormatted) {
                return 0;
            } else if (fileFormattedCorrectly == false) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    private StashThread createNewThread(String source, String type, String id, String key, StashData stash, boolean inStash) {
        StashThread thread = new StashThread();

        thread.setSource(source);
        thread.setType(type);
        thread.setCode(id);
        if (inStash) {
            thread.setSkeinsOwned(DEFAULT);
        }

        stash.addThread(thread);

        if (threadMap.get(key) == null) {
            ArrayList<UUID> threadList = new ArrayList<UUID>();
            threadList.add(thread.getId());
            threadMap.put(key, threadList);
        } else {
            ArrayList<UUID> threadList = threadMap.get(key);
            threadList.add(thread.getId());
        }

        return thread;
    }

    private StashFabric createNewFabric(String source, String type, String color, int count, Double height, Double width, StashData stash) {
        StashFabric fabric = new StashFabric();

        fabric.setSource(source);
        fabric.setType(type);
        fabric.setColor(color);
        fabric.setCount(count);
        fabric.setHeight(height);
        fabric.setWidth(width);

        stash.addFabric(fabric);

        return fabric;
    }

    private StashEmbellishment createNewEmbellishment(String source, String type, String id, StashData stash, boolean inStash) {
        StashEmbellishment embellishment = new StashEmbellishment();

        embellishment.setSource(source);
        embellishment.setType(type);
        embellishment.setCode(id);
        if (inStash) {
            embellishment.setNumberOwned(DEFAULT);
        }

        stash.addEmbellishment(embellishment);

        if (embellishmentMap.get(id) == null) {
            ArrayList<UUID> embellishmentList = new ArrayList<UUID>();
            embellishmentList.add(embellishment.getId());
            embellishmentMap.put(id, embellishmentList);
        } else {
            ArrayList<UUID> embellishmentList = embellishmentMap.get(id);
            embellishmentList.add(embellishment.getId());
        }

        return embellishment;
    }

    private void incrementOrAddThread(String source, String type, String id, String key, StashData stash) {
        ArrayList<UUID> threadList = threadMap.get(key);
        StashThread thread;

        if (threadList != null) {
            for (UUID threadId : threadList) {
                thread = stash.getThread(threadId);
                if (isSameThread(thread, source, type, id, key)) {
                    if (!key.equals(id) && key.equals(thread.getCode())) {
                        thread.setCode(id);
                    }
                    thread.increaseOwnedQuantity();
                    return;
                }
            }

            createNewThread(source, type, id, key, stash, true);
        } else {
            createNewThread(source, type, id, key, stash, true);
        }
    }

    private StashThread findOrAddThread(String source, String type, String id, String key, StashData stash) {
        ArrayList<UUID> threadList = stash.getThreadList();
        StashThread thread;

        for (int i = 0; i < threadList.size(); i++) {
            thread = stash.getThread(threadList.get(i));
            if (isSameThread(thread, source, type, key, id)) {
                if (!key.equals(id) && key.equals(thread.getCode())) {
                    thread.setCode(id);
                }
                return thread;
            }
        }

        thread = createNewThread(source, type, id, key, stash, false);

        return thread;
    }

    private void incrementOrAddEmbellishment(String source, String type, String id, StashData stash) {
        ArrayList<UUID> embellishmentList = embellishmentMap.get(id);
        StashEmbellishment embellishment;

        if (embellishmentList != null) {
            for (UUID embellishmentId : embellishmentList) {
                embellishment = stash.getEmbellishment(embellishmentId);
                if (embellishment.getSource().equals(source) && embellishment.getType().equals(type) && embellishment.getCode().equals(id)) {
                    embellishment.increaseOwned();
                    return;
                }
            }

            createNewEmbellishment(source, type, id, stash, true);
        } else {
            createNewEmbellishment(source, type, id, stash, true);
        }
    }

    private StashEmbellishment findOrAddEmbellishment(String source, String type, String id, StashData stash) {
        ArrayList<UUID> embellishmentList = embellishmentMap.get(id);
        StashEmbellishment embellishment;

        if (embellishmentList != null) {
            for (UUID embellishmentId : embellishmentList) {
                embellishment = stash.getEmbellishment(embellishmentId);
                if (embellishment.getSource().equals(source) && embellishment.getType().equals(type) && embellishment.getCode().equals(id)) {
                    return embellishment;
                }
            }

            embellishment = createNewEmbellishment(source, type, id, stash, false);
        } else {
            embellishment = createNewEmbellishment(source, type, id, stash, false);
        }

        return embellishment;
    }

    private boolean checkToContinue(String toCheck) {
        if (toCheck != null && !toCheck.equals("***") && !toCheck.equals("---") && !toCheck.equals("*") && !toCheck.equals("-")) {
            return false;
        } else {
            fileFormattedCorrectly = false;
            return true;
        }
    }

    private void populateMaps(StashData stash) {
        ArrayList<UUID> threadList = stash.getThreadList();
        ArrayList<UUID> shortThreadList;

        for (UUID threadId : threadList) {
            StashThread thread = stash.getThread(threadId);
            shortThreadList = threadMap.get(thread.getCode());

            if (shortThreadList == null) {
                shortThreadList = new ArrayList<UUID>();
            }

            shortThreadList.add(threadId);

            threadMap.put(thread.getCode(), shortThreadList);
        }

        ArrayList<UUID> embellishmentList = stash.getEmbellishmentList();
        ArrayList<UUID> shortEmbellishmentList;

        for (UUID embellishmentId : embellishmentList) {
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
            shortEmbellishmentList = embellishmentMap.get(embellishment.getCode());

            if (shortEmbellishmentList == null) {
                shortEmbellishmentList = new ArrayList<UUID>();
            }

            shortEmbellishmentList.add(embellishmentId);

            embellishmentMap.put(embellishment.getCode(), shortEmbellishmentList);
        }
    }

    private boolean isSameThread(StashThread thread, String source, String type, String id, String key) {
        if (thread.getSource() != null && thread.getSource().equals(source)) {
            if (thread.getType() != null && thread.getType().equals(type)) {
                if (thread.getCode() != null && thread.getCode().equals(id)) {
                    return true;
                } else if (thread.getCode() != null && (thread.getCode().split("\\s")[0].equals(id) || key.equals(thread.getCode()))) {
                    return true;
                }
            }
        }

        return false;
    }

}
