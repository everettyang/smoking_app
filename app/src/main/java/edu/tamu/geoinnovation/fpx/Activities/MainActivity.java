package edu.tamu.geoinnovation.fpx.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import com.getpebble.android.kit.PebbleKit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import edu.tamu.geoinnovation.fpx.Modules.AppModule;
import edu.tamu.geoinnovation.fpx.Modules.AppModuleResponse;
import edu.tamu.geoinnovation.fpx.Modules.BasicServerResponse;
import edu.tamu.geoinnovation.fpx.Modules.SensorModule;
import edu.tamu.geoinnovation.fpx.Preferences.SettingsActivityV2;
import edu.tamu.geoinnovation.fpx.R;
import edu.tamu.geoinnovation.fpx.RestClient;
import edu.tamu.geoinnovation.fpx.Utils.AppInfo;
import edu.tamu.geoinnovation.fpx.Utils.GIDBHelper;
import edu.tamu.geoinnovation.fpx.Utils.GeoInnovationPermissions;
import edu.tamu.geoinnovation.fpx.Utils.GeoInnovationUtils;
import edu.tamu.geoinnovation.fpx.Utils.NetConManager;
import edu.tamu.geoinnovation.fpx.Utils.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.schedulers.Schedulers;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener, AdapterView.OnItemSelectedListener {

    public static final String TAG  = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;
    private static Location mLastReceivedLocation;
    private static GIDBHelper dbh;
    private SensorManager sensorManager;
    private List<Sensor> sensorList = new ArrayList<>();
    private boolean startUp;
    private String currentGuid;
    private SparseArray<SensorModule> savedValues = new SparseArray<>();
    private HashMap<String, ArrayList<SensorEvent>> savedContinuousValues = new HashMap<>();
    private SparseArray<SensorEvent> savedOnChangeValues = new SparseArray<>();
    private Timer time = new Timer();
    private static String thisActivity = "Nothing";
    private final String NOTHING = "Nothing";
    private final int REPORTING_MODE_CONTINUOUS = 0;
    private final int REPORTING_MODE_ON_CHANGE = 1;
    private final int REPORT_FREQUENCY = 6;  // IN MILLISECONDS
    private final int SENSOR_FREQUENCY = SensorManager.SENSOR_DELAY_UI;
//    private final int SENSOR_FREQUENCY = SensorManager.SENSOR_DELAY_FASTEST;
    private Handler uploadHandler;
    private HandlerThread uploadThread;
    private static JSONObject continuous = new JSONObject();
    private static int counter = 0;
    public static String stringRep;
    final UUID PEBBLE_APP_UUID = UUID.fromString("bb039a8e-f72f-43fc-85dc-fd2516c7f328");
    public static BroadcastReceiver receiver;
    public static String STATUS_REQ = "com.example.gestures.SW_STATUS";
    public static String STATUS_MSG = "com.example.gestures.GE_STATUS";
    private LocalBroadcastManager broadcaster;
    boolean mBound = false;
    private EditText et;
    private ConnectivityManager mConnectivityManager;
    public static boolean usingWifi;
    public boolean usingBluetooth;
    private BroadcastReceiver btReceiver;
    public NetConManager netConManager;
    private static final String collectionName = "doesNotWork";
    private boolean hadToLogin = false;
    private List<AppModule> appModules = new ArrayList<>();
    private List<String> appNames = new ArrayList<>();
    private File currentFile;
    private File path;
    private FileOutputStream outputStream;
    private static OutputStreamWriter outputStreamWriter;

    //Todo: Toolbar error in Android 4.4, related to not using widget.v7.support.Toolbar or something like that
    //DONE DONE DONE Data sent to server should use actual userGuid in module, with REAL times DONE DONE DONE
    //Todo: Fix that major memory leak that is in MainActivityV2, but not in MainActivityV1
    //DONE DONE DONE If no sensors are on, turn the progress bar off DONE DONE DONE
    //Todo: Get upload icon on the toolbar
    //Todo: MainActivityV2 should run as a service in the background
    //Todo: Switch RestClient to FPX server
    //Todo: Add submission frequency slider
    //Todo: Integrate StepCounter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent pebble = new Intent(this, PebbleService.class);
        setContentView(R.layout.activity_main_v3);

        UserInfo.setUserGuid(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hadToLogin = extras.getBoolean(LoginActivity.EXTRA_KEY);
            Log.d(TAG, "hadToLogin " + hadToLogin);
        }


//        Log.d(TAG, UserInfo.userGuid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setActionBar(toolbar);
        thisActivity = NOTHING;

//        timeDelay(REPORT_FREQUENCY);

        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());


        path = getGeocreepDir();

        currentFile = new File(path, fileName + ".txt");

        try {
            if(!currentFile.exists()) {
                currentFile.createNewFile();
            }
            outputStream = new FileOutputStream(currentFile);
            outputStreamWriter = new OutputStreamWriter(outputStream);



        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }


        Call<AppModuleResponse> getAppGuids = RestClient.get().getAppGuidsServer();
        getAppGuids.enqueue(new Callback<AppModuleResponse>() {

            @Override
            public void onResponse(Call<AppModuleResponse> call, Response<AppModuleResponse> response) {
                if (response != null) {
                    if (response.body().items != null) {
                        ArrayList<AppModule> items = response.body().items;
                        appModules = items;
                        for (AppModule module : items) {
                            appNames.add(module.Name);
//                            Log.d(TAG, module.Name);
                        }
                        Spinner appSpinner = ((Spinner) findViewById(R.id.spinner_app));
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, appNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        appSpinner.setAdapter(adapter);
                        appSpinner.setSelection(0);
                        appSpinner.setOnItemSelectedListener(MainActivity.this);
                    } else {
                        Log.d(TAG, "items is null " + response.raw().request().toString());
                    }
                } else {
                    Log.d(TAG, "Response is null getAppGuids");
                }
            }

            @Override
            public void onFailure(Call<AppModuleResponse> call, Throwable t) {
                if (t != null) {
                    Log.d(TAG, t.getLocalizedMessage());
                }
            }

        });




        Spinner spinner = ((Spinner) findViewById(R.id.spinner));
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this, R.array.activities, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(activityAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);

        mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        usingBluetooth = btAdapter.isEnabled();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "Bluetooth off");
                            usingBluetooth = false;
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "Turning Bluetooth off...");
                            usingBluetooth = false;
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "Bluetooth on");
                            usingBluetooth = true;
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "Turning Bluetooth on...");
                            break;
                    }
                }
            }
        };
        registerReceiver(btReceiver, filter);

        IntentFilter filterWifi = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        NetConManager netConManager = new NetConManager();
        registerReceiver(netConManager, filterWifi);


        dbh = new GIDBHelper(this);
        if (GeoInnovationUtils.checkPermission(this, GeoInnovationPermissions.ACCESS_FINE_LOCATION)) {
            buildGoogleApiClient();
        }
        startUp = true;


//        uploadThread = new HandlerThread("sensor-thread", Thread.NORM_PRIORITY);
//        uploadThread.start();
//        uploadHandler = new Handler(uploadThread.getLooper());

        broadcaster = LocalBroadcastManager.getInstance(this);

        final CheckBox checky = (CheckBox) findViewById(R.id.checkbox_mccheckbox);
        View.OnClickListener click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    // CREEP
                    if (usingBluetooth) {
//                        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
//                        currentFile = new File(path, fileName + ".txt");

                        start(pebble);
                    } else {
                        final Snackbar snack = Snackbar.make(checky, "Enable Bluetooth to use Pebble", Snackbar.LENGTH_INDEFINITE);
                        snack.setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snack.dismiss();
                            }
                        });
                        snack.show();
                        checky.setChecked(false);
                    }
                } else {
                    // DISABLE CREEP
                    try {
                        outputStreamWriter.flush();
                    }catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    stop(pebble);
                }
            }
        };
        checky.setOnClickListener(click);

        et = (EditText) findViewById(R.id.other_edittext);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged");
                thisActivity = s.toString();
                Log.d(TAG, thisActivity);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context,  Intent intent) {
//                Log.d(TAG, "Connection to Pebble lost");
                if (checky.isChecked()) {
                    checky.setChecked(false);
                    final Snackbar snack = Snackbar.make(checky, "Connection to Pebble lost", Snackbar.LENGTH_INDEFINITE);
                    snack.setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    });
                    snack.show();
                }
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            pauseAllSensors();
            startPreferenceIntent();
            return true;
        }

        if (id == R.id.action_upload_data) {
            if (dbh != null) {
//                uploadSavedData();
                if (usingWifi) {
//                    uploadSavedDataWithCursor();
                } else {
//                    Log.d(TAG, "No wifi");
                    Toast.makeText(this, "Wifi not enabled, please enable and try again", Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }

        if (id == R.id.action_logout) {
            logout();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(250).setFastestInterval(100);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(750);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (GeoInnovationUtils.checkPermission(this, GeoInnovationPermissions.ACCESS_FINE_LOCATION)) {
            mLastReceivedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastReceivedLocation = location;
//        SensorModule sdm = new SensorModule();
//        sdm.Accuracy = String.valueOf(location.getAccuracy());
//        sdm.TimeStamp = String.valueOf(location.getTime());
//        sdm.SampleRate = "N/A";
//        sdm.Type = "GPS";
//
//        float lat = (float) location.getLatitude();
//        float lon = (float) location.getLongitude();
//
//        sdm.Values = new float[]{lat, lon};
//        savedValues.put(3339, sdm);

//        Log.d(TAG, "Lat: " + lat + ", Lon: " + lon);

        JSONObject obj = new JSONObject();
        try {
            obj.put("Time", new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime()));
            obj.put("Lat", mLastReceivedLocation.getLatitude());
            obj.put("Lon", mLastReceivedLocation.getLongitude());
            obj.put("GPS_Accuracy", mLastReceivedLocation.getAccuracy());
            obj.put("GPS_Timestamp", mLastReceivedLocation.getTime());
            obj.put("GPS_Bearing", mLastReceivedLocation.getBearing());
            obj.put("GPS_Speed", mLastReceivedLocation.getSpeed());
            obj.put("GPS_Altitude", mLastReceivedLocation.getAltitude());
            obj.put("GPS_Provider", mLastReceivedLocation.getProvider());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        String locationRep = obj.toString();
        try {
            outputStreamWriter.append(locationRep);
            outputStreamWriter.append(",\n");

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    protected void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeAllSensors();


    }

    public void onPause() {
        super.onPause();
//        pauseAllSensors();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
//        uploadThread.quitSafely();
        dbh.close();
        if (netConManager != null) {
            unregisterReceiver(netConManager);
        }

        Intent pebble = new Intent(this, PebbleService.class);
        if (btReceiver != null) {
            unregisterReceiver(btReceiver);
        }
        stop(pebble);



        JSONObject obj = new JSONObject();
        try {
            obj.put("Time", new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime()));
            obj.put("Lat", mLastReceivedLocation.getLatitude());
            obj.put("Lon", mLastReceivedLocation.getLongitude());
            obj.put("GPS_Accuracy", mLastReceivedLocation.getAccuracy());
            obj.put("GPS_Timestamp", mLastReceivedLocation.getTime());
            obj.put("GPS_Bearing", mLastReceivedLocation.getBearing());
            obj.put("GPS_Speed", mLastReceivedLocation.getSpeed());
            obj.put("GPS_Altitude", mLastReceivedLocation.getAltitude());
            obj.put("GPS_Provider", mLastReceivedLocation.getProvider());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        String locationRep = obj.toString();
        try {
            outputStreamWriter.append(locationRep);
            outputStreamWriter.append(",\n");

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        try {
            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            continuous.put("UserGuid", UserInfo.userGuid);
            continuous.put("Activity", thisActivity);
            continuous.put("Time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
            continuous.put("PhoneDevice", Build.MANUFACTURER + "-" + Build.MODEL);
            continuous.put("PhoneOS", String.valueOf(Build.VERSION.SDK_INT));
            continuous.put("AppGuid", AppInfo.selectedAppGuid);
            if (mLastReceivedLocation != null) {
                continuous.put("Lat", mLastReceivedLocation.getLatitude());
                continuous.put("Lon", mLastReceivedLocation.getLongitude());
                continuous.put("GPS_Accuracy", mLastReceivedLocation.getAccuracy());
                continuous.put("GPS_Timestamp", mLastReceivedLocation.getTime());
                continuous.put("GPS_Bearing", mLastReceivedLocation.getBearing());
                continuous.put("GPS_Speed", mLastReceivedLocation.getSpeed());
                continuous.put("GPS_Altitude", mLastReceivedLocation.getAltitude());
                continuous.put("GPS_Provider", mLastReceivedLocation.getProvider());
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        String sensorNameTrimmed = event.sensor.getName().replace(" ", "");
        if (event.sensor.getReportingMode() == REPORTING_MODE_CONTINUOUS) {
            String trimName = event.sensor.getStringType().replace(".", "_");
            long dateBase = new Date().getTime();
//            try {
//                continuous.put(trimName + "A", event.accuracy);
//                continuous.put(trimName + "T", String.valueOf(dateBase));
//                continuous.put(trimName + "S", SENSOR_FREQUENCY);
//                continuous.put(trimName + "X", event.values[0]);
//                continuous.put(trimName + "Y", event.values[1]);
//                continuous.put(trimName + "Z", event.values[2]);
//            } catch (JSONException e) {
//                Log.d(TAG, e.getLocalizedMessage());
//            }
            try {
                continuous.put(trimName + "A", event.accuracy);
//                continuous.put(trimName + "T", String.valueOf(dateBase));
//                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
                Date date = new Date(dateBase);
                continuous.put(trimName + "T", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date));
//                continuous.put(trimName + "S", SENSOR_FREQUENCY);
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            for (int i = 0; i < event.values.length; i++) {
                try {
                    continuous.put(trimName + i, event.values[i]);
                } catch (JSONException f) {
                    Log.d(TAG, f.getLocalizedMessage());
                }
            }

        } else if (event.sensor.getReportingMode() == REPORTING_MODE_ON_CHANGE) {
//            Log.d(TAG, "onChange");
            try {
                continuous.put(sensorNameTrimmed, event.values[0]);
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
        stringRep = continuous.toString();
//        if(stringRep != null) {
//            Log.d(TAG, stringRep);
//        }
//        try {
//            outputStreamWriter.append(stringRep);
//            outputStreamWriter.append(",\n");
//
//        } catch (IOException e) {
//            Log.d(TAG, e.getMessage());
//        }

        continuous = new JSONObject();
//        sendToServer();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public List<Sensor> getListOfSensors() {
        if (sensorManager != null) {
            return sensorManager.getSensorList(Sensor.TYPE_ALL);
        } else {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            return sensorManager.getSensorList(Sensor.TYPE_ALL);
        }


    }

    public String getListOfSensorNames() {
        List<Sensor> list = getListOfSensors();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Sensor sens = list.get(i);
            names.add(sens.getName());
        }
        String trimmed = names.toString().trim();
        // REMOVE THE [] SURROUNDING THE STRING
        String fixed = trimmed.substring(1, (trimmed.length() - 1));
//        Log.d(TAG, fixed);
        return fixed;

    }

    public static void sendToServer() {
//        if (usingWifi) {
            Log.d(TAG, stringRep);
            try {
                outputStreamWriter.append(stringRep);
                outputStreamWriter.append(",\n");

            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
//            Call<BasicServerResponse> uploadStuff = RestClient.get().pushSensorDataFlatServer(collectionName, stringRep);
//            uploadStuff.enqueue(new Callback<BasicServerResponse>() {
//
//                @Override
//                public void onResponse(Call<BasicServerResponse> call, Response<BasicServerResponse> response) {
//                    if (response.body() != null) {
//                        if (response.body().ResultCode != null && response.body().ResultCode.equals("200")) {
//                            Log.d(TAG, "SUCCESS");
////                        Snackbar.make(findViewById(R.id.toolbar_main_activity), "Uploaded data", Snackbar.LENGTH_LONG).show();
////                        savedValues.clear();
//                            continuous = new JSONObject();
//                            counter = 0;
//                        } else {
//                            Log.d(TAG, "STATUS CODE NOT 200");
//                            Log.d(TAG, "STATUS CODE: " + response.body().ResultCode);
//                            Log.d(TAG, "RESULT MESSAGE: " + response.body().ResultMessage);
//                        }
//                    } else {
//                        Log.d(TAG, "RESPONSE BODY IS NULL");
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<BasicServerResponse> call, Throwable t) {
//                    if (t.getMessage() != null) {
//                        Log.d(TAG, t.getMessage());
//                    } else {
//                        Log.d(TAG, "retrofit onFailure");
//                    }
//                    dbh.insertIntoTableSensorData(stringRep);
//                }
//
//            });
//        } else {
//            Log.d(TAG, "Write To DB");
//            dbh.insertIntoTableSensorData(stringRep);
//        }


    }

    public void startPreferenceIntent() {
        Intent intent = new Intent(this, SettingsActivityV2.class);
        intent.putExtra("sensors", getListOfSensorNames());
        startActivity(intent);

    }

    public void pauseAllSensors() {
        Log.d(TAG, "pauseAllSensors");
        if (mGoogleApiClient != null) {
            if(mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }


    }

    public void resumeAllSensors() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                if(mRequestingLocationUpdates) {
                    startLocationUpdates();
                }
            }
        }
        checkPreferencesForEnabledSensors();
    }

    public void checkPreferencesForEnabledSensors() {
        String sensorString = getListOfSensorNames();
        String[] sensors = sensorString.split(",");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
//        if (startUp == true) {
//            SensorDataModule sdm = new SensorDataModule();
//            sdm.UserGuid = UserInfo.userGuid;
//            sdm.OS = String.valueOf(Build.VERSION.SDK_INT);
//            sdm.Device = Build.MANUFACTURER + " " + Build.MODEL;
//            sdm.UserSentTime = String.valueOf(System.nanoTime());
//            sdm.Guid = currentGuid;
//            dbh.insertIntoTableSensorData(sdm);
//
//            startUp = false;
//        }

        int numSensorsEnabled = 0;
        for (int i = 0; i < sensors.length; i++) {
            boolean sensorIsEnabled = sharedPreferences.getBoolean(sensors[i], false);
            if (sensorIsEnabled) {
                numSensorsEnabled++;
                int sensorType = getSensorTypeV2(sensors[i]);
                if (sensorType != -1) {
                    Sensor sensor = sensorManager.getDefaultSensor(sensorType);

                    sensorList.add(sensor);
                    sensorManager.registerListener(this, sensor, SENSOR_FREQUENCY, uploadHandler); // 1000000 for accelerometer makes it go quite slow actually
                }
            }
        }
//        Log.d(TAG, "SENSORS ENABLED: " + numSensorsEnabled);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_main_progressbar);
        if (numSensorsEnabled == 0) {
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (hadToLogin) {
            uploadSensorNames();
        }

    }

    public int getSensorTypeV2(String preferenceKey) {
        int ret = -1;
        String trim = preferenceKey.trim();
        List<Sensor> list = getListOfSensors();
        int size = list.size();
        for(int i = 0; i < size; i++) {
            String sensName = list.get(i).getName();
            if (sensName.equals(trim)) {
                ret = list.get(i).getType();
            }
        }

        return ret;

    }

    private void logout() {
        UserInfo.removeUserGuid(this);
        pauseAllSensors();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }


    public void timeDelay(long val) {
        final long delay = val * 1000;
        time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                sendToServer();
            }
        }, 0, delay);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner) {
            String value = (String) parent.getItemAtPosition(position);
            if (value.equals("Other")) {
                et.setVisibility(View.VISIBLE);
            } else if (et.getVisibility() == View.VISIBLE){
                et.setText("", null);
                et.setVisibility(View.GONE);
            }
            checkForActivity(value);
        } else {
            // app spinner
            if (appModules != null) {
                if (parent.getId() == R.id.spinner_app) {
                    if (appModules.size() > 0) {
                        Log.d(TAG, "App: " + appModules.get(position).Name + ", GUID: " + appModules.get(position).Guid);
                        AppInfo.selectedAppGuid = appModules.get(position).Guid;
                    }
                }

            }

        }

//        Log.d(TAG, "New activity : " + checkForActivity(value));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "NOTHING SELECTED");
    }

    public boolean checkForActivity(String value) {
        boolean ret = false;
        if (!value.equals(NOTHING)) {
            ret = true;
            thisActivity = value;
        } else {
            thisActivity = NOTHING;
        }
        return ret;
    }

    private void start(Intent intent_pebble) {

        Log.d(TAG, "Starting Pebble...");

        //*********************//
        //Pebble Accelerometer//
        //********************//
        Log.d(TAG, "Start pebble service");
        startService(intent_pebble);
        mBound = true;

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(PebbleService.STATUS_REQ)
        );
    }

    public void stop(Intent intent) {
        if (mBound) {
            PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
            stopService(intent);
            mBound = false;
        }
    }

    public static JSONObject createJsonPebble(JSONObject jsonObject) {
        try {
            jsonObject.put("UserGuid", UserInfo.userGuid);
            jsonObject.put("Activity", thisActivity);
            jsonObject.put("Time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
//            if (!jsonObject.has("OS")) {
//                jsonObject.put("OS", String.valueOf(Build.VERSION.SDK_INT));
//            }
//            if (!jsonObject.has("Device")) {
//                jsonObject.put("Device", Build.MANUFACTURER + "-" + Build.MODEL);
//            }
            jsonObject.put("AppGuid", AppInfo.selectedAppGuid);
            if (mLastReceivedLocation != null) {
                jsonObject.put("Lat", mLastReceivedLocation.getLatitude());
                jsonObject.put("Lon", mLastReceivedLocation.getLongitude());
                jsonObject.put("GPS_Accuracy", mLastReceivedLocation.getAccuracy());
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return jsonObject;
    }

//    public void uploadSavedData() {
//        ArrayList<String> savedJSONStrings = dbh.getListOfJSONObjectStrings();
//        if (savedJSONStrings != null) {
//            Toast.makeText(this, "Uploading data", Toast.LENGTH_LONG).show();
//            int size = savedJSONStrings.size();
//            for (int i = 0; i < size; i++) {
////                Log.d(TAG, savedJSONStrings.get(i));
//                Call<BasicServerResponse> bsr = RestClient.get().pushSensorDataFlatLocal(collectionName, savedJSONStrings.get(i));
//                try {
//                    bsr.enqueue(new Callback<BasicServerResponse>() {
//                        @Override
//                        public void onResponse(Call<BasicServerResponse> call, Response<BasicServerResponse> response) {
//
//                        }
//
//                        @Override
//                        public void onFailure(Call<BasicServerResponse> call, Throwable t) {
//                            if (t != null) {
//                                Log.d(TAG, t.getLocalizedMessage());
//                            }
//                        }
//
//                    });
//                } catch (Exception e) {
//                    Log.d(TAG, e.getLocalizedMessage());
//                }
//
////                Observable<BasicServerResponse> bsr = RestClient.get().pushSensorDataFlatServerObservable("accData", stringRep);
////                bsr.subscribeOn(Schedulers.newThread())
////                        .observeOn(AndroidSchedulers.mainThread())
////                        .subscribe(new Subscriber<BasicServerResponse>() {
////                            @Override
////                            public void onCompleted() {
////                                this.unsubscribe();
////                            }
////
////                            @Override
////                            public void onError(Throwable e) {
////                                Log.d(TAG, e.getLocalizedMessage());
////                            }
////
////                            @Override
////                            public void onNext(BasicServerResponse basicServerResponse) {
//////                                if (basicServerResponse.Status.equals("200")) {
//////                                    Log.d(TAG, "Gewd");
//////
//////                                }
////                            }
////                        });
//            }
//            Toast.makeText(this, "Uploading complete", Toast.LENGTH_LONG).show();
//            dbh.update();
//        }
//
//
//
//    }

    public void uploadSavedDataWithCursor() {
        final ArrayList<String> smallBatch = new ArrayList<>();
        Cursor savedPoints = dbh.queryGetAllSavedPoints();
        int maximum = 5000;
        int runningTotal = 0;
        if (savedPoints != null ) {
            if (savedPoints.moveToFirst()) {
                Toast.makeText(this, "Uploading data", Toast.LENGTH_LONG).show();
                do {
                    String sub;
                    sub = savedPoints.getString(savedPoints.getColumnIndex("string_value"));
                    if (sub != null) {
                        if (runningTotal < maximum) {
                            smallBatch.add(sub);
                            runningTotal += 1;
                        } else {
                            // clear runningTotal, smallBatch and upload
                            runningTotal = 0;
                            // copy smallBatch, set smallBatch to 0, then send the copy to an upload method
                            ArrayList<String> copyBatch = new ArrayList<>();
                            int size = smallBatch.size();
                            for (int i = 0; i < size; i++) {
                                copyBatch.add(i, smallBatch.get(i));
                            }
//                            send(copyBatch);
                            Log.d(TAG, "RUNNING TOTAL: " + runningTotal);
//                            writeToFile(copyBatch);
                            smallBatch.clear();
                        }
                    }
//                    smallBatch.clear();
                } while(savedPoints.moveToNext());
                if (smallBatch.size() > 0) {
                    Log.d(TAG, "Second area");
                    ArrayList<String> copyBatch = new ArrayList<>();
                    int size = smallBatch.size();
                    for (int i = 0; i < size; i++) {
                        copyBatch.add(i, smallBatch.get(i));
                    }
//                    send(copyBatch);
                    Log.d(TAG, "RUNNING TOTAL: " + runningTotal);
//                    writeToFile(copyBatch);
                }
            } else {
                Log.d(TAG, "Cannot move to first");
            }


        } else {
            Log.d(TAG, "savedPoints is null");
        }
        Log.d(TAG, "end of statement");
//        dbh.update();
//        sendEmail(currentFile);
    }

//    public void writeToFile( ArrayList<String> batch) {
//        final File path = getGeocreepDir();
//        currentFile = path;
//        final File file = new File(path, fileName + ".txt");
//
//        try {
//            file.createNewFile();
//            FileOutputStream outputStream = new FileOutputStream(file);
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
//            for(String jsonObj : batch) {
//                outputStreamWriter.append(jsonObj);
//            }
//            outputStreamWriter.close();
//
//            outputStream.flush();
//            outputStream.close();
//        } catch (IOException e) {
//            Log.d(TAG, e.getMessage());
//        }
//
////        sendEmail(file);
//
//    }

    public File getGeocreepDir() {
        if (GeoInnovationUtils.checkPermission(this, GeoInnovationPermissions.WRITE_EXTERNAL_STORAGE)) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Geocreep");
            if (!file.mkdirs()) {
                Log.d(TAG, "Directory not created");
            }

            return file;
        } else {
            return null;
        }

    }

    public void sendEmail(File file) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"aplecore@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geocreep email");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Sup dawg");

//        File root = Environment.getExternalStorageDirectory();
//        String pathToMyAttachedFile = "temp/geocreep.csv";
//        File file = new File(root, pathToMyAttachedFile);
        if (!file.exists() || !file.canRead()) {
            return;
        }
        Uri uri = Uri.fromFile(file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }



    public void send(ArrayList<String> smallBatch) {
        for (int i = 0; i < smallBatch.size(); i++) {
            boolean finishedUploading = false;

            Call<BasicServerResponse> bsr = RestClient.get().pushSensorDataFlatServer(collectionName, smallBatch.get(i));
            try {
                Log.d(TAG, "sending");
                bsr.enqueue(new Callback<BasicServerResponse>() {

                    @Override
                    public void onResponse(Call<BasicServerResponse> call, Response<BasicServerResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<BasicServerResponse> call, Throwable t) {
                        if (t != null) {
                            Log.d(TAG, t.getLocalizedMessage());
                        }
                    }

                });
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
        if(smallBatch != null) {

        }
    }

    public void uploadSensorNames() {
        JSONObject obj = new JSONObject();
        if (sensorManager != null) {
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sens : sensors) {
//                String sensorNameTrimmed = sens.getName().replace(" ", "");
                String trimName = sens.getStringType().replace(".", "_");
                if (trimName != null && trimName.length() > 0) {
//                    Log.d(TAG, trimName);
                    try {
                        obj.put(trimName, "0");
                    } catch (JSONException e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                }
            }
            try {
                obj.put("SensorList","1");
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
//            Log.d(TAG, obj.toString());
            Call<BasicServerResponse> send = RestClient.get().pushSensorDataFlatServer("sensordata", obj.toString());
            send.enqueue(new Callback<BasicServerResponse>() {

                @Override
                public void onResponse(Call<BasicServerResponse> call, Response<BasicServerResponse> response) {

                }

                @Override
                public void onFailure(Call<BasicServerResponse> call, Throwable t) {
                    if (t.getMessage() != null) {
                        Log.d(TAG, t.getMessage());
                    }
                }

            });
        }
    }

    public static String getDateTimeFromUnix(long epoch) {
        Date date = new Date(epoch); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // the format of your date
//        Log.d(TAG, sdf.format(date));
        return sdf.format(date);
    }


    @Override
    protected void onStop() {
        super.onStop();


    }



}

