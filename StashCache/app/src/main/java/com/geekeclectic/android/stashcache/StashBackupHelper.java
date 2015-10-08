package com.geekeclectic.android.stashcache;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

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

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized (SingleFragmentActivity.sDataLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized (SingleFragmentActivity.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

}
