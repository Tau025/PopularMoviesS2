package com.devtau.popularmoviess2.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
/**
 * Фейковый аутентификатор, необходимый для работы SyncAdapter
 *
 * Manages "Authentication" to backend service. The SyncAdapter framework
 * requires an authenticator object, so syncing to a service that doesn't need authentication
 * typically means creating a stub authenticator like this one.
 */
public class Authenticator extends AbstractAccountAuthenticator {
    public Authenticator(Context context) {
        super(context);
    }


    //У нашего Authenticator нет свойств, которые можно было бы редактировать
    //There are no properties to edit in our Authenticator
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }


    //В действительности мы не добавляем аккаунт на устройство, так что вернем null
    //Because we're not actually adding an account to the device, just return null
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse r, String s, String s2, String[] strings,
                             Bundle bundle) throws NetworkErrorException {
        return null;
    }

    //Получить свойства аккаунта также невозможно
    //Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }


    //Игнорируем попытки подтвердить учетные данные
    //Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account,
                                     Bundle bundle) throws NetworkErrorException {
        return null;
    }


    //Обновить учетные данные также невозможно
    //Updating user credentials is not supported
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse r, Account account, String s,
                                    Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }


    //Получить токен аутентификации также невозможно
    //Getting an authentication token is not supported
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse r, Account account, String s,
                               Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }


    //Получить ярлык токена аутентификации также невозможно
    //Getting a label for the auth token is not supported
    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }
}
