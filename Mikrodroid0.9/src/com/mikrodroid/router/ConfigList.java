package com.mikrodroid.router;

import java.util.ArrayList;

/**
 * ConfigList of configuration items
 * 
 * A bug in going to /system/identity/print trigger a list error here
 * 
 * @author eugene
 *
 */
public class ConfigList {
	
	ArrayList<ConfigCollection> list = new ArrayList<ConfigCollection>();

	public void addItem(ConfigCollection c) {
		list.add(c);
	}
	
	/**
	 * Get the item at position index
	 * 
	 * @param index
	 * @return
	 */
	public ConfigCollection getItem(int index) {
		return list.get(index);
	}
	
	public ArrayList<ConfigCollection> getAllItems() {		
		return list;
	}
	
}
