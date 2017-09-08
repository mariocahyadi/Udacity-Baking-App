package com.mario99ukdw.bakingapp.net;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.ContentHandler;

/**
 * Created by mario99ukdw on 10.08.2017.
 */

public class NetworkUtil {
    /**
     * check if internet connection is available
     * Based on a stackoverflow snippet
     * URL : https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
     * @return true if there is Internet. false if not.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
