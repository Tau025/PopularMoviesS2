package com.devtau.popularmoviess2.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
/**
 * Служба для обеспечения доступа SyncAdapter к Authenticator
 * The service which allows the sync adapter framework to access the authenticator
 */
public class AuthenticatorService extends Service {
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
