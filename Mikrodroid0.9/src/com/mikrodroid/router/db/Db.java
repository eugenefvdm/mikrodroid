package com.mikrodroid.router.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author eugene
 * 
 * Superclass that all databases inherit from
 *
 */
public class Db {  

	private static final String TAG = "Db";	
    
    private static final String DATABASE_NAME = "devices";
    private static final int DATABASE_VERSION = 5;
    
    protected static final String TABLE_DEVICES = "devices";
    
    public static final String KEY_DEVICES_DEVICE_ID = "_id";
    public static final String KEY_DEVICES_NAME = "name";
    public static final String KEY_DEVICES_IP_ADDRESS = "ip_address";
    public static final String KEY_DEVICES_IP_PORT = "ip_port";    
    public static final String KEY_DEVICES_TYPE = "type";
    public static final String KEY_DEVICES_USE_GLOBAL_LOGIN = "use_global_login";
    public static final String KEY_DEVICES_USERNAME = "username";
    public static final String KEY_DEVICES_PASSWORD = "password";
    public static final String KEY_DEVICES_STATUS = "status";
    public static final String KEY_DEVICES_PING_RESPONSE = "ping_response";    

    // When you add a new database column below you have to up the DATABASE_VERSION to a new number so that the new schema is created
    private static final String CREATE_TABLE_DEVICES =
        "CREATE TABLE " + TABLE_DEVICES + " (" 
        + KEY_DEVICES_DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + KEY_DEVICES_NAME + " TEXT, "
        + KEY_DEVICES_IP_ADDRESS + " TEXT NOT NULL, "                
        + KEY_DEVICES_TYPE + " TEXT, "
        + KEY_DEVICES_USE_GLOBAL_LOGIN + " INTEGER, "
        + KEY_DEVICES_USERNAME + " TEXT, "
        + KEY_DEVICES_PASSWORD + " TEXT, "
        + KEY_DEVICES_STATUS + " TEXT, "
        + KEY_DEVICES_PING_RESPONSE + " TEXT);";    
    /**
     * Database super class from which all database inherit
     * @param ctx
     */
    public Db(Context ctx) {
    	super();
	}
        		
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) throws SQLException {
        	db.execSQL(CREATE_TABLE_DEVICES);	        	
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);            
            onCreate(db);
        }
    }
   
}