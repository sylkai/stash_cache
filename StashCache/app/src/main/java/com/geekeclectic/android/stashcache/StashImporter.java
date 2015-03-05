package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 1/22/2015.
 */
public class StashImporter {

    private static InputStream in;
    private static int DEFAULT = 1;
    public static final String TAG = "StashImporter";

    public StashImporter(InputStream input) {
        // mFilename = "stash_input.txt";
        in = input;
    }

    public void importStash(Context context) throws IOException {
        StashData stash = StashData.get(context);

        AssetManager am = context.getAssets();
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

                // iterate through to the end of the source/type block and create a new thread for each id
                while ((line = reader.readLine()) != null) {
                    if (line.equals("***") || line.equals("---")) {
                        break;
                    }

                    String id = line;
                    createNewThread(source, type, id, stash, true);
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
                }

                String source = line;
                String type = reader.readLine();
                String color = reader.readLine();
                int count = Integer.parseInt(reader.readLine());
                Double height = Double.parseDouble(reader.readLine());
                Double width = Double.parseDouble(reader.readLine());

                createNewFabric(source, type, color, count, height, width, stash);
            }

            // read in embellishment data; end of the block is marked by *** and will break loop
            while ((line = reader.readLine()) != null && !line.equals("***")) {
                // store source and type information
                String source = line;
                String type = reader.readLine();

                // iterate through to the end of the source/type block and create a new embellishment for each id
                while ((line = reader.readLine()) != null) {
                    if (line.equals("***") || line.equals("---")) {
                        break;
                    }

                    String id = line;
                    createNewEmbellishment(source, type, id, stash, true);
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
                int width = Integer.parseInt(reader.readLine());
                int height = Integer.parseInt(reader.readLine());

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
                    String color = reader.readLine();
                    int count = Integer.parseInt(reader.readLine());
                    Double fabric_height = Double.parseDouble(reader.readLine());
                    Double fabric_width = Double.parseDouble(reader.readLine());

                    StashFabric fabric = createNewFabric(source, type, color, count, fabric_height, fabric_width, stash);

                    pattern.setFabric(fabric);
                    fabric.setUsedFor(pattern);
                    reader.readLine();
                }

                // if thread information is entered, check for thread in stash already and add it if not present
                while ((line = reader.readLine()) != null) {
                    source = line;
                    String type = reader.readLine();
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("*") || line.equals("-")) {
                            break;
                        }

                        String id = line;
                        StashThread thread = findOrAddThread(source, type, id, stash);

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
        }
    }

    private StashThread createNewThread(String source, String type, String id, StashData stash, boolean inStash) {
        StashThread thread = new StashThread();

        thread.setSource(source);
        thread.setType(type);
        thread.setCode(id);
        if (inStash) {
            thread.setSkeinsOwned(DEFAULT);
        }

        stash.addThread(thread);

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

        return embellishment;
    }

    private StashThread findOrAddThread(String source, String type, String id, StashData stash) {
        ArrayList<UUID> threadList = stash.getThreadList();
        StashThread thread;

        for (int i = 0; i < threadList.size(); i++) {
            thread = stash.getThread(threadList.get(i));
            if (thread.getSource().equals(source) && thread.getType().equals(type) && thread.getCode().equals(id)) {
                return thread;
            }
        }

        thread = createNewThread(source, type, id, stash, false);

        return thread;
    }

    private StashEmbellishment findOrAddEmbellishment(String source, String type, String id, StashData stash) {
        ArrayList<UUID> embellishmentList = stash.getEmbellishmentList();
        StashEmbellishment embellishment;

        for (int i = 0; i < embellishmentList.size(); i++) {
            embellishment = stash.getEmbellishment(embellishmentList.get(i));
            if (embellishment.getSource().equals(source) && embellishment.getType().equals(type) && embellishment.getCode().equals(id)) {
                return embellishment;
            }
        }

        embellishment = createNewEmbellishment(source, type, id, stash, false);

        return embellishment;
    }

}
