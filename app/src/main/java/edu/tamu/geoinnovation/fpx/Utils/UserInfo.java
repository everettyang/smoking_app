package edu.tamu.geoinnovation.fpx.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by atharmon on 4/15/2016.
 */
public class UserInfo {
    public static final String PREFS_NAME = "fpx";
    public static final String USER_GUID = "userGuid";
    public static String userGuid;

    public static void setUserGuid(Context activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, activity.MODE_PRIVATE);
        userGuid = preferences.getString(USER_GUID, null);
    }

    public static void removeUserGuid(Context activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, activity.MODE_PRIVATE);
        preferences.edit().remove(USER_GUID).commit();
    }

}

