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
 * Created by sylk on 3/4/2015.
 */
public class StashExporter {

    private static String mFilename;
    private static String UTF8 = "utf8";
    private static String newline;
    public static final String TAG = "StashExporter";

    public StashExporter() {
        mFilename = "stash_export.txt";

        // newline = System.getProperty("line.separator") would be the proper way to do that but
        // this does not display the linebreaks properly in Windows Notepad, whereas this does.
        // Since users are not likely to be technically savvy, this matters.
        newline = "\r\n";
    }

    public void exportStash(Context context) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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

        MediaScannerConnection.scanFile(context, new String[] { file.getPath() }, new String[] { "text/rtf" }, null);


    }

    public String buildStashString(Context context) {
        StashData stash = StashData.get(context);

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

        return sb.toString();
    }

    private String threadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(threadList, new StashThreadComparator(context));

        for (UUID threadId : threadList) {
            // get the thread for each entry on the list
            StashThread thread = stash.getThread(threadId);

            if (!thread.getSource().equals(currentSource) || !thread.getType().equals(currentType)) {
                if (!currentSource.equals("") || !currentType.equals("")) {
                        newItem = true;
                }
            }

            if (newItem) {
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            }

            for (int i = 0; i < thread.getSkeinsOwned(); i++) {
                sb.append(thread.getCode());
                sb.append(newline);
            }

            currentSource = thread.getSource();
            currentType = thread.getType();
            newItem = false;
        }

        return sb.toString();
    }

    private String threadListString(ArrayList<UUID> threadList, String betweenItems, StashData stash, Context context, StashPattern pattern) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(threadList, new StashThreadComparator(context));

        for (UUID threadId : threadList) {
            // get the thread for each entry on the list
            StashThread thread = stash.getThread(threadId);

            if (!thread.getSource().equals(currentSource) || !thread.getType().equals(currentType)) {
                if (!currentSource.equals("") || !currentType.equals("")) {
                    newItem = true;
                }
            }

            if (newItem) {
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                sb.append(thread.getSource());
                sb.append(newline);
                sb.append(thread.getType());
                sb.append(newline);
            }

            for (int i = 0; i < pattern.getQuantity(thread); i++) {
                sb.append(thread.getCode());
                sb.append(newline);
            }

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

    private String embellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(embellishmentList, new StashEmbellishmentComparator(context));

        for (UUID embellishmentId : embellishmentList) {
            // get the thread for each entry on the list
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);

            if (!embellishment.getSource().equals(currentSource) || !embellishment.getType().equals(currentType)) {
                if (!currentSource.equals("") || !currentType.equals("")) {
                    newItem = true;
                }
            }

            if (newItem) {
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            }

            for (int i = 0; i < embellishment.getNumberOwned(); i++) {
                sb.append(embellishment.getCode());
                sb.append(newline);
            }

            currentSource = embellishment.getSource();
            currentType = embellishment.getType();
            newItem = false;
        }

        return sb.toString();
    }

    private String embellishmentListString(ArrayList<UUID> embellishmentList, String betweenItems, StashData stash, Context context, StashPattern pattern) {
        StringBuilder sb = new StringBuilder();

        String currentSource = "";
        String currentType = "";

        boolean newItem = false;

        Collections.sort(embellishmentList, new StashEmbellishmentComparator(context));

        for (UUID embellishmentId : embellishmentList) {
            // get the thread for each entry on the list
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);

            if (!embellishment.getSource().equals(currentSource) || !embellishment.getType().equals(currentType)) {
                if (!currentSource.equals("") || !currentType.equals("")) {
                    newItem = true;
                }
            }

            if (newItem) {
                sb.append(betweenItems);
                sb.append(newline);
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            } else if (currentSource.equals("") && currentType.equals("")) {
                sb.append(embellishment.getSource());
                sb.append(newline);
                sb.append(embellishment.getType());
                sb.append(newline);
            }

            for (int i = 0; i < embellishment.getNumberOwned(); i++) {
                sb.append(embellishment.getCode());
                sb.append(newline);
            }

            for (int i = 0; i < pattern.getQuantity(embellishment); i++) {
                sb.append(embellishment.getCode());
                sb.append(newline);
            }

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

            sb.append(threadListString(pattern.getThreadList(), patternItems, stash, context, pattern));
            sb.append(patternCategories);
            sb.append(newline);

            sb.append(embellishmentListString(pattern.getEmbellishmentList(), patternItems, stash, context, pattern));
        }

        return sb.toString();
    }

}
