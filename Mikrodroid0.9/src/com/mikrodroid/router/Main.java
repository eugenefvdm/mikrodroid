/** 
 * 
 * Entry point for the Mikrodroid application
 * 
 */

package com.mikrodroid.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.mikrodroid.router.api.*;
import com.mikrodroid.router.db.DevicesDbAdapter;
import com.mikrodroid.router.ui.Navigation;
//import com.mikrodroid.router.ui.NavigationRoot;

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
 * 
 * Main extends ListActivity and shows a list of devices
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
	public static ArrayList<MenuObject> rootLevelNav;
	
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
	
	private String mCommandFileName;
	
	private boolean mMenuBootFileExists;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new DevicesDbAdapter(this);
		mDbHelper.open();
		setContentView(R.layout.main);
		fillData();				
		registerForContextMenu(getListView());
		
		mCommandFileName = "commands.rsc";
		mMenuBootFileExists = false;
		
		if (MikrotikApi.checkBootMenuExists(mCommandFileName) == true) {
			Log.d(TAG, "MikroTik boot menu exists");
			mMenuBootFileExists = true;			
			// Assign MikroTik menu system to global navigation variables upon initialisation of app
			MikrotikCommandLoad commands = new MikrotikCommandLoad();
			commands.importCommands("commands.rsc");
			Main.menuList = commands.getMenus();
			// TODO Fix Type safety: Unchecked invocation sort(MenuList, NameComparator) of the generic method sort(List<T>, Comparator<? super T>) of type Collections
			Collections.sort(menuList, new NameComparator());
			rootLevelNav = Main.menuList.getRootMenus();
		} else {
			Log.w(TAG, "MikroTik boot menu does not exist");			
		}
		
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String ipAddress = prefs.getString("setting_bootstrap_ip", "");
			String username = prefs.getString("setting_bootstrap_username", "");
        	String password = prefs.getString("setting_bootstrap_password", "");        
			if (MikrotikApi.getExportFile(mCommandFileName, ipAddress, username, password) == true) {
				Toast.makeText(this, "Successfully retrieved menus", Toast.LENGTH_LONG).show();
				mMenuBootFileExists = true;
			} else {
				Toast.makeText(this, "Failed to retrieve menus", Toast.LENGTH_LONG).show();
			}
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
			if (mMenuBootFileExists == true) {
				loginToDevice(id);
				return true;	
			} else {
				Log.d(TAG, "MikroTik boot menu file does not exist");
				Toast.makeText(this, getString(R.string.error_boot_menu_first), Toast.LENGTH_LONG).show();
				return true;
			}			
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
		Log.d(TAG, "loginToDevice with id #" + id);
		
		String ipAddress;
		String status;
		String deviceType;
		int useGlobalLogin;
		String username;
		String password;		
				
		int apiPort;

		Cursor device = mDbHelper.fetchDevice(id);
        startManagingCursor(device);
        
        mDeviceName = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_NAME));
        ipAddress = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS));        
        status = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_STATUS));		
        // Assign device type. This will determine if we can log in or now
        deviceType = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_TYPE));
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the API port and parse it as an integer
        apiPort = Integer.parseInt(prefs.getString("setting_api_port", "8728"));
        
        useGlobalLogin = device.getInt(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_USE_GLOBAL_LOGIN));        
        if (useGlobalLogin == 1) {        
        	username = prefs.getString("pref_global_username", "");
        	password = prefs.getString("pref_global_password", "");        
        } else {
        	username = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_USERNAME));
        	password = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_PASSWORD));
        }
                
        if (deviceType.contains("MikroTik")) {
        	if (loginMtRouter(ipAddress, username, password, apiPort)) {
        		// Upon successful login add the router name and status to the database
    			setupMikrotikRouter(id, mDeviceName, ipAddress, status);    			
    			// Start navigation intent
    			Log.d(TAG, "Starting NavigrationRoot");    			
    			//Intent i = new Intent(this, NavigationRoot.class); // TODO Migrate NavigrationRoot to single file Navigation based on Children
    			Intent i = new Intent(this, Navigation.class);
    			i.putExtra("id", id);
    			i.putExtra("ipAddress", ipAddress);			
    			i.putExtra("name", mDeviceName);
    			i.putExtra("firstLaunch", true);
    			startActivity(i);
    		} else {    			
    			mDbHelper.updateDeviceStatus(id, "down");
    			fillData();
    		}
        } else {
        	// Trying to login to a non-MikroTik device
        	Toast.makeText(this, R.string.error_invalid_login_type, Toast.LENGTH_SHORT).show();        	
        }
	}
	
	/**
	 * Log into a MikroTik router with a username and password
	 * 
	 * TODO Consider not use result variable and just plain fall through
	 * 
	 * @param ipAddress
	 * @param username
	 * @param password
	 * @return
	 */
	private boolean loginMtRouter(String ipAddress, String username, String password, int apiPort) {
		Log.v(TAG, "In loginMtRouter method of " + TAG);
		boolean result = false;
		
		Log.v(TAG, "Checking for blank username");
		if (username.length() ==0) {
			Toast.makeText(this, R.string.error_blank_username, Toast.LENGTH_SHORT).show();
			return result;
		}
		
		String loginResult = null;
		Log.d(TAG, "Creating a new API connection");
		// TODO Assign API constant 8728 to value contained in app settings
		apiConn = new MikrotikApi(ipAddress, apiPort);				
		if (!apiConn.isConnected()) {
			Log.d(TAG, "API isConnected() is now " + apiConn.isConnected());
			apiConn.start();
			try {
				apiConn.join();
				if (apiConn.isConnected()) {
					Log.v(TAG, "Calling the login method of apiConn");
					loginResult = apiConn.login(username, password);
					
					if (loginResult == "Login successful") {						
						result = true;
						Toast.makeText(this, loginResult, Toast.LENGTH_SHORT).show();
						Log.i(TAG, loginResult);
					} else  {						
						Log.e(TAG, "Username: " + username + ": " + loginResult);
						Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();	
						result = false;
					}										
				} else {
					// Possible reasons for being here:
					//  API port no enabled  
					//  API port not correct
					// TODO: Improve error checking to display possible API not enabled / API port issues. Use string comparison for that
					// test by way switching on and off API port
					result = false;
					Log.e(TAG, "Login error " + ipAddress + ": " + apiConn.getMessage());
					Toast.makeText(this, "Login error " + ipAddress + "\n" + apiConn.getMessage(), Toast.LENGTH_LONG).show();
				}
			} catch (InterruptedException e) {
				result = false;
				Log.e(TAG + "isDeviceAccessible Exception", e.getMessage());
			}
		}
		return result;
	}
	
	/* 
	 * 
	 * Trying to get rid of database exception close() was never explicitly called on database
	 * http://stackoverflow.com/questions/4464892/android-error-close-was-never-explicitly-called-on-database
	 * 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}
	
	/**
	 * Add router name and status to database
	 * TODO Check if cursor needs closing
	 * @param id
	 * @param ipAddress
	 * @param deviceName
	 * @param status
	 */
	private void setupMikrotikRouter(long id, String deviceName, String ipAddress, String status) {
		boolean statusChanged = false;
		boolean nameChanged = false;		
		if (deviceName == null || deviceName.length() == 0) {
			Log.w(TAG, "Router name not in database, setting");
			deviceName = apiConn.setRouterName();	
			mDeviceName = deviceName;
			nameChanged = true;
		}
		if (status != "up") {
			status = "up";			
			statusChanged = true;
		}
		if (nameChanged || statusChanged) {
			mDbHelper.updateDevice(id, deviceName, ipAddress, status);
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