package edu.tamu.geoinnovation.fpx.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import edu.tamu.geoinnovation.fpx.Activities.MainActivity;

/**
 * Created by atharmon on 3/24/2016.
 */
public class NetConManager extends BroadcastReceiver {
    private Context mContext;
    private ConnectivityManager mConn;
    private NetworkInfo networkInfo;
    private boolean onlineViaWifi;
    private boolean onlineViaMobile;
    private static final int CELL = 0;
    private static final int WIFI = 1;

    public NetConManager() {

    }

    public NetConManager(Context context) {
        this.mContext = context;
        mConn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = mConn.getActiveNetworkInfo();
        try {
            onlineViaWifi = mConn.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
            onlineViaMobile = mConn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI)
            MainActivity.usingWifi = true;
        else
            MainActivity.usingWifi = false;
    }

    public boolean isOnline() {
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

    public static int getOnlineService() {
        int ret = -1;


        return ret;
    }


}

