package io.aggreg.app.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import io.aggreg.app.R;

/**
 * Created by Timo on 3/26/15.
 */
public class AccountUtil {
    private Context mContext;

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public final String ACCOUNT_TYPE = mContext.getString(R.string.account_type);
    // The account name
    public final String ACCOUNT = mContext.getString(R.string.account_type);
    // Instance fields

    public AccountUtil(Context mContext){
        this.mContext = mContext;
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;

    }
}
