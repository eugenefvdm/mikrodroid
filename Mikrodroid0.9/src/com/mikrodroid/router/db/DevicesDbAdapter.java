package com.mikrodroid.router.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Devices database adapter
 * 
 * The definition of a device is anything on a network, e.g. a MikroTik router, a Cisco router, a random host, etc. 
 * 
 * @author eugene
 *
 */
public class DevicesDbAdapter extends Db {  

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
       
    private final Context mCtx;
    
    public DevicesDbAdapter(Context ctx) {
    	super(ctx);
    	this.mCtx = ctx;
	}
    
    public DevicesDbAdapter open() throws SQLException {    	
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long addDevice(String name, String ipAddress, String type, String username, String password) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(Db.KEY_DEVICES_NAME, name);
        initialValues.put(Db.KEY_DEVICES_IP_ADDRESS, ipAddress);        
        initialValues.put(Db.KEY_DEVICES_TYPE, type);
        initialValues.put(Db.KEY_DEVICES_USERNAME, username);
        initialValues.put(Db.KEY_DEVICES_PASSWORD, password);
        return mDb.insert(Db.TABLE_DEVICES, null, initialValues);
    }
   
    public boolean deleteDevice(long rowId) {
        return mDb.delete(Db.TABLE_DEVICES, Db.KEY_DEVICES_DEVICE_ID + "=" + rowId, null) > 0;
    }
    
    /**
     * Fetch all devices
     * TODO Figure out why type is not here (is it never used but assumed?)
     * 
     * @return
     */
    public Cursor fetchAllDevices() {
        return mDb.query(Db.TABLE_DEVICES, new String[] {
        		Db.KEY_DEVICES_DEVICE_ID,
        		Db.KEY_DEVICES_NAME,
        		Db.KEY_DEVICES_IP_ADDRESS,
        		Db.KEY_DEVICES_USERNAME,
        		Db.KEY_DEVICES_PASSWORD,
        		Db.KEY_DEVICES_STATUS,
                }, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching database row item, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchDevice(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, Db.TABLE_DEVICES, new String[] {
        	Db.KEY_DEVICES_DEVICE_ID,
        	Db.KEY_DEVICES_NAME,
        	Db.KEY_DEVICES_IP_ADDRESS,            
            Db.KEY_DEVICES_STATUS,
            Db.KEY_DEVICES_TYPE,
            Db.KEY_DEVICES_USERNAME,
            Db.KEY_DEVICES_PASSWORD
        }, Db.KEY_DEVICES_DEVICE_ID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
   
    /**
     * Update device table with new device information
     * 
     * Optional method below updates with only limited information needs to be updated, e.g. a status update
     * @param id
     * @param ipAddress
     * @param name
     * @param type
     * @param username
     * @param password
     * @param status
     * @return
     */
    public boolean updateDevice(long id, String ipAddress, String name, String type, String username, String password, String status) {
        ContentValues args = new ContentValues();
        args.put(Db.KEY_DEVICES_NAME, name);
        args.put(Db.KEY_DEVICES_IP_ADDRESS, ipAddress);                        
        args.put(Db.KEY_DEVICES_TYPE, type);
        args.put(Db.KEY_DEVICES_USERNAME, username);
        args.put(Db.KEY_DEVICES_PASSWORD, password);
        args.put(Db.KEY_DEVICES_STATUS, status);
        return mDb.update(Db.TABLE_DEVICES, args, Db.KEY_DEVICES_DEVICE_ID + "=" + id, null) > 0;
    }
    
    /**
     * Update the device with minimal arguments when changing name or status
     * 
     * @param id
     * @param ipAddress
     * @param name
     * @param status
     * @return
     */
    public boolean updateDevice(long id, String ipAddress, String name, String status) {
        ContentValues args = new ContentValues();
        args.put(Db.KEY_DEVICES_NAME, name);
        args.put(Db.KEY_DEVICES_IP_ADDRESS, ipAddress);                
        args.put(Db.KEY_DEVICES_STATUS, status);        
        return mDb.update(Db.TABLE_DEVICES, args, Db.KEY_DEVICES_DEVICE_ID + "=" + id, null) > 0;
    }
    
    public String getDeviceIpAddress(long id) {
    	Cursor device = this.fetchDevice(id);
    	return device.getString(
    			device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS)
		);        
    }
    
    public boolean updateDeviceStatus(long rowId, String status) {
    	ContentValues args = new ContentValues();
    	args.put(Db.KEY_DEVICES_STATUS, status);
    	return mDb.update(Db.TABLE_DEVICES, args, Db.KEY_DEVICES_DEVICE_ID + "=" +rowId, null) > 0;
    }
}

   