package com.geekeclectic.android.stashcache;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

/**
 * Created by sylk on 10/7/2015.
 */
public class StashBackupHelper extends BackupAgentHelper {

    static final String FILES_BACKUP_KEY = "stash_backup";

    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, StashConstants.FILENAME);
        addHelper(FILES_BACKUP_KEY, helper);
    }

}
