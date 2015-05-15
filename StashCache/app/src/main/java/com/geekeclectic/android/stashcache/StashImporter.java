package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
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
    private static String stashBlockDivider = StashConstants.BETWEEN_CATEGORIES;
    private static String stashTypeDivider = StashConstants.BETWEEN_ITEMS;
    private static String patternBlockDivider = StashConstants.PATTERN_CATEGORIES;
    private static String patternTypeDivider = StashConstants.PATTERN_ITEMS;
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

        // read in what already is in stash into shorted key maps for easy finding
        populateMaps(stash);

        BufferedReader reader = null;

        try {
            // open and read the file into a StringBuilder
            // InputStream in = am.open(mFilename);
            reader = new BufferedReader(new InputStreamReader(in, UTF8));

            Log.d(TAG, "Reader successfully opened.");

            // lastRead keeps track of what the previously read line was for passing between functions
            lastRead = "";

            readStashThread(reader, stash);
            readStashFabric(reader, stash);
            readStashEmbellishment(reader, stash);

            readPatterns(reader, stash, true);

            readThread(reader, stash);
            readEmbellishment(reader, stash);

            readPatterns(reader, stash, false);

        } catch (FileNotFoundException e) {
            // ignore because it happens when program is opened for the first time
        } catch (ParseException e) {
            // only happens if the user screws something up
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        // return the proper constant to let the user know if everything imported properly
        if (fileFormattedCorrectly && allNumbersFormatted) {
            return StashConstants.FILE_IMPORT_ALL_CLEAR;
        } else if (!fileFormattedCorrectly) {
            return StashConstants.FILE_INCORRECT_FORMAT;
        } else {
            return StashConstants.NUMBER_FORMAT_INCORRECT;
        }
    }

    // called to read in the info for one pattern
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

            // move forward one line end on the *
            lastRead = reader.readLine();
        }

        return pattern;
    }

    private void readPatterns(BufferedReader reader, StashData stash, boolean inStash) throws IOException, ParseException {
        while ((lastRead = reader.readLine()) != null && !lastRead.equals(stashBlockDivider)) {

            StashPattern pattern = readPatternInfo(reader, stash);

            // if something went wrong in the creation of the pattern, break this loop
            if (pattern == null) {
                break;
            }

            pattern.setInStash(inStash);

            // patterns are added to stash automatically on creation, so remove it if it should not
            // be in the stash
            if (!inStash) {
                stash.removePatternFromStash(pattern);
            }

            // try to read in the fabric info for the pattern, if there is some (break if an error)
            if (!readPatternFabric(reader, stash, pattern)) {
                break;
            }

            readPatternThread(reader, stash, pattern);
            readPatternEmbellishment(reader, stash, pattern);

            if (lastRead == null || lastRead.equals(stashBlockDivider)) {
                break;
            }

            readFinishes(reader, stash, pattern);

            if (lastRead == null || lastRead.equals(stashBlockDivider)) {
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

            // try to read in the count number
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

            // try to read in the width number
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

            // try to read in the height number
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

            StashFabric fabric = createNewFabric(source, type, color, count, width, height, stash);

            lastRead = reader.readLine();

            // if there are notes info, prep to read it in
            if (!lastRead.equals(stashBlockDivider) && !lastRead.equals(stashTypeDivider)) {
                StringBuilder sb = new StringBuilder();
                // save the first bit that was read in
                sb.append(lastRead);
                sb.append(System.getProperty("line.separator"));

                // add the additional note info
                while (!(lastRead = reader.readLine()).equals(stashBlockDivider) && !lastRead.equals(stashTypeDivider)) {
                    sb.append(lastRead);
                    sb.append(System.getProperty("line.separator"));
                }

                // cut off the last line separator
                sb.setLength(sb.length() - 1);

                // eliminate the extra spaces added to keep the notes from mixing up the importer
                fabric.setNotes(sb.toString().replace(" * ", StashConstants.PATTERN_CATEGORIES).replace(" - ", StashConstants.PATTERN_ITEMS));
            }

            // finished reading stash fabrics, so break out of the loop
            if (lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private boolean readPatternFabric(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException, ParseException {
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

            // try to read in the count
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

            // try to read in the width
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

            // try to read in the height
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

            StashFabric fabric = createNewFabric(source, type, color, count, fabric_width, fabric_height, stash);

            pattern.setFabric(fabric);
            fabric.setUsedFor(pattern);

            // if the next line is "in use", the fabric is marked as in use
            lastRead = reader.readLine();
            if (lastRead.equals(StashConstants.IN_USE)) {
                fabric.setUse(true);

                lastRead = reader.readLine();
                if (!lastRead.equals("")) {
                    fabric.setStartDate(ISO8601.toCalendar(lastRead));
                }

                // move forward one line to skip the first *
                lastRead = reader.readLine();
            // fabric is not in use, so we need to skip the empty lines for that info
            } else {
                lastRead = reader.readLine();
                lastRead = reader.readLine();
            }

            // if there are notes, read them in
            if (!lastRead.equals(patternBlockDivider)) {
                StringBuilder sb = new StringBuilder();
                // save the read information
                sb.append(lastRead);
                sb.append(System.getProperty("line.separator"));

                // read in the additonal information
                while (!(lastRead = reader.readLine()).equals(patternBlockDivider) && !lastRead.equals(patternTypeDivider)) {
                    sb.append(lastRead);
                    sb.append(System.getProperty("line.separator"));
                }

                // cut off the last line separator
                sb.setLength(sb.length() - 1);

                // fix the extra spaces added to keep the importer happy
                fabric.setNotes(sb.toString().replace(" * ", StashConstants.PATTERN_CATEGORIES).replace(" - ", StashConstants.PATTERN_ITEMS));
            }
        }

        return true;
    }

    private void readFinishes(BufferedReader reader, StashData stash, StashPattern pattern) throws IOException, ParseException {
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

            // try to read in count
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

            // try to read in width
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

            // try to read in height
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

            StashFabric fabric = createNewFabric(source, type, color, count, fabric_width, fabric_height, stash);

            // fabric is for a finish, so make all the connections and remove it from the stash
            fabric.setUsedFor(pattern);
            fabric.setComplete(true);
            pattern.addFinish(fabric);
            stash.removeFabricFromStash(fabric.getId());

            // read in start date (process if line is not empty)
            lastRead = reader.readLine();
            if (!lastRead.equals("")) {
                fabric.setStartDate(ISO8601.toCalendar(lastRead));
            }

            // read in finish date (process if line is not empty)
            lastRead = reader.readLine();
            if (!lastRead.equals("")) {
                fabric.setEndDate(ISO8601.toCalendar(lastRead));
            }

            lastRead = reader.readLine();

            // if there are notes, read them in
            if (!lastRead.equals(patternBlockDivider) && !lastRead.equals(stashTypeDivider)) {
                StringBuilder sb = new StringBuilder();

                // save the previously read info
                sb.append(lastRead);
                sb.append(System.getProperty("line.separator"));

                // read to the end of the notes
                while ((lastRead = reader.readLine()) != null && (!lastRead.equals(patternBlockDivider) && !lastRead.equals(patternTypeDivider) && !lastRead.equals("---"))) {
                    sb.append(lastRead);
                    sb.append(System.getProperty("line.separator"));
                }

                // cut off the last line separator
                sb.setLength(sb.length() - 1);

                // fix the extra spaces added to keep the importer happy
                fabric.setNotes(sb.toString().replace(" * ", StashConstants.PATTERN_CATEGORIES).replace(" - ", StashConstants.PATTERN_ITEMS));
            }

            // break the loop at the end of the file/section/item
            if (lastRead == null || lastRead.equals(StashConstants.BETWEEN_CATEGORIES) || lastRead.equals(StashConstants.BETWEEN_ITEMS)) {
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
                // if there are spaces in the title, split on the space and use the first section as the key
                if (lastRead.contains(" ")) {
                    key = lastRead.split("\\s")[0];
                } else {
                    key = lastRead;
                }
                incrementOrAddThread(source, type, lastRead, key, stash);
            }

            // if we broke out of the previous loop due to end of the block, break out of the larger
            // loop before reading the next line
            if (lastRead != null && lastRead.equals(stashBlockDivider)) {
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
                // if there are spaces in the title, split on the space and use the first section as the key
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
            // break if we encounter * indicating the end of the thread block (for when user entered no threads
            if (lastRead.equals(patternBlockDivider)) {
                break;
            }

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
                // if there are spaces in the title, split on the space and use the first section as the key
                if (lastRead.contains(" ")) {
                    key = lastRead.split("\\s")[0];
                } else {
                    key = lastRead;
                }
                StashThread thread = findOrAddThread(source, type, lastRead, key, stash);

                // used in the pattern, so wire this up
                thread.usedInPattern(pattern);
                pattern.increaseQuantity(thread);
            }

            // found the end of the thread block (user entered threads) so break
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

            if (lastRead != null && lastRead.equals(stashBlockDivider)) {
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
            // user entered no threads, but there is finish information/another pattern/end of pattern block
            if (lastRead.equals(patternBlockDivider) || lastRead.equals(stashTypeDivider) || lastRead.equals(stashBlockDivider)) {
                break;
            }

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

                // used in the pattern, so wire it up
                embellishment.usedInPattern(pattern);
                pattern.increaseQuantity(embellishment);
            }

            // user did enter embellishments, but end of file/finish information/another pattern/end of pattern block
            if (lastRead == null || lastRead.equals(patternBlockDivider) || lastRead.equals(stashTypeDivider) || lastRead.equals(stashBlockDivider)) {
                break;
            }
        }
    }

    private StashThread createNewThread(String source, String type, String id, String key, StashData stash, boolean inStash) {
        StashThread thread = new StashThread(mContext);

        // set info for the new thread
        thread.setSource(source);
        thread.setType(type);
        thread.setCode(id);
        if (inStash) {
            thread.increaseOwnedQuantity();
        }

        // add the thread to the stash
        stash.addThread(thread);

        // add the thread to the list for that key
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

    private StashFabric createNewFabric(String source, String type, String color, int count, Double width, Double height, StashData stash) {
        StashFabric fabric = new StashFabric(mContext);

        // set all appropriate fields for the fabric
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

        // set the appropriate fields
        embellishment.setSource(source);
        embellishment.setType(type);
        embellishment.setCode(id);
        if (inStash) {
            embellishment.increaseOwned();
        }

        stash.addEmbellishment(embellishment);

        // add the id to the lookup map (don't need to do the splitting like we do for thread)
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

        // if there is a list of things for the key, check to see if any match the thread
        if (threadList != null) {
            for (UUID threadId : threadList) {
                thread = stash.getThread(threadId);
                if (isSameThread(thread, source, type, id, key)) {
                    // if it is the same thread but has a different key, save the longer form of the id
                    if (!key.equals(id) && key.equalsIgnoreCase(thread.getCode())) {
                        thread.setCode(id);
                    }

                    // this is only called for things in stash, so increase the number owned
                    thread.increaseOwnedQuantity();
                    return;
                }
            }

            // didn't find a match, so make a new thread
            createNewThread(source, type, id, key, stash, true);
        } else {
            // there was no list, so create a new thread
            createNewThread(source, type, id, key, stash, true);
        }
    }

    private StashThread findOrAddThread(String source, String type, String id, String key, StashData stash) {
        ArrayList<UUID> threadList = threadMap.get(key);
        StashThread thread;

        // check to see if the thread exists on the list for that key
        if (threadList != null) {
            for (UUID threadId : threadList) {
                thread = stash.getThread(threadId);
                if (isSameThread(thread, source, type, key, id)) {
                    // if it is the same thread and the key is a shorter version of the id, save the
                    // longer form of the id
                    if (!key.equals(id) && key.equalsIgnoreCase(thread.getCode())) {
                        thread.setCode(id);
                    }

                    // return the thread for connections (called for patterns)
                    return thread;
                }
            }
        }

        // no matching thread found, so create a new one and return it to the calling function
        thread = createNewThread(source, type, id, key, stash, false);

        return thread;
    }

    private void incrementOrAddEmbellishment(String source, String type, String id, StashData stash) {
        ArrayList<UUID> embellishmentList = embellishmentMap.get(id);
        StashEmbellishment embellishment;

        // check to see if the embellishment is on the list
        if (embellishmentList != null) {
            for (UUID embellishmentId : embellishmentList) {
                embellishment = stash.getEmbellishment(embellishmentId);
                if (embellishment.getSource().equalsIgnoreCase(source) && embellishment.getType().equalsIgnoreCase(type) && embellishment.getCode().equalsIgnoreCase(id)) {
                    // increment up the number owned (only called for the stash building)
                    embellishment.increaseOwned();
                    return;
                }
            }

            // no match found, so make a new embellishment
            createNewEmbellishment(source, type, id, stash, true);
        } else {
            // no list, so create a new embellishment
            createNewEmbellishment(source, type, id, stash, true);
        }
    }

    private StashEmbellishment findOrAddEmbellishment(String source, String type, String id, StashData stash) {
        ArrayList<UUID> embellishmentList = embellishmentMap.get(id);
        StashEmbellishment embellishment;

        // check to see if the embellishment is on the list
        if (embellishmentList != null) {
            for (UUID embellishmentId : embellishmentList) {
                embellishment = stash.getEmbellishment(embellishmentId);
                if (embellishment.getSource().equalsIgnoreCase(source) && embellishment.getType().equalsIgnoreCase(type) && embellishment.getCode().equalsIgnoreCase(id)) {
                    // return the match
                    return embellishment;
                }
            }

            // no match, so create a new one
            embellishment = createNewEmbellishment(source, type, id, stash, false);
        } else {
            // no list so no match, create a new one
            embellishment = createNewEmbellishment(source, type, id, stash, false);
        }

        // return the embellishment
        return embellishment;
    }

    // if the string is not null/a divider, this returns false (preventing a break)
    // if it does return true, it marks the file format check as false so that the user can be alerted
    // that there was a formatting issue
    private boolean checkToContinue(String toCheck) {
        if (toCheck != null && !toCheck.equals(StashConstants.BETWEEN_CATEGORIES) && !toCheck.equals(StashConstants.BETWEEN_ITEMS) && !toCheck.equals(StashConstants.PATTERN_CATEGORIES) && !toCheck.equals(StashConstants.PATTERN_ITEMS)) {
            return false;
        } else {
            // there was an error in the formatting somewhere, so flag it and return true to break
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
            if (id != null && id.contains(" ")) {
                key = id.split("\\s")[0];
            } else if (id != null) {
                key = id;
            } else {
                key = "null";
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
            String id = embellishment.getCode();

            if (id == null) {
                id = "null";
            }

            shortEmbellishmentList = embellishmentMap.get(id);

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
