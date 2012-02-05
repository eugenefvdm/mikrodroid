package com.mikrodroid.router;

import java.util.Comparator;

/**
 * Sort menu items alphabetically
 * 
 * @author eugene
 *
 */
@SuppressWarnings("rawtypes")
// TODO Fix Comparator is a raw type. References to generic type Comparator<T> should be parameterized
class NameComparator implements Comparator {
	
	public int compare(Object menu1, Object menu2) {
		
		String menu1Name = ((MenuObject)menu1).getName();
		String menu2Name = ((MenuObject)menu2).getName();
		
		return menu1Name.compareTo(menu2Name);
	}

}
