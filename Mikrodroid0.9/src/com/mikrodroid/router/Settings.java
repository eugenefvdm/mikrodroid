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
 * By default in Android you have to first click, this actually shows you the value underneath. Neat.
 *  
 * @author Eugene
 *
 */
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String SETTING_GLOBAL_USERNAME = "pref_global_username";	
	public static final String SETTING_API_PORT = "pref_api_port";
	 
    private EditTextPreference mSetting1;
    private EditTextPreference mSetting2;
    
	public static final String PREFS_NAME = "MikrodroidPrefsFile";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		 // Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.settings);
	    mSetting1 = (EditTextPreference)getPreferenceScreen().findPreference(SETTING_GLOBAL_USERNAME);
	    mSetting2 = (EditTextPreference)getPreferenceScreen().findPreference(SETTING_API_PORT);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
		// Setup the initial values
		mSetting1.setSummary(mSetting1.getText());
		mSetting2.setSummary(mSetting2.getText());
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
        	mSetting1.setSummary(sharedPreferences.getString(key, "")); 
        }
        if (key.equals(SETTING_API_PORT)) {
        	mSetting2.setSummary(sharedPreferences.getString(key, "")); 
        }
        
    }
	
}