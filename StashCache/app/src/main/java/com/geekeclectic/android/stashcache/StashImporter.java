package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Class to import data from a text file into the stash.  Formatted in the pattern / fabric not assigned /
 * embellishments / pattern (pattern info / fabric if assigned / thread / embellishments). InputStream
 * is provided by the caller - may come from file on the device, Dropbox, etc.
 */
public class StashImporter {

    public static final String TAG = "StashImporter";

    private boolean fileFormattedCorrectly;
    private boolean allNumbersFormatted;
    private static HashMap<String, ArrayList<UUID>> threadMap;
    private static HashMap<String, ArrayList<UUID>> embellishmentMap;
    private static InputStream in;
    private static String UTF8 = "utf8";
    private static String stashBlockDivider = "***";
    private static String stashTypeDivider = "---";
    private static String patternBlockDivider = "*";
    private static String patternTypeDivider = "-";
    private static String lastRead;
    private Context mContext;

    public StashImporter(InputStream input) {
        in = input;
        threadMap = new HashMap<String, ArrayList<UUID>>();
        embellishmentMap = new HashMap<String, ArrayList<UUID>>();
        fileFormattedCorrectly = true;
        allNumbersFormatted = true;
    }

    public int importStash(Context context) throws IOException {
        mContext = context;
        StashData stash = StashData.get(mContext);
        populateMaps(stash);

        BufferedReader reader = null;

        try {
            // open and read the file into a StringBuilder
            // InputStream in = am.open(mFilename);
            reader = new BufferedReader(new InputStreamReader(in, UTF8));

            Log.d(TAG, "Reader successfully opened.");

            lastRead = "";

            readStashThread(reader, stash);
            readStashFabric(reader, stash);
            readStashEmbellishment(reader, stash);

            readPatterns(reader, stash);

            readThread(reader, stash);
            readEmbellishment(reader, stash);

        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (fileFormattedCorrectly && allNumbersFormatted) {
            return StashConstants.FILE_IMPORT_ALL_CLEAR;
        } else if (!fileFormattedCorrectly) {
            return StashConstants.FILE_INCORRECT_FORMAT;
        } else {
            return StashConstants.NUMBER_FORMAT_INCORRECT;
        }
    }

    private StashPattern readPatternInfo(BufferedReader reader, StashData stash) throws IOException {
        // read in pattern information
        String title = lastRead;
        String source = reader.readLine();
        if (checkToContinue(source)) {
            lastRead = source;
            return null;
        }

        int width;
        int height;

        lastRead = reader.readLine();
        if (checkToContinue(lastRead)) {
            return null;
        } else {
            try {
                width = Integer.parseInt(lastRead);
            } catch (NumberFormatException e) {
                width = StashConstants.INT_ZERO;
                allNumbersFormatted = false;
            }
        }

        lastRead = reader.readLine();
        if (checkToContinue(lastRead)) {
            return null;
        } else {
            try {
                height = Integer.parseInt(lastRead);
            } catch (NumberFormatException e) {
                height = StashConstants.INT_ZERO;
                allNumbersFormatted = false;
            }
        }

        // create pattern
        StashPattern pattern = new StashPattern(mContext);
        pattern.setPatternName(title);
        pattern.setSource(source);
        pattern.setHeight(height);
        pattern.setWidth(width);
        stash.addPattern(pattern);

        // if the next line is not a *, the pattern is marked as kitted
        lastRead = reader.readLine();
        if (!lastRead.equals(patternBlockDivider)) {
            pattern.setKitted(true);

            // move forward one line to skip the first *
            reader.readLine();
        }

        return pattern;
    }

    private void readPatterns(BufferedReader reader, StashData stash) throws IOException {
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {

            StashPattern pattern = readPatternInfo(reader, stash);

            if (!readPatternFabric(reader, stash, pattern)) {
                break;
            }

            readPatternThread(reader, stash, pattern);
            readPatternEmbellishment(reader, stash, pattern);

            if (lastRead.equals(stashBlockDivider)) {
                break;
            }

            readFinishes(reader, stash, pattern);

            if (lastRead.equals(stashBlockDivider)) {
                break;
            }

        }
    }

    private void readStashFabric(BufferedReader reader, StashData stash) throws IOException {

        // read in fabric data; end of the block is marked by *** and will break loop
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {
            // skip a line if the current line marks the fabric delimiter ---
            if (lastRead.equals(stashTypeDivider)) {
                lastRead = reader.readLine();
                if (checkToContinue(lastRead)) {
                    break;
                }
            }

            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            String color = reader.readLine();
            if (checkToContinue(color)) {
                lastRead = color;
                break;
            }

            lastRead = reader.readLine();
            int count;
            Double height;
            Double width;

            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    count = Integer.parseInt(lastRead);
                } catch (NumberFormatException e) {
                    count = StashConstants.INT_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    height = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    height = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    width = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    width = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            createNewFabric(source, type, color, count, height, width, stash);
        }
    }

    private boolean readPatternFabric(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException {
        // if fabric is entered, create a new fabric
        if (!(lastRead = reader.readLine()).equals(patternBlockDivider)) {
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                return false;
            }

            String color = reader.readLine();
            if (checkToContinue(color)) {
                lastRead = color;
                return false;
            }

            int count;
            Double fabric_height;
            Double fabric_width;

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                return false;
            } else {
                try {
                    count = Integer.parseInt(lastRead);
                } catch (NumberFormatException e) {
                    count = StashConstants.INT_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                return false;
            } else {
                try {
                    fabric_height = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    fabric_height = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                return false;
            } else {
                try {
                    fabric_width = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    fabric_width = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            StashFabric fabric = createNewFabric(source, type, color, count, fabric_height, fabric_width, stash);

            pattern.setFabric(fabric);
            fabric.setUsedFor(pattern);

            // if the next line is not a *, the fabric is marked as in use
            lastRead = reader.readLine();
            if (!lastRead.equals(patternBlockDivider)) {
                fabric.setUse(true);

                // move forward one line to skip the first *
                reader.readLine();
            }
        }

        return true;
    }

    private void readFinishes(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException {

        while (!lastRead.equals(stashTypeDivider) && (lastRead = reader.readLine()) != null) {
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            String color = reader.readLine();
            if (checkToContinue(color)) {
                lastRead = color;
                break;
            }

            int count;
            Double fabric_height;
            Double fabric_width;

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    count = Integer.parseInt(lastRead);
                } catch (NumberFormatException e) {
                    count = StashConstants.INT_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    fabric_height = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    fabric_height = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            lastRead = reader.readLine();
            if (checkToContinue(lastRead)) {
                break;
            } else {
                try {
                    fabric_width = Double.parseDouble(lastRead);
                } catch (NumberFormatException e) {
                    fabric_width = StashConstants.DOUBLE_ZERO;
                    allNumbersFormatted = false;
                }
            }

            StashFabric fabric = createNewFabric(source, type, color, count, fabric_height, fabric_width, stash);

            fabric.setUsedFor(pattern);
            fabric.setComplete(true);

            lastRead = reader.readLine();
            if (lastRead.equals("***") || lastRead.equals("---")) {
                break;
            }
        }
    }

    private void readStashThread(BufferedReader reader, StashData stash) throws IOException {

        // read in thread data; end of thread block is marked by *** and will break loop
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {
            // store source and type information
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            // iterate through to the end of the source/type block and create a new thread for each id
            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(stashBlockDivider) || lastRead.equals(stashTypeDivider)) {
                    break;
                }

                String key;
                if (lastRead.contains(" ")) {
                    key = lastRead.split("\\s")[0];
                } else {
                    key = lastRead;
                }
                incrementOrAddThread(source, type, lastRead, key, stash);
            }

            // if we broke out of the previous loop due to end of the block, break out of the larger
            // loop before reading the next line
            if (lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private void readThread(BufferedReader reader, StashData stash) throws IOException {

        // read in thread data; end of thread block is marked by *** and will break loop
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {
            // store source and type information
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            // iterate through to the end of the source/type block and create a new thread for each id
            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(stashBlockDivider) || lastRead.equals(stashTypeDivider)) {
                    break;
                }

                String key;
                if (lastRead.contains(" ")) {
                    key = lastRead.split("\\s")[0];
                } else {
                    key = lastRead;
                }
                findOrAddThread(source, type, lastRead, key, stash);
            }

            if (lastRead == null || lastRead.equals(stashBlockDivider)) {
                break;
            }

        }
    }

    private void readPatternThread(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException {

        // if thread information is entered, check for thread in stash already and add it if not present
        while ((lastRead = reader.readLine()) != null) {
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(patternBlockDivider) || lastRead.equals(patternTypeDivider)) {
                    break;
                }

                String key;
                if (lastRead.contains(" ")) {
                    key = lastRead.split("\\s")[0];
                } else {
                    key = lastRead;
                }
                StashThread thread = findOrAddThread(source, type, lastRead, key, stash);

                thread.usedInPattern(pattern);
                pattern.increaseQuantity(thread);
            }

            if (lastRead == null || lastRead.equals(patternBlockDivider)) {
                break;
            }
        }
    }

    private void readStashEmbellishment(BufferedReader reader, StashData stash) throws IOException{

        // read in embellishment data; end of the block is marked by *** and will break loop
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {
            // store source and type information
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            // iterate through to the end of the source/type block and create a new embellishment for each id
            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(stashBlockDivider) || lastRead.equals(stashTypeDivider)) {
                    break;
                }

                incrementOrAddEmbellishment(source, type, lastRead, stash);
            }

            if (lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private void readEmbellishment(BufferedReader reader, StashData stash) throws IOException {

        // read in embellishment data; end of the block is marked by *** and will break loop
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {
            // store source and type information
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            // iterate through to the end of the source/type block and create a new embellishment for each id
            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(stashBlockDivider) || lastRead.equals(stashTypeDivider)) {
                    break;
                }

                findOrAddEmbellishment(source, type, lastRead, stash);
            }

            if (lastRead == null || lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private void readPatternEmbellishment(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException {

        // if embellishment information is entered, check for embellishment in stash already and add it if not present
        while ((lastRead = reader.readLine()) != null) {
            String source = lastRead;
            String type = reader.readLine();
            if (checkToContinue(type)) {
                lastRead = type;
                break;
            }

            while ((lastRead = reader.readLine()) != null) {
                if (lastRead.equals(patternBlockDivider) || lastRead.equals(patternTypeDivider) || lastRead.equals(stashTypeDivider) || lastRead.equals(stashBlockDivider)) {
                    break;
                }

                StashEmbellishment embellishment = findOrAddEmbellishment(source, type, lastRead, stash);

                embellishment.usedInPattern(pattern);
                pattern.increaseQuantity(embellishment);
            }

            if (lastRead == null || lastRead.equals(patternBlockDivider) || lastRead.equals(stashTypeDivider) || lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private StashThread createNewThread(String source, String type, String id, String key, StashData stash, boolean inStash) {
        StashThread thread = new StashThread(mContext);

        thread.setSource(source);
        thread.setType(type);
        thread.setCode(id);
        if (inStash) {
            thread.increaseOwnedQuantity();
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
        StashFabric fabric = new StashFabric(mContext);

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
        StashEmbellishment embellishment = new StashEmbellishment(mContext);

        embellishment.setSource(source);
        embellishment.setType(type);
        embellishment.setCode(id);
        if (inStash) {
            embellishment.increaseOwned();
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
                    if (!key.equals(id) && key.equalsIgnoreCase(thread.getCode())) {
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
        ArrayList<UUID> threadList = threadMap.get(key);
        StashThread thread;

        if (threadList != null) {
            for (UUID threadId : threadList) {
                thread = stash.getThread(threadId);
                if (isSameThread(thread, source, type, key, id)) {
                    if (!key.equals(id) && key.equalsIgnoreCase(thread.getCode())) {
                        thread.setCode(id);
                    }
                    return thread;
                }
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
                if (embellishment.getSource().equalsIgnoreCase(source) && embellishment.getType().equalsIgnoreCase(type) && embellishment.getCode().equalsIgnoreCase(id)) {
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
                if (embellishment.getSource().equalsIgnoreCase(source) && embellishment.getType().equalsIgnoreCase(type) && embellishment.getCode().equalsIgnoreCase(id)) {
                    return embellishment;
                }
            }

            embellishment = createNewEmbellishment(source, type, id, stash, false);
        } else {
            embellishment = createNewEmbellishment(source, type, id, stash, false);
        }

        return embellishment;
    }

    // if the string is not null/a divider, this returns false (preventing a break)
    // if it does return true, it marks the file format check as false so that the user can be alerted
    // that there was a formatting issue
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

            // check to see if there is additional info in the code, if so, shorten it so only the first
            // portion is used as the map key
            String id = thread.getCode();
            String key;
            if (id.contains(" ")) {
                key = id.split("\\s")[0];
            } else {
                key = id;
            }
            shortThreadList = threadMap.get(key);

            if (shortThreadList == null) {
                shortThreadList = new ArrayList<UUID>();
            }

            shortThreadList.add(threadId);

            threadMap.put(key, shortThreadList);
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

    // check to see if the thread is the same
    private boolean isSameThread(StashThread thread, String source, String type, String id, String key) {
        if (thread.getSource() != null && thread.getSource().equalsIgnoreCase(source)) {
            // same source
            if (thread.getType() != null && thread.getType().equalsIgnoreCase(type)) {
                // same type
                if (thread.getCode() != null && thread.getCode().equalsIgnoreCase(id)) {
                    // same code, so the same
                    return true;
                } else if (thread.getCode() != null && (thread.getCode().split("\\s")[0].equalsIgnoreCase(id) || key.equalsIgnoreCase(thread.getCode()))) {
                    // check if the code has a bit (likely numeric) in front that matches the stored code (or vice versa)
                    return true;
                }
            }
        }

        return false;
    }

}
