package edu.tamu.geoinnovation.fpx.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atharmon on 2/26/2016.
 */
public class GeoInnovationUtils {
    private static final int PERMISSION_LOCATION = 1;

    public static boolean checkPermission(Activity activity, String permission) {
        boolean hasPermission = false;
        int locationCheck = ContextCompat.checkSelfPermission(activity, permission);
        if (locationCheck == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_LOCATION);
        }


        return hasPermission;
    }

    public static String[] permissionLookup(int sensorType) {
        String[] ret = {};
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:

                break;

            case Sensor.TYPE_PROXIMITY:

                break;

            case Sensor.TYPE_LIGHT:

                break;
        }

        return ret;
    }

}
