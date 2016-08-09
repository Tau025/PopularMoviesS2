package com.devtau.popularmoviess2.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.devtau.popularmoviess2.utility.Logger;
/**
 * Служба, на которой крутится наш SyncAdapter
 * Service to run our SyncAdapter on
 */
public class SyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static final String LOG_TAG = SyncService.class.getSimpleName();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Logger.d(LOG_TAG, "onCreate()");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}