package edu.tamu.geoinnovation.fpx.Activities;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by atharmon on 6/22/2016.
 */
public class PebbleService extends Service {
    final UUID PEBBLE_APP_UUID = UUID.fromString("bb039a8e-f72f-43fc-85dc-fd2516c7f328");//"b32d0883-4ed5-445a-a4ea-cac457f48c12");
    private PebbleKit.PebbleDataReceiver mDataReceiver;
    private BroadcastReceiver receiver;
    ArrayList<Messenger> mClients = new ArrayList<>(); // Keeps track of all current registered clients.
    static final int MSG_SET_STRING_VALUE = 3;
    public static String STATUS_REQ = "com.example.gestures.UP_STATUS";
    public static String STATUS_MSG = "com.example.gestures.MSG_STATUS";
    private LocalBroadcastManager broadcaster;
    private String direction = "Down";
    boolean start;
    String gestures[];
    Intent gestureintent = new Intent();
    private String[] pebbleModels = {"Unknown", "PebbleOriginal", "PebbleSteel", "PebbleTime", "PebbleTimeSteel", "PebbleTimeRound14", "PebbleTimeRound20"};

    private static final String TAG = "PebbleService";
    private static final int SAMPLES_PER_UPDATE = 5;   // Must match the watchapp value
    private static final int ELEMENTS_PER_PACKAGE = 10;

    public void pebbleCommunication() {
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);

        Log.d(TAG, "Registering DataHandler");

        mDataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

            int instance = 0;
            @Override
            public void receiveData(Context context, int transactionID, PebbleDictionary data) {

                PebbleKit.sendAckToPebble(context, transactionID);
                String dataString;
                Integer[] coordinates = new Integer[ELEMENTS_PER_PACKAGE];
                int delimiter_num = 0;
                boolean corrupted = false;
                StringBuilder builder = new StringBuilder();
                String[] dataName = {"Time", "TimeR", "PebbleAccX", "PebbleAccY", "PebbleAccZ", "Major", "Minor", "Patch", "Device", "Heading"};
                for(int i = 0; i < SAMPLES_PER_UPDATE; i++) {
                    JSONObject ret = new JSONObject();
//                    Log.d(TAG, String.valueOf(data.size()));
                    for(int j = 0; j < ELEMENTS_PER_PACKAGE; j++) {
                        if (j == 0 || j == 1|| j == 5 || j == 6 || j == 7 || j == 9) {
                            if(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + j) != null) {
                                coordinates[j] = data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + j).intValue();
//                                Log.d(TAG, dataName[j] + " " + String.valueOf(coordinates[j]));

                            } else {
//                                Log.e(TAG, "T Item " + i + " does not exist");
                            }
                        } else {
                            if(data.getInteger((i * ELEMENTS_PER_PACKAGE) + j) != null) {
                                coordinates[j] = data.getInteger((i * ELEMENTS_PER_PACKAGE) + j).intValue();
                                Log.d(TAG, dataName[j] + " " + String.valueOf(coordinates[j]));
                            } else {
//                                Log.e(TAG, "B Item " + i + " does not exist");
                            }
                        }
                        try {
                            if (j == 0) {
//                                String timeR = (String.valueOf(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + (j + 1))));
//                                ret.put("PebbleAccT", String.valueOf(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + j)) + String.format("%03d", data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + (j + 1))));
                                long epoch = Long.parseLong(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + j) + String.format("%03d", data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + (j + 1))));
                                ret.put("PebbleAccT", MainActivity.getDateTimeFromUnix(epoch));
//                                if (coordinates[j + 1] == null) {
//                                    Log.d(TAG, "i: " + i + ", j: " + j);
//                                }
                            } else if(j == 5) {
                                ret.put("PebbleOS", String.valueOf(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + j)) + "." + String.valueOf(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + (j + 1))) + "." + String.valueOf(data.getUnsignedIntegerAsLong((i * ELEMENTS_PER_PACKAGE) + (j + 2))));
                            } else if (j == 8) {
                                ret.put("PebbleDevice", pebbleModels[data.getInteger((i * ELEMENTS_PER_PACKAGE) + j).intValue()]);
                            } else if (j != 1 && j != 6 && j!= 7 && j!= 8) {
//                                ret.put(dataName[j], String.valueOf(coordinates[j]));
                                ret.put(dataName[j], coordinates[j]);
                            }

                        } catch (JSONException e) {
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
//                    if (ret.length() != 0) {
//                        Log.d(TAG, ret.toString());
//                    }
//                    Log.d(TAG, MainActivity.createJsonPebble(ret).toString());
                    MainActivity.stringRep =  MainActivity.createJsonPebble(ret).toString();
                    MainActivity.sendToServer();
                }

            }
        };
        // Register DataLogging Receiver
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);
        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), MainActivity.receiver);
//        PebbleKit.registerReceivedAckHandler(getApplicationContext(),
//                new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
//
//                    @Override
//                    public void receiveAck(Context context, int i) {
//                        Log.d(TAG, "receiveACK");
//                    }
//
//                });
//
//        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
//            @Override
//            public void receiveNack(Context context, int transactionId) {
//                Log.d(TAG, "receiveNACK");
//            }
//        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting...");
        start = false;
        broadcaster = LocalBroadcastManager.getInstance(this);


        pebbleCommunication();
        return startId;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "END PEBBLE SERVICE onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "END PEBBLE SERVICE onDestroy");
    }
}
