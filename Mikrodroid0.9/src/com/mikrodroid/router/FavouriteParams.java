/*
 * Copyright (C) 2011 Snowball 
 */
package com.mikrodroid.router;

import java.util.ArrayList;


/**
 * An ArrayList of favourites where 'contains' is overridden so that we only compare name and not value
 * 
 * This method solely exists to override the contains method
 * 
 * The concept is:
 * Go through the current list. See if this menu object exists.
 * If the menu object exists, go through the list of items for that menu object.
 * Add the new item only if it does not already exist
 */
public class FavouriteParams extends ArrayList<ConfigItem> {	
		
	private static final long serialVersionUID = -1511643337216516819L;
	
	public boolean contains(ConfigItem configItem) {		
		for (ConfigItem c : this) {
        	if (c.name.equals(configItem.name)) {
        		return true;
        	}
        }
		return false;
	}
	
}
