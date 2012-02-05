package com.mikrodroid.router;

import java.util.ArrayList;

//import android.util.Log;

public class MenuList extends ArrayList<MenuObject> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7067219011532914693L;

	private ArrayList<MenuObject> allMenusList = new ArrayList<MenuObject>();
	//private ArrayList<MenuObject> allMenusList;
	
	private ArrayList<MenuObject> rootMenus = new ArrayList<MenuObject>();
	
	//private static final String TAG = "MenuList";
	
	public MenuList() {		
		//initAllMenus();		
	}

	public void addMenu(MenuObject c) {
		allMenusList.add(c);
	}
	
	public MenuObject getMenu(int index) {
		return allMenusList.get(index);
	}
	
	/**
	 * Get a list of all root menus (where parent is null)
	 * 
	 * Cycle through each menu and see if their parent is null
	 * 
	 * @return ArrayList of root menus
	 */
	public ArrayList<MenuObject> getRootMenus() {
		if (this.rootMenus.size() != 0) {
			return rootMenus;
		}
		for (MenuObject menu : this) {
			if (menu.getPath().equals("/")) {
				rootMenus.add(menu);
			}
		}
		return rootMenus;		
	}
		
	/**
	 * Build a list of all children (where parent != null)
	 * @return
	 */
	public ArrayList<MenuObject> getAllChildMenus() {		
		ArrayList<MenuObject> allChildMenus = new ArrayList<MenuObject>();			
		for (MenuObject menu : this) {		
			if (menu.getParent() != null) {
				allChildMenus.add(menu);
			}
		}
		return allChildMenus;		
	}
	
//	public ArrayList<MenuObject> initAllMenus() {
//		populateMenus();
//		return allMenusList;				
//	}
	
	/**
	 * Iterate through all menus and return array of strings of favourites
	 * 
	 * This is used when showing a filtered configuration
	 * @return
	 */
	public ArrayList<String> getFavouriteMenusFullPath() {
		ArrayList<String> menuList = new ArrayList<String>();
		
		for (MenuObject m : allMenusList) {
			if (m.isFavouriteMenu) {
				menuList.add(m.getFullPath(m));
			}			
		}
		return menuList;
	}
	
	/**
	 * Build a list of favourite menu objects this will be used to determine menu favourite param items
	 * 
	 * @return
	 */
	public ArrayList<MenuObject> getFavouriteMenus() {
		ArrayList<MenuObject> menuList = new ArrayList<MenuObject>();		
		for (MenuObject m : allMenusList) {
			if (m.isFavouriteMenu) {
				menuList.add(m);
			}			
		}
		return menuList;
	}
	/**
	private void populateMenus() {
		
		allMenusList = new ArrayList<MenuObject>();
		
		MenuObject menu1 = new MenuObject();
		menu1.setName("ip");
		menu1.setFriendlyName("IP");
		menu1.setParent(null);		
		allMenusList.add(menu1);

		MenuObject menu2 = new MenuObject();
		menu2.setName("firewall");		
		menu2.setParent(menu1);
		allMenusList.add(menu2);
		
		MenuObject menu3 = new MenuObject();
		menu3.setName("nat");
		menu3.setFriendlyName("NAT");
		menu3.setParent(menu2);
		menu3.setFinalNode(true);
		menu3.setMultiLine(true);
		allMenusList.add(menu3);
		
		MenuObject menu15 = new MenuObject();
		menu15.setName("filter");		
		menu15.setParent(menu2);
		menu15.setFinalNode(true);
		menu15.setMultiLine(true);
		allMenusList.add(menu15);
		
		MenuObject menu16 = new MenuObject();
		menu16.setName("mangle");		
		menu16.setParent(menu2);
		menu16.setFinalNode(true);
		menu16.setMultiLine(true);
		allMenusList.add(menu16);

		MenuObject menu4 = new MenuObject();
		menu4.setName("address");		
		menu4.setParent(menu1);		
		menu4.setFinalNode(true);
		menu4.setMultiLine(true);
		allMenusList.add(menu4);
		
		MenuObject menu10 = new MenuObject();
		menu10.setName("hotspot");
		menu10.setParent(menu1);
		menu10.setPrintable(true);
		menu10.setMultiLine(true);
		allMenusList.add(menu10);
		
		MenuObject menu11 = new MenuObject();
		menu11.setName("user");
		menu11.setParent(menu10);
		menu11.setPrintable(true);
		allMenusList.add(menu11);
		
		MenuObject menu12 = new MenuObject();
		menu12.setName("profile");
		menu12.setParent(menu11);
		menu12.setFinalNode(true);
		allMenusList.add(menu12);		
		
		MenuObject menu5 = new MenuObject();
		menu5.setName("system");
		menu5.setParent(null);
		allMenusList.add(menu5);
		
		MenuObject menu13 = new MenuObject();
		menu13.setName("identity");
		menu13.setParent(menu5);
		menu13.setFinalNode(true);		
		allMenusList.add(menu13);
		
		MenuObject menu6 = new MenuObject();
		menu6.setName("routerboard");
		menu6.setParent(menu5);				
		menu6.setPrintable(true);
		allMenusList.add(menu6);
		
		MenuObject menu7 = new MenuObject();
		menu7.setName("resource");
		menu7.setParent(menu5);
		menu7.setFinalNode(true);		
		allMenusList.add(menu7);
		
		MenuObject menu8 = new MenuObject();
		menu8.setName("queue");
		menu8.setParent(null);		
		allMenusList.add(menu8);
		
		MenuObject menu9 = new MenuObject();
		menu9.setName("simple");
		menu9.setParent(menu8);
		menu9.setFinalNode(true);
		menu9.setMultiLine(true);
		allMenusList.add(menu9);
		
		MenuObject menu14 = new MenuObject();
		menu14.setName("active");
		menu14.setParent(menu10);
		menu14.setFinalNode(true);
		menu14.setMultiLine(true);
		allMenusList.add(menu14);
		
		MenuObject menu17 = new MenuObject();
		menu17.setName("interface");
		menu17.setParent(null);
		menu17.setPrintable(true);
		menu17.setMultiLine(true);
		allMenusList.add(menu17);
		
		MenuObject menu18 = new MenuObject();
		menu18.setName("wireless");
		menu18.setParent(menu17);
		menu18.setPrintable(true);
		menu18.setMultiLine(true);
		allMenusList.add(menu18);
		
		MenuObject menu19 = new MenuObject();
		menu19.setName("ethernet");
		menu19.setParent(menu17);
		menu19.setPrintable(true);
		menu19.setMultiLine(true);
		allMenusList.add(menu19);
		
		MenuObject menu20 = new MenuObject();
		menu20.setName("pppoe-client");
		menu20.setFriendlyName("PPPoE-Client");
		menu20.setParent(menu17);
		menu20.setFinalNode(true);
		menu20.setMultiLine(true);
		allMenusList.add(menu20);
		
		MenuObject menu21 = new MenuObject();
		menu21.setName("registration-table");
		menu21.setFriendlyName("Registration-Table");
		menu21.setParent(menu18);
		menu21.setFinalNode(true);
		menu21.setMultiLine(true);
		allMenusList.add(menu21);
		
	}
	**/

	public boolean checkMenuExists(String menuPath, String menuName) {
		for (MenuObject m : this) {
			if (m.getPath().equals(menuPath) && m.getName().equals(menuName)) {					
				return true;
			}	
		}
		return false;
	}

//	public boolean checkRootMenuExists(String menuName) {
//		for (MenuObject m : this) {
//			if (m.getName().equals(menuName)) {
//				return true;
//			}
//		}
//		return false;
//	}
	
}
