package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Class to write the stash data to a text file that can then be passed by the app to another app
 * for uploading to Dropbox/emailing/etc.  The stash is written in the pattern of threads / fabrics
 * not assigned / embellishments / patterns (pattern info / fabric / threads / embellishments).  The
 * string is constructed in its entirety before being written to the file.
 */
public class StashExporter {

    private static String mFilename;
    private static String UTF8 = "utf8";
    private static String newline;
    public static final String TAG = "StashExporter";
    private static ArrayList<UUID> mOrphanThread;
    private static ArrayList<UUID> mOrphanEmbellishments;

    public StashExporter() {
        mFilename = "stash_export.txt";

        // newline = System.getProperty("line.separator") would be the proper way to do that but
        // this does not display the linebreaks properly in Windows Notepad, whereas this does.
        // Since users are not likely to be technically savvy, this matters.
        newline = "\r\n";
    }

    public File exportStash(Context context) throws IOException {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, mFilename);

        Writer writer = null;
        try {
            FileOutputStream out = new FileOutputStream(file);
            writer = new OutputStreamWriter(out, UTF8);
            writer.write(buildStashString(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        MediaScannerConnection.scanFile(context, new String[] { file.getPath() }, new String[] { "file/text" }, null);

        return file;

    }

    public String buildStashString(Context context) {
        StashData stash = StashData.get(context);
        mOrphanThread = new ArrayList<UUID>(stash.getThreadList());
        mOrphanEmbellishments = new ArrayList<UUID>(stash.getEmbellishmentList());

        StringBuilder sb = new StringBuilder();

        String betweenItems = "---";
        String betweenCategories = "***";

        // build thread stash
        sb.append(threadListString(stash.getThreadStashList(), betweenItems, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // build fabric stash
        sb.append(fabricListString(stash, betweenItems));
        sb.append(betweenCategories);
        sb.append(newline);

        // build embellishment stash
        sb.append(embellishmentListString(stash.getEmbellishmentStashList(), betweenItems, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // build pattern stash
        sb.append(patternListString(betweenItems, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // if there are any threads that have not been added, append them now
        if (!mOrphanThread.isEmpty()) {
            sb.append(orphanThreadListString(mOrphanThread, betweenItems, stash, context));
        }

        sb.append(betweenCategories);
        sb.append(newline);

        // if there are any embellishemnts that have not been added, append them now
        if (!mOrphanEmbellishments.isEmpty()) {
            sb.append(orphanEmbellishmentListString(mOrphanEmbellishments, betweenItems, stash, context));
        }

        return sb.toString();
    }

    // for threads in the stash
    private String threadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(threadList, new StashThreadComparator(context));

        for (UUID threadId : threadList) {
            // get the thread for each entry on the list
            StashThread thread = stash.getThread(threadId);
            mOrphanThread.remove(threadId);

            if (!thread.getSource().equals(currentSource) || !thread.getType().equals(currentType)) {
                // not equal to the previous source/type
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not a new file
                    newItem = true;
                }
            }

            if (newItem) {
                // not a new file, so add the marker between items before the source and type
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if it is a brand new file, just write the source and type
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            }

            for (int i = 0; i < thread.getSkeinsOwned(); i++) {
                // write the number n times, where n is the quantity owned
                sb.append(thread.getCode());
                sb.append(newline);
            }

            // set to match the last item entered, and reset the "new item" marker
            currentSource = thread.getSource();
            currentType = thread.getType();
            newItem = false;
        }

        return sb.toString();
    }

    // for threads called for in a pattern
    private String threadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context, StashPattern pattern) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(threadList, new StashThreadComparator(context));

        for (UUID threadId : threadList) {
            // get the thread for each entry on the list
            StashThread thread = stash.getThread(threadId);
            mOrphanThread.remove(threadId);

            if (!thread.getSource().equals(currentSource) || !thread.getType().equals(currentType)) {
                // not equal to the previous source / type
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not the first item of the thread list
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first item on the thread list, so needs the divider
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if the first thread entered for the pattern
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            }

            // not the number owned but the number called for by the pattern
            for (int i = 0; i < pattern.getQuantity(thread); i++) {
                sb.append(thread.getCode());
                sb.append(newline);
            }

            // set to match the last item entered, and reset the "new item" marker
            currentSource = thread.getSource();
            currentType = thread.getType();
            newItem = false;
        }

        return sb.toString();
    }

    // for miscellaneous other threads
    private String orphanThreadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(threadList, new StashThreadComparator(context));

        for (UUID threadId : threadList) {
            // get the thread for each entry on the list
            StashThread thread = stash.getThread(threadId);

            if (!thread.getSource().equals(currentSource) || !thread.getType().equals(currentType)) {
                // not equal to the previous source/type
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not a new file
                    newItem = true;
                }
            }

            if (newItem) {
                // not a new file, so add the marker between items before the source and type
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if it is a brand new file, just write the source and type
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            }

            // write the number n times, where n is the quantity owned
            sb.append(thread.getCode());
            sb.append(newline);

            // set to match the last item entered, and reset the "new item" marker
            currentSource = thread.getSource();
            currentType = thread.getType();
            newItem = false;
        }

        return sb.toString();
    }

    private String fabricListString(StashData stash, String betweenItems) {
        StringBuilder sb = new StringBuilder();

        ArrayList<UUID> fabricList = stash.getFabricList();

        for (UUID fabricId : fabricList) {
            StashFabric fabric = stash.getFabric(fabricId);

            // skip fabrics assigned to patterns because they will be included with the pattern
            if (!fabric.isAssigned()) {
                if (sb.length() != 0) {
                    sb.append(betweenItems);
                    sb.append(newline);
                }

                sb.append(fabric.getSource());
                sb.append(newline);
                sb.append(fabric.getType());
                sb.append(newline);
                sb.append(fabric.getColor());
                sb.append(newline);

                sb.append(fabric.getCount());
                sb.append(newline);
                sb.append(fabric.getWidth());
                sb.append(newline);
                sb.append(fabric.getHeight());
                sb.append(newline);
            }
        }

        return sb.toString();
    }

    // for embellishments in stash
    private String embellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(embellishmentList, new StashEmbellishmentComparator(context));

        for (UUID embellishmentId : embellishmentList) {
            // get the thread for each entry on the list
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
            mOrphanEmbellishments.remove(embellishmentId);

            if (!embellishment.getSource().equals(currentSource) || !embellishment.getType().equals(currentType)) {
                // not the same as the previous item entered
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not the first embellishment entered
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first embellishment entered, so add the divider
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // first embellishment entered, so no divider
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            }

            for (int i = 0; i < embellishment.getNumberOwned(); i++) {
                // add the the code n times, where n is the number owned
                sb.append(embellishment.getCode());
                sb.append(newline);
            }

            currentSource = embellishment.getSource();
            currentType = embellishment.getType();
            newItem = false;
        }

        return sb.toString();
    }

    // for embellishments called for in a pattern
    private String embellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context, StashPattern pattern) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(embellishmentList, new StashEmbellishmentComparator(context));

        for (UUID embellishmentId : embellishmentList) {
            // get the thread for each entry on the list
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
            mOrphanEmbellishments.remove(embellishmentId);

            if (!embellishment.getSource().equals(currentSource) || !embellishment.getType().equals(currentType)) {
                // not the same as the previous embellishment entered
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not the first embellishment entered
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first embellishment entered, so add the divider
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // first embellishment entered, so no divider
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            }

            for (int i = 0; i < pattern.getQuantity(embellishment); i++) {
                // if it's called for n times in the pattern, add the number n times to the list
                sb.append(embellishment.getCode());
                sb.append(newline);
            }

            // reset to account for the item just entered
            currentSource = embellishment.getSource();
            currentType = embellishment.getType();
            newItem = false;
        }

        return sb.toString();
    }

    // for miscellaneous other embellishments
    private String orphanEmbellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(embellishmentList, new StashEmbellishmentComparator(context));

        for (UUID embellishmentId : embellishmentList) {
            // get the thread for each entry on the list
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);

            if (!embellishment.getSource().equals(currentSource) || !embellishment.getType().equals(currentType)) {
                // not the same as the previous item entered
                if (!currentSource.equals("") || !currentType.equals("")) {
                    // not the first embellishment entered
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first embellishment entered, so add the divider
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // first embellishment entered, so no divider
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            }

            // add the the code n times, where n is the number owned
            sb.append(embellishment.getCode());
            sb.append(newline);

            currentSource = embellishment.getSource();
            currentType = embellishment.getType();
            newItem = false;
        }

        return sb.toString();
    }

    private String patternListString(String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String patternItems = "-";
        String patternCategories = "*";

        ArrayList<StashPattern> patternList = stash.getPatternData();
        Collections.sort(patternList, new StashPatternComparator());

        for (StashPattern pattern : patternList) {
            if (sb.length() != 0) {
                // not the first thing entered into the string, so add the divider
                sb.append(betweenItems);
                sb.append(newline);
            }

            sb.append(pattern.getPatternName());
            sb.append(newline);
            sb.append(pattern.getSource());
            sb.append(newline);

            sb.append(pattern.getWidth());
            sb.append(newline);
            sb.append(pattern.getHeight());
            sb.append(newline);

            sb.append(patternCategories);
            sb.append(newline);

            // if there is a fabric associated with this pattern, this is where it goes
            if (pattern.getFabric() != null) {
                StashFabric fabric = pattern.getFabric();

                sb.append(fabric.getSource());
                sb.append(newline);
                sb.append(fabric.getType());
                sb.append(newline);
                sb.append(fabric.getColor());
                sb.append(newline);

                sb.append(fabric.getCount());
                sb.append(newline);
                sb.append(fabric.getWidth());
                sb.append(newline);
                sb.append(fabric.getHeight());
                sb.append(newline);
            }

            sb.append(patternCategories);
            sb.append(newline);

            // add the info for the strings in the pattern
            sb.append(threadListString(pattern.getThreadList(), patternItems, stash, context, pattern));
            sb.append(patternCategories);
            sb.append(newline);

            // add the info for the embellishments in the pattern
            sb.append(embellishmentListString(pattern.getEmbellishmentList(), patternItems, stash, context, pattern));
        }

        return sb.toString();
    }

}
