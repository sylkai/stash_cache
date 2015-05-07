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
    private static ArrayList<StashPattern> mPatternsNotInStash;

    public StashExporter() {
        // newline = System.getProperty("line.separator") would be the proper way to do that but
        // this does not display the linebreaks properly in Windows Notepad, whereas this does.
        // Since users are not likely to be technically savvy, this matters.
        newline = "\r\n";
    }

    public File exportStash(Context context) throws IOException {
        mFilename = context.getResources().getString(R.string.stash_export);

        // create the new file in the external storage directory
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

        // update the media scanner to let it know that the file has been added
        MediaScannerConnection.scanFile(context, new String[] { file.getPath() }, new String[] { "file/text" }, null);

        // return the file so the user can send it to the desired service for sharing/uploading
        return file;

    }

    public File exportPattern(StashPattern pattern, Context context) throws IOException {
        mFilename = pattern.toString() + StashConstants.TEXT_EXTENSION;

        // create the new file in the external storage directory
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, mFilename);

        Writer writer = null;
        try {
            FileOutputStream out = new FileOutputStream(file);
            writer = new OutputStreamWriter(out, UTF8);
            writer.write(buildPatternString(pattern, context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        // let the media scanner know about the new file
        MediaScannerConnection.scanFile(context, new String[] { file.getPath() }, new String[] { "file/text" }, null);

        // return the file so the user can send it to the desired service for uploading
        return file;

    }

    private String buildStashString(Context context) {
        StashData stash = StashData.get(context);
        mOrphanThread = new ArrayList<UUID>(stash.getThreadList());
        mOrphanEmbellishments = new ArrayList<UUID>(stash.getEmbellishmentList());
        mPatternsNotInStash = new ArrayList<StashPattern>(stash.getPatternData());

        StringBuilder sb = new StringBuilder();

        String betweenItems = StashConstants.BETWEEN_ITEMS;
        String betweenCategories = StashConstants.BETWEEN_CATEGORIES;

        // build thread stash
        sb.append(threadListString(stash.getThreadStashList(), betweenItems, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // build fabric stash
        sb.append(fabricListString(stash, betweenItems, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // build embellishment stash
        sb.append(embellishmentListString(stash.getEmbellishmentStashList(), betweenItems, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // build pattern stash
        sb.append(patternListString(betweenItems, stash, context, true));
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

        sb.append(betweenCategories);
        sb.append(newline);

        // if there is data for patterns no longer in the stash, append them now
        if (!mPatternsNotInStash.isEmpty()) {
            sb.append(patternListString(betweenItems, stash, context, false));
        }

        return sb.toString();
    }

    private String buildPatternString(StashPattern pattern, Context context) {
        StashData stash = StashData.get(context);

        StringBuilder sb = new StringBuilder();

        String betweenCategories = StashConstants.BETWEEN_CATEGORIES;

        // marker for thread stash
        sb.append(betweenCategories);
        sb.append(newline);

        // marker for fabric stash
        sb.append(betweenCategories);
        sb.append(newline);

        // marker for embellishment stash
        sb.append(betweenCategories);
        sb.append(newline);

        // build pattern stash
        sb.append(patternString(pattern, stash, context));
        sb.append(betweenCategories);
        sb.append(newline);

        // marker for orphan threads
        sb.append(betweenCategories);
        sb.append(newline);

        return sb.toString();
    }

    // for threads in the stash
    private String threadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        // needed to keep track of whether we need to write new source and type info
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
                    // will not be the first item on the list, so flag to add the divider
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first item, so add the marker between items before the source and type
                sb.append(betweenItems);
                sb.append(newline);

                // write source and type
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if it is a brand new file, just write the source and type
                // write source and type
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
                    // not the first item of the thread list, so flag it to add the divider
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first item on the thread list, so needs the divider
                sb.append(betweenItems);
                sb.append(newline);

                // write source and type
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if the first thread entered for the pattern
                // write source and type
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

        // needed to keep track if we need to write new source and type info
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
                    // not the first thread, so flag it as a new item
                    newItem = true;
                }
            }

            if (newItem) {
                // not the first item, so add the marker between items before the source and type
                sb.append(betweenItems);
                sb.append(newline);

                // write source and type
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // if it is a brand new file, just write the source and type
                // write source and type
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

    private String fabricListString(StashData stash, String betweenItems, Context context) {
        StringBuilder sb = new StringBuilder();

        ArrayList<UUID> fabricList = stash.getFabricList();
        Collections.sort(fabricList, new StashFabricComparator(context));

        for (UUID fabricId : fabricList) {
            StashFabric fabric = stash.getFabric(fabricId);

            // skip fabrics assigned to patterns because they will be included with the pattern
            if (!fabric.isAssigned()) {
                if (sb.length() != 0) {
                    sb.append(betweenItems);
                    sb.append(newline);
                }

                // write fabric source, type, and color
                sb.append(fabric.getSource());
                sb.append(newline);
                sb.append(fabric.getType());
                sb.append(newline);
                sb.append(fabric.getColor());
                sb.append(newline);

                // write fabric count, width, and height
                sb.append(fabric.getCount());
                sb.append(newline);
                sb.append(fabric.getWidth());
                sb.append(newline);
                sb.append(fabric.getHeight());
                sb.append(newline);

                // not associated with a pattern, so no date info

                // if there are notes, clean up them and append them
                if (fabric.getNotes() != null) {
                    sb.append(cleanUpNotes(fabric.getNotes()));
                    sb.append(newline);
                }
            }
        }

        return sb.toString();
    }

    // for fabrics associated with completions for a particular pattern
    private String completedFabricString(ArrayList<UUID> fabricList, StashData stash, String betweenItems, Context context) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(fabricList, new StashFabricComparator(context));

        for (UUID fabricId : fabricList) {
            // not the first trip through, so add the divider
            if(sb.length() != 0) {
                sb.append(betweenItems);
                sb.append(newline);
            }

            StashFabric fabric = stash.getFabric(fabricId);

            // write fabric source, type, and color
            sb.append(fabric.getSource());
            sb.append(newline);
            sb.append(fabric.getType());
            sb.append(newline);
            sb.append(fabric.getColor());
            sb.append(newline);

            // write fabric count, width, and height
            sb.append(fabric.getCount());
            sb.append(newline);
            sb.append(fabric.getWidth());
            sb.append(newline);
            sb.append(fabric.getHeight());
            sb.append(newline);

            // write the start date if there is one, otherwise leave the line blank
            if (fabric.getStartDate() != null) {
                sb.append(ISO8601.fromCalendar(fabric.getStartDate()));
                sb.append(newline);
            } else {
                sb.append(newline);
            }

            // write the end date if there is one, otherwise leave the line blank
            if (fabric.getEndDate() != null) {
                sb.append(ISO8601.fromCalendar(fabric.getEndDate()));
                sb.append(newline);
            } else {
                sb.append(newline);
            }

            // append notes, if they exist
            if (fabric.getNotes() != null && !fabric.getNotes().equals("")) {
                sb.append(cleanUpNotes(fabric.getNotes()));
                sb.append(newline);
            }
        }

        return sb.toString();
    }

    // for embellishments in stash
    private String embellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        // needed to keep track of whether we need to write new source and type info
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

                // write source and type
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // first embellishment entered, so no divider
                // write source and type
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

        // needed to help keep track of whether we need to write new source and type info
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

                // write source and type
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                // first embellishment entered, so no divider
                // write source and type
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

    private String patternListString(String betweenItems, StashData stash, Context context, boolean inStash) {
        StringBuilder sb = new StringBuilder();

        String patternItems = StashConstants.PATTERN_ITEMS;
        String patternCategories = StashConstants.PATTERN_CATEGORIES;

        ArrayList<StashPattern> patternList = stash.getPatternData();
        Collections.sort(patternList, new StashPatternComparator());

        for (StashPattern pattern : patternList) {
            if (pattern.inStash() == inStash) {
                mPatternsNotInStash.remove(pattern);
                if (sb.length() != 0) {
                    // not the first thing entered into the string, so add the divider
                    sb.append(betweenItems);
                    sb.append(newline);
                }

                // write name and designer
                sb.append(pattern.getPatternName());
                sb.append(newline);
                sb.append(pattern.getSource());
                sb.append(newline);

                // write width and height
                sb.append(pattern.getWidth());
                sb.append(newline);
                sb.append(pattern.getHeight());
                sb.append(newline);

                // if the pattern is kitted, mark it as such (no entry is assumed to not be kitted)
                if (pattern.isKitted()) {
                    sb.append(StashConstants.KITTED);
                    sb.append(newline);
                }

                // end of pattern specifics
                sb.append(patternCategories);
                sb.append(newline);

                // if there is a fabric associated with this pattern, this is where it goes
                if (pattern.getFabric() != null) {
                    StashFabric fabric = pattern.getFabric();

                    // fabric source, type and color
                    sb.append(fabric.getSource());
                    sb.append(newline);
                    sb.append(fabric.getType());
                    sb.append(newline);
                    sb.append(fabric.getColor());
                    sb.append(newline);

                    // fabric count, width, and height
                    sb.append(fabric.getCount());
                    sb.append(newline);
                    sb.append(fabric.getWidth());
                    sb.append(newline);
                    sb.append(fabric.getHeight());
                    sb.append(newline);

                    // if it's in use, write so and add date
                    if (fabric.inUse()) {
                        sb.append(StashConstants.IN_USE);
                        sb.append(newline);

                        if (fabric.getStartDate() != null) {
                            sb.append(ISO8601.fromCalendar(fabric.getStartDate()));
                            sb.append(newline);
                        } else {
                            sb.append(newline);
                        }
                    // need to have the empty lines to avoid mistaking the notes as in use info
                    } else {
                        sb.append(newline);
                        sb.append(newline);
                    }

                    // if there are notes, write notes
                    if (fabric.getNotes() != null) {
                        sb.append(cleanUpNotes(fabric.getNotes()));
                        sb.append(newline);
                    }
                }

                // finished fabric info
                sb.append(patternCategories);
                sb.append(newline);

                // add the info for the strings in the pattern
                sb.append(threadListString(pattern.getThreadList(), patternItems, stash, context, pattern));
                sb.append(patternCategories);
                sb.append(newline);

                // add the info for the embellishments in the pattern
                sb.append(embellishmentListString(pattern.getEmbellishmentList(), patternItems, stash, context, pattern));

                // add info for any previous completions, if any
                if (!pattern.getFinishes().isEmpty()) {
                    sb.append(patternCategories);
                    sb.append(newline);
                    sb.append(completedFabricString(pattern.getFinishes(), stash, patternItems, context));
                }
            }
        }

        return sb.toString();
    }

    private String patternString(StashPattern pattern, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String patternItems = StashConstants.PATTERN_ITEMS;
        String patternCategories = StashConstants.PATTERN_CATEGORIES;

        // write name and designer
        sb.append(pattern.getPatternName());
        sb.append(newline);
        sb.append(pattern.getSource());
        sb.append(newline);

        // write width and height
        sb.append(pattern.getWidth());
        sb.append(newline);
        sb.append(pattern.getHeight());
        sb.append(newline);

        // info if kitted would go here but is excluded from export of pattern for sharing

        sb.append(patternCategories);
        sb.append(newline);

        // fabric info would go here but is excluded from export of pattern for sharing

        sb.append(patternCategories);
        sb.append(newline);

        // add the info for the strings in the pattern
        sb.append(threadListString(pattern.getThreadList(), patternItems, stash, context, pattern));
        sb.append(patternCategories);
        sb.append(newline);

        // add the info for the embellishments in the pattern
        sb.append(embellishmentListString(pattern.getEmbellishmentList(), patternItems, stash, context, pattern));

        return sb.toString();
    }

    // in case the user has included the delimiters used for category/items in the notes field, this
    // adds spaces so that it doesn't get read in incorrectly when importing from the exported stash
    private String cleanUpNotes(String notes) {
        // replace the line separator to preserve the line breaks
        notes = notes.replace(System.getProperty("line.separator"), newline);

        // fix any potential delimiter issues
        notes = notes.replace(StashConstants.PATTERN_CATEGORIES, " * ");
        notes = notes.replace(StashConstants.PATTERN_ITEMS, " - ");

        return notes;
    }

}
