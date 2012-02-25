/**
 * 
 * OutputPreferences.java
 * 
 * Display MikroTik configuration parameters as Android Preferences
 * 
 *  TODO OnResume drops the listener. Figure out how to add it. (StackOverflow question would work)
 *  TODO Move the API code into it's own class instead of extending this class. Then calls to onResume and onPause will work better 
 * 
 */
package com.mikrodroid.router;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mikrodroid.router.api.MikrotikApi;

/**
 * 
 * This was an experiment to see if I can split the processAsync task into a seperate class but it failed.
 * This file can be deleted. 
 * 
 * Asynchronous class that queries the MikroTik API device and displays it's configuration information
 * 
 * 
 *
 */
// public class OutputPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
public class CallApi {
	
	private static final String TAG = "OutputPreferences";
		
	private AsyncReceiver task = null;
	
	// TODO Figure out how to use getListView without a variable
	private ListView mItemListView = null;
		
	private ArrayList<ConfigItem> mItemValueList;
	
	/**
	 * Convenience field that is assigned to current menu
	 */
	private MenuObject mCurrentMenu;

	public ArrayList<ConfigItem> apiResult = null;
	
	
	CallApi(String ipAddress, String id) {
				
		String menuCommand;
				
		mCurrentMenu = Main.currentMenu;
		// setTitle(ipAddress + mCurrentMenu.getBreadCrumb(mCurrentMenu));	
								
		// mItemListView = getListView(); 
		
		// TODO Move this code to it's own class. Execute calling the menu and then output results by way of bundle or something
		task = new AsyncReceiver(Main.apiConn);
		task.execute();
		
		menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu);		
		menuCommand = menuCommand + "/print";		
		menuCommand = menuCommand + "\n?.id=" + id;				
		menuCommand = menuCommand + "\n.tag=1";		
		Log.v(TAG, menuCommand);		
		Main.apiConn.sendCommand(menuCommand);
	
	}

	private class AsyncReceiver extends AsyncTask<Void, Void, String> {

		private String currentResult = "";		
		private String allResults = "";
		private MikrotikApi apiConn; 
		
		AsyncReceiver(MikrotikApi apiConn) {			
			this.apiConn = apiConn;
		}
		
		@Override
		protected String doInBackground(Void... unused) {
			
			while (true) {
				try {
					currentResult = apiConn.getData();
					if (currentResult != null) {
						Log.v(TAG, currentResult);					
						allResults = allResults + currentResult;						
						if (currentResult.contains("!done")) {
							return allResults;							
						}
					}
				} catch (InterruptedException e) {
					Log.e(TAG + " exception in AsyncReceiver doInBackground()", e.getMessage());
				}
			}
		}
		
		protected void onPostExecute(String result) {
			processAsyncTask(result);
		}
		
	}
	
	public void processAsyncTask(String result) {
		
		ConfigList list = getConfigList(result);
		ConfigCollection singleItem = list.getItem(0);
		mItemValueList = singleItem.getAllItems();
		
		this.apiResult = mItemValueList;
		
		// return (mItemValueList);
			
	}
	
	public ConfigList getConfigList(String data) {

		ConfigList list = new ConfigList();
		ConfigCollection collection = null;		
		ConfigItem item = null;
		
		String paramName, paramValue;
		int start_of_value;

		String lines[] = data.split("\\n");		
		for (String line : lines) {
			
			if (line.equals("!trap")) {				
				// Do something errors for example invalid commands
			}			
			
			// Start of result
			if (line.equals("!re") || line.equals("!trap")) {				
				collection = new ConfigCollection();
			}
			
			// If there are two or more = signs then this line should contain a parameter
			if (StringUtils.countMatches(line, "=") >= 2 && line.charAt(0) == '=') {
				start_of_value = line.indexOf('=', 1);
				paramName = line.substring(1, start_of_value);
				paramValue = line.substring(start_of_value + 1, line.length());
				item = new ConfigItem();				
				item.setName(paramName);
				item.setValue(paramValue);
				collection.addItem(item);
				item = null;
			}
			
			// End of result for this round, time to start another collection 
			if (line.contains(".tag=") == true) {
				list.addItem(collection);
				collection = null;
			}
			
			// End of configuration request
			if (line.equals("!done") == true) {
				return list;
			}			
			
		}
		return null;
	}
	
}