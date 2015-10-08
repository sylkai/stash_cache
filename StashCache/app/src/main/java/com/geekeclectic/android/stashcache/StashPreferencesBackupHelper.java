package com.geekeclectic.android.stashcache;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by sylk on 10/7/2015.
 */
public class StashPreferencesBackupHelper extends BackupAgentHelper {

    static final String PREFS_BACKUP_KEY = "prefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, StashConstants.STASH_PREFERENCES_NAME);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

}
