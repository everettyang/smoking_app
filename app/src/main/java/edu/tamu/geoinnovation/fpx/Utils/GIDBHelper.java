package edu.tamu.geoinnovation.fpx.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import edu.tamu.geoinnovation.fpx.Modules.SensorDataModule;
import edu.tamu.geoinnovation.fpx.Modules.SensorModule;


/**
 * Created by A.H. on 10/8/2015.
 */
public class GIDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "GIDBHelper";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "geocreep.db";
    public Context mContext;

    // TABLE NAMES
    public static final String TABLE_SENSOR_DATA = "sensor_data";

    // TABLE_SENSOR_DATA COLUMN NAMES
    public static final String KEY_STRING_VALUE = "string_value";


    public SQLiteDatabase db = null;

    public GIDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        db = this.getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_SENSOR_DATA_TABLE = "CREATE TABLE " + TABLE_SENSOR_DATA + " ("
                + KEY_STRING_VALUE + " TEXT "
                + ")";

        db.execSQL(CREATE_SENSOR_DATA_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_DATA);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void update() {
        this.onUpgrade(db, 1, 2);
    }

    public boolean checkIfDatabaseExists() {
        File databaseFile = new File(mContext.getFilesDir().getParent() + "/databases/" + DATABASE_NAME);
        return databaseFile.exists();
    }

    public void insertIntoTableSensorData(String obj) {
        try {
            ContentValues first = new ContentValues();
            first.put(KEY_STRING_VALUE, obj);
            db.insert(TABLE_SENSOR_DATA, null, first);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getListOfJSONObjectStrings() {

        ArrayList<String> sensorData = new ArrayList<>();

        String getAllSubmissions =  "SELECT * " + " FROM " + TABLE_SENSOR_DATA;

        Cursor result = null;
        try {
            result = db.rawQuery(getAllSubmissions, new String[]{});

            if(result.moveToFirst()) {
                do {
                    String sub = new String();
                    sub = result.getString(result.getColumnIndex(KEY_STRING_VALUE));
                    sensorData.add(sub);
                } while(result.moveToNext());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public Cursor queryGetAllSavedPoints() {
        String getAllSubmissions =  "SELECT * " + " FROM " + TABLE_SENSOR_DATA;
        Cursor result = null;
        try {
            result = db.rawQuery(getAllSubmissions, new String[]{});
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return result;
    }

    public boolean clearRecordFromTable(String tableName, String stringy) {
        return db.delete(TABLE_SENSOR_DATA, KEY_STRING_VALUE + "=" + stringy, null) > 0;

    }

}
