/** 
 * 
 * Entry point for application version 0.9.5
 * 
 * by Eugene  
 * 
 */

package com.mikrodroid.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.mikrodroid.router.api.*;
import com.mikrodroid.router.db.DevicesDbAdapter;
import com.mikrodroid.router.ui.NavigationRoot;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Mikrodroid v2 Main
 * 
 * @author Eugene
 *
 */
public class Main extends ListActivity {
	
	private static final String TAG = "Main";

	private static final int MENU_ADD_DEVICE = Menu.FIRST;
	private static final int MENU_BOOTSTRAP_MIKROTIK_MENU = Menu.FIRST + 1;
	private static final int MENU_SETTINGS = Menu.FIRST + 2;
	
	private static final int CTX_MENU_LOGIN = Menu.FIRST + 3;
	private static final int CTX_MENU_EDIT = Menu.FIRST + 4;
	private static final int CTX_MENU_DELETE = Menu.FIRST + 5;
	
	private static final int ACTIVITY_EDIT_DEVICE = 0;
	
	/**
	 * MikroTik API connection static field name
	 */
	public static MikrotikApi apiConn;	
	
	/**
	 * TODO To be documented
	 */
	public static MenuList menuList = new MenuList();
	
	/**
	 * TODO To be documented
	 */
	public static ArrayList<MenuObject> rootMenuList;
	
	/**
	 * TODO To be documented
	 */
	public static MenuObject currentMenu = new MenuObject();
	
	/**
	 * Devices database helper
	 */
	private DevicesDbAdapter mDbHelper;
	
	/**
	 * Field variable assigned in setupRouter used to set the device name which in turn is passed to menu navigation activity
	 */
	private String mDeviceName;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new DevicesDbAdapter(this);
		mDbHelper.open();
		setContentView(R.layout.main);
		fillData();				
		registerForContextMenu(getListView());	
		// Assign MikroTik menu system to global navigation variables upon initialisation of app
		MikrotikCommandSet commands = new MikrotikCommandSet();
		commands.importCommands("commands.rsc");
		Main.menuList = commands.getMenus();
		// TODO Fix Type safety: Unchecked invocation sort(MenuList, NameComparator) of the generic method sort(List<T>, Comparator<? super T>) of type Collections
		Collections.sort(menuList, new NameComparator());
		rootMenuList = Main.menuList.getRootMenus();
	}
	
	private void fillData() {
		Cursor devicesCursor = mDbHelper.fetchAllDevices();
		startManagingCursor(devicesCursor);		
		this.setListAdapter(new DeviceAdapter(this, devicesCursor));		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Log.v(TAG, "Connecting to device id " + id);				

		Cursor device = mDbHelper.fetchDevice(id);
        startManagingCursor(device);
        String ipAddress = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS));
        
        new PingAsync(ipAddress, id).execute();		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ADD_DEVICE, 0, R.string.menu_add_device);
		menu.add(0, MENU_BOOTSTRAP_MIKROTIK_MENU, 0, R.string.menu_bootstrap_routeros);
		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_DEVICE:
			Intent i1 = new Intent(this, EditDevice.class);
			startActivityForResult(i1, ACTIVITY_EDIT_DEVICE);
			return true;	
		case MENU_BOOTSTRAP_MIKROTIK_MENU:
			MikrotikApi.getExportFile("commands.rsc");                
            return true;
		case MENU_SETTINGS:
			Intent i2 = new Intent(Main.this, Settings.class);
    		Main.this.startActivity(i2);                
            return true;    
		}			
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);		
		menu.add(0, CTX_MENU_LOGIN, 0, R.string.menu_ctx_login);
		menu.add(0, CTX_MENU_EDIT, 0, R.string.menu_ctx_edit);
		menu.add(0, CTX_MENU_DELETE, 0, R.string.menu_ctx_delete);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Long id = info.id;
		switch (item.getItemId()) {	
		case CTX_MENU_LOGIN:
			loginToDevice(id);
			return true;
		case CTX_MENU_EDIT:
			Intent i1 = new Intent(this, EditDevice.class);
			i1.putExtra(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID, id);
			startActivityForResult(i1, CTX_MENU_EDIT);
			return true;		
		case CTX_MENU_DELETE:
			mDbHelper.deleteDevice(id);
			fillData();
			return true;		
		}
		return super.onContextItemSelected(item);
	}
	
	private void loginToDevice(long id) {
		Log.v(TAG, "Connecting to device with id " + id);				

		Cursor device = mDbHelper.fetchDevice(id);
        startManagingCursor(device);
        String ipAddress = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS));
        mDeviceName = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_NAME));
        String status = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_STATUS));
		// Assign device type which will be used to determine if we can log in or not
        String deviceType = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_TYPE));
        // Get global username and password
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("pref_global_username", "");
        String password = prefs.getString("pref_global_password", "");
                
        if (deviceType.contains("MikroTik")) {
        	if (loginMtRouter(ipAddress, username, password)) {
        		// Upon successful login add the router name and status to the database
    			setupMikrotikRouter(id, ipAddress, mDeviceName, status);    			
    			// Start navigation intent
    			Log.d(TAG, "Starting NavigrationRoot.class");    			
    			Intent i = new Intent(this, NavigationRoot.class);
    			i.putExtra("id", id);
    			i.putExtra("ipAddress", ipAddress);			
    			i.putExtra("name", mDeviceName);
    			startActivity(i);
    		} else {    			
    			mDbHelper.updateDeviceStatus(id, "down");
    			fillData();
    		}
        } else {
        	Toast.makeText(this, R.string.invalid_login_type, Toast.LENGTH_SHORT).show();        	
        	// Don't do anything, we're trying to access a non-MikroTik device
        }
	}
	
	/**
	 * Log into a MikroTik router with a username and password
	 * 
	 * @param ipAddress
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean loginMtRouter(String ipAddress, String username, String password) {
		Log.v(TAG, "In loginMtRouter method of " + TAG);
		boolean result = false;				
		String loginResult = null;
		Log.d(TAG, "Creating a new API connection");
		// TODO Migrate 8728 to settings
		apiConn = new MikrotikApi(ipAddress, 8728);				
		if (!apiConn.isConnected()) {
			Log.d(TAG, "API isConnected() is now " + apiConn.isConnected());
			apiConn.start();
			try {
				apiConn.join();
				if (apiConn.isConnected()) {
					Log.d(TAG, "Calling the login method of apiConn");
					loginResult = apiConn.login(username, password);
					
					if (loginResult == "Login successful") {						
						result = true;
						Toast.makeText(this, loginResult, Toast.LENGTH_SHORT).show();
						Log.i(TAG, loginResult);
					} else  {						
						Log.e(TAG, "Username: " + username + ": " + loginResult);
						Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
						result = false;
					}					
					
				} else {
					result = false;
					Log.i(TAG, "Login error " + ipAddress + ": " + apiConn.getMessage());
					Toast.makeText(this, "Login error " + ipAddress + "\n" + apiConn.getMessage(), Toast.LENGTH_LONG).show();
				}
			} catch (InterruptedException e) {
				result = false;
				Log.e(TAG + "isDeviceAccessible Exception", e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * Add router name and status to database
	 * TODO Check if cursor needs closing
	 * @param id
	 * @param ipAddress
	 * @param deviceName
	 * @param status
	 */
	private void setupMikrotikRouter(long id, String ipAddress, String deviceName, String status) {
		boolean statusChanged = false;
		boolean nameChanged = false;		
		if (deviceName == null || deviceName.length() == 0) {
			Log.v(TAG, "Router name not in database, setting");
			deviceName = apiConn.setRouterName();	
			mDeviceName = deviceName;
			nameChanged = true;
		}
		if (status != "up") {
			status = "up";			
			statusChanged = true;
		}
		if (nameChanged || statusChanged) {
			mDbHelper.updateDevice(id, ipAddress, deviceName, status);
		}		
	}
	
	private class PingAsync extends AsyncTask<Void, Void, String> {

		private String ipAddress; 
		long id;
		
		PingAsync(String host, long id) {			
			this.ipAddress = host;
			this.id = id;
		}
		
		@Override
		protected String doInBackground(Void... unused) {
			String result = null;
			try {
				result = Ping.execPing(ipAddress);				
			} catch (IOException e) {				
				e.printStackTrace();
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			return result;
		}
		
		protected void onPostExecute(String result) {			
			if (result != null) {								
				mDbHelper.updateDeviceStatus(id, "up");
				fillData();
				Toast.makeText(Main.this, ipAddress + "\n" + result + " ms", Toast.LENGTH_SHORT).show();
			} else {
				mDbHelper.updateDeviceStatus(id, "down");
				Toast.makeText(Main.this, ipAddress + "\n" + "Ping " + Ping.pingError, Toast.LENGTH_LONG).show();
				fillData();
			}
		}
		
	}
	
	private class DeviceAdapter extends CursorAdapter {
    	
    	private final LayoutInflater mInflater;
    	
    	public DeviceAdapter(Context context, Cursor cursor) {
    		super(context, cursor, true);
    		mInflater = LayoutInflater.from(context);
    	}
    	
    	@Override
    	public void bindView(View view, Context context, Cursor cursor) {
    		TextView t1 = (TextView) view.findViewById(R.id.device);
    		String ipAddress = cursor.getString(cursor.getColumnIndex("ip_address"));    			
    		String deviceName = cursor.getString(cursor.getColumnIndex("name")); 
    		if (deviceName != null && deviceName.length() != 0) {
    			t1.setText(deviceName);
    		} else {
    			t1.setText(ipAddress);	
    		}
    		
    		ImageView image = (ImageView) view.findViewById(R.id.status);
			String status = cursor.getString(cursor.getColumnIndex("status"));
			
			if (status == null) {
				image.setImageResource(R.drawable.grey_dot_nuvola);
			} else if (status.equals("up")) {
				image.setImageResource(R.drawable.green_dot_nuvola);				
			} else {
				image.setImageResource(R.drawable.red_dot_nuvola);
			}
    		
    	}
    	
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.device_row, parent, false);
		      return view;
		}
    	
    }
	
}