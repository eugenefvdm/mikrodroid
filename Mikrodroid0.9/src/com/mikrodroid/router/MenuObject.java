/**
 * Menu hierarchy
 * 
 * Method inspired and provided by Jeremy Druker
 * 
 * Child->Parent->Parent->RootNode
 * 
 * Each child can easily reference their parent so that they can get information about the parent.
 * The parents at the root node references null.
 * The menu consist of a list of lists.
 * The net result is we work from the bottom up
 * 
 */

package com.mikrodroid.router;

import java.util.ArrayList;

import android.util.Log;

/**
 * A menu object
 * 
 * Contains a list of favourite configuration parameters (item / value pairs)
 * 
 * @author eugene
 *
 */
public class MenuObject {
	
	private static final String TAG = "MenuObject.java";
	
	String name;
	private String friendlyName = null;
	MenuObject parent;
	
	String path;
	
	String fullPath;
		
	public boolean isPrintable;
	boolean isFinalNode;	
	boolean isMultiLine;
	
	/**
	 * Comma delimited list of interesting menu items used when outputting print lists
	 */
	String filter;
		
	private ArrayList<MenuObject> childMenus = new ArrayList<MenuObject>();
	
	boolean isFavouriteMenu;
	
	public FavouriteParams favouriteParamsList = new FavouriteParams();
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;					
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getPathWithoutName(String menuPath) {
		int lastSpace = menuPath.lastIndexOf(" ");
		return menuPath.substring(0, lastSpace);
	}
	
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	
	public String getFullPath() {
		return this.fullPath;
	}
	
	/**
	 * Out full menu command in terminal format, e.g. /IP Firewall NAT
	 * 
	 * We use the friendly name not the lower case name 
	 * 
	 * @param menu
	 * @return
	 */
	public String getFullPath(MenuObject menu) {		
		String s = " " + menu.getFriendlyName();
		MenuObject parentMenu = menu.getParent();
		while (parentMenu != null) {
			s = " " + parentMenu.getFriendlyName() + s;
			parentMenu = parentMenu.getParent();			
		}
		return "/" + s.substring(1, s.length());
	}
	
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;		
	}
	
	public String getFriendlyName() {
		if (this.friendlyName != null) {
			return this.friendlyName;
		} else if (this.name != "PRINT") {
			String firstLetter = this.name.substring(0,1);
			String remainder   = this.name.substring(1);
	        this.friendlyName = firstLetter.toUpperCase() + remainder.toLowerCase();
			return this.friendlyName;	
		} else {
			return this.name;
		}
	}
	
	public void setParent(MenuObject parent) {
		this.parent = parent;		
	}	
	
	public MenuObject getParent() {
		return this.parent;		
	}
	
	public void setPrintable(boolean isPrintable) {
		this.isPrintable = isPrintable;		
	}	
		
	public boolean getPrintable() {
		return this.isPrintable;		
	}
	
	public void setFinalNode(boolean isFinalNode) {
		this.isFinalNode = isFinalNode;		
	}	
	
	public boolean getFinalNode() {
		return this.isFinalNode;		
	}
	
	public void setMultiLine(boolean isMultiLine) {
		this.isMultiLine = isMultiLine;		
	}
	
	public boolean getMultiLine() {
		return this.isMultiLine;		
	}
	
	public void setProplist(String filter) {
		this.filter = filter;		
	}
	
	public String getPropList() {
		return this.filter;		
	}
	
	/**
	 * Build a command hierarchy, e.g. /ip/firewall/nat/
	 * @param menu
	 * @return
	 */
	public String getCommandHierarchy(MenuObject menu) {
		String s = "/" + menu.name;
		MenuObject parentMenu = menu.getParent();
		while (parentMenu != null) {
			s = "/" + parentMenu.getName() + s;
			parentMenu = parentMenu.getParent();			
		}
		return s.toLowerCase();
	}
	
	/**
	 * Traverse the menu tree upwards to figure out the bread crumb for this menu  
	 * 
	 * @param menu
	 * @return String in this format IP->Firewall->NAT 
	 */
	public String getBreadCrumb(MenuObject menu) {
		String s = "->" + menu.getFriendlyName();
		MenuObject parentMenu = menu.getParent();
		while (parentMenu != null) {
			s = "->" + parentMenu.getFriendlyName() + s;
			parentMenu = parentMenu.getParent();			
		}
		return s;
	}
	
	/**
	 * Adds a PRINT item at index position 0 but only if there is not one already
	 * 
	 * Accommodate incomplete menus by also checking is the list has zero elements (should never be but during development we had such a case /system/routerboard/print)
	 * 
	 * This seems to be legacy code that was never implemented or tested
	 * 
	 * @param list
	 * @return
	 */
	public ArrayList<MenuObject> addPrintItem(ArrayList<MenuObject> list) {
		if (list.size() == 0) {
			MenuObject printMenu = new MenuObject();
			printMenu.setName("PRINT");
			printMenu.setParent(null);
			printMenu.setFinalNode(true);
			list.add(0, printMenu);	
			return list;
		}
		if (!list.get(0).getName().equals("PRINT")) {
			MenuObject printMenu = new MenuObject();
			printMenu.setName("PRINT");
			printMenu.setParent(null);
			printMenu.setFinalNode(true);
			list.add(0, printMenu);	
		}		
		return list;
	}
	
	/**
	 * Iterate over all child menus and check if child's parent is the parent we're trying to show
	 * 
	 * Get list of children for a parent. If there are no children then add some by matching full paths.
	 * 
	 * Used to determine which menus must be browsed in the onCreate of NavigationChildren
	 * 
	 * @param parent
	 * @return ArrayList of child menus (ArrayList defined as field)
	 */
	public ArrayList<MenuObject> getChildren(MenuList list) {
		// public ArrayList<MenuObject> getChildren(MenuObject parent, MenuList list) {
		if (this.childMenus.size() != 0) {
			return childMenus;
		}
		ArrayList<MenuObject> childMenus = new ArrayList<MenuObject>();		
		ArrayList<MenuObject> allChildMenus = list.getAllChildMenus();
		// Based on the 'allChildMenus' filter this routine starts searching one level down from root
		for (MenuObject m : allChildMenus) {
			if (this.getFullPath().equals(m.getParent().getFullPath())) {
			// if (parent.getFullPath().equals(m.getParent().getFullPath())) {
				childMenus.add(m);				
			}
		}
		return childMenus;		
	}
	
	
	/**
	 * The idea behind this method was to determine is a menu has children, this is useful in navigation.
	 * This should really be set programmatically. But what about isFinalNode?
	 * @return
	 */
	public boolean hasChildren() {
		
		return false;
		
	}
	
	/**
	 * Add a name/value pair as a favourite
	 * 
	 * @param c
	 * @return True if added
	 */
	public void addFavouriteParam(ConfigItem c) {				
		favouriteParamsList.add(c);
		this.isFavouriteMenu = true;
	}
	
	/**
	 * Remove a favourite parameter from this menu object. If it's the last item in the list make the menu a non-favourite
	 * @param c
	 */
	public void removeFavouriteParam(ConfigItem c) {
		boolean result = favouriteParamsList.remove(c);
		if (result) {
			Log.d(TAG, "Object removed");			
		} else {
			Log.d(TAG, "No object removed");
		}
		if (this.favouriteParamsList.size() == 0) {
			this.isFavouriteMenu = false;
		}		
	}
	
	/**
	 * Check if this menu object already has the passed configuration item as favourite
	 * 
	 * @param c
	 * @return
	 */
	public boolean isFavouriteParam(ConfigItem c) {
		if (this.favouriteParamsList.contains(c)) {			
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<String> getFavouriteItems() {
		ArrayList<String> favouriteItems = new ArrayList<String>();
		for (ConfigItem c : this.favouriteParamsList) {
			favouriteItems.add(c.getName() + ": " + c.getValue());
		}
		return favouriteItems;
	}
	
//	public MenuObject findParentFromPath(String menuPath, MenuList list) {
//		for (MenuObject m : list) {
//			if (m.parent != null && m.parent.path.equals(menuPath)) {
//				return m;
//			}
//		}
//		// This should never happen
//		return null;
//	}

//	/**
//	 * Check if a parent exists in the list and if not set it
//	 * @param menuPath
//	 */
//	public void addParent(String menuPath, MenuList list) {
//		if (list.contains(menuPath)) {
//			return;
//		} else {			
//			MenuObject menu = new MenuObject();
//			int lastSpace = menuPath.lastIndexOf(" ");
//			if (lastSpace != -1) {				
//				String menuName = menuPath.substring(lastSpace + 1, menuPath.length());
//				menu.setName(menuName);	
//				String parentMenuPath = menuPath.substring(0, lastSpace);
//				menu.setPath(parentMenuPath);
//			} else {
//				menu.setName(menuPath);
//				menu.setPath("/");
//			}
//			
//			this.setParent(menu);			
//		}
//	}
	
}
