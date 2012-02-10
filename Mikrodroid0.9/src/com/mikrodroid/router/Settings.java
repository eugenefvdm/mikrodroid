/**
 * Copyright (C) 2011-2012 Snowball
 * 
 * Using example from API Demos and see here:
 * http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-sum
 * 
 */

package com.mikrodroid.router;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * Display preferences neatly underneath the input boxes.
 * 
 * By default in Android you have to first click, this actually shows you the value underneath. Neat.
 *  
 * @author Eugene
 *
 */
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String SETTING_GLOBAL_USERNAME = "pref_global_username";	
	public static final String SETTING_API_PORT = "pref_api_port";
	 
    private EditTextPreference mSettingGlobalUsername;
    private EditTextPreference mSettingApiPort;
    
	public static final String PREFS_NAME = "MikrodroidPrefsFile";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		 // Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.settings);
	    mSettingGlobalUsername = (EditTextPreference)getPreferenceScreen().findPreference(SETTING_GLOBAL_USERNAME);
	    mSettingApiPort = (EditTextPreference)getPreferenceScreen().findPreference(SETTING_API_PORT);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
		// Setup the initial values
		mSettingGlobalUsername.setSummary(mSettingGlobalUsername.getText());
		mSettingApiPort.setSummary(mSettingApiPort.getText());
		// Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something a preference value changes		        
        if (key.equals(SETTING_GLOBAL_USERNAME)) {
        	mSettingGlobalUsername.setSummary(sharedPreferences.getString(key, "")); 
        }
        if (key.equals(SETTING_API_PORT)) {
        	mSettingApiPort.setSummary(sharedPreferences.getString(key, "")); 
        }
        
    }
	
}