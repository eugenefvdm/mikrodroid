/*
 *  
 */

package com.mikrodroid.router.ui;

import java.util.ArrayList;

import com.mikrodroid.router.AsyncOutput;
import com.mikrodroid.router.Main;
import com.mikrodroid.router.MenuObject;
import com.mikrodroid.router.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * NavigationChildren gets launched from NavigationRoot as soon as you start browsing a root menu
 * It is called iteratively
 * Once you encounter a final or print menu it launches AsyncOutput
 * 
 * @author eugene
 *
 */
public class Navigation extends ListActivity {	
	
	private static final String TAG = "Navigation";

	/**
	 * 
	 */
	private ArrayList<MenuObject> mSecondLevelNav;
	
	/**
	 * Store current menu for when iterating to print because this it is overwritten when navigating
	 */
	private MenuObject mCurrentMenu = Main.currentMenu;
	
	/**
	 * IP address is passed [iteratively] to the navigation
	 */
	private String mIpAddress;
	
	/**
	 * Name is passed [iteratively] the the navigation
	 */
	private String mName;
	
	/**
	 * firstLaunch used in bundle to determine if we're working with root menus or not
	 */
	private boolean firstLaunch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.menu_list);
		
		Log.d(TAG, "Getting extras");
		Bundle b = getIntent().getExtras();			
		mIpAddress = b.getString("ipAddress");		
		mName = b.getString("name");
		firstLaunch = b.getBoolean("firstLaunch");
		
		if (firstLaunch == true) {			
			Log.v(TAG, "Root level navigation active");
			setTitle(mIpAddress + " (" + mName + ")"); // Original code from NavigationRoot			
			//mSecondLevelNav = Main.currentMenu.getChildren(Main.menuList);
			//Main.currentMenu = Main.rootMenuList.get(position);
		} else {
			Log.v(TAG, "Second level navigation active");
			setTitle(mIpAddress + Main.currentMenu.getBreadCrumb(Main.currentMenu));
			mSecondLevelNav = Main.currentMenu.getChildren(Main.menuList);
			
			Log.d(TAG, "Navigation to " + Main.currentMenu.getName() + " menu");
			
			if (mSecondLevelNav.size() == 0) {
				Log.v(TAG, "In constructor menu has no children");
				// Toast.makeText(this, "Menu has no children", Toast.LENGTH_SHORT).show();
				// Main.currentMenu = menu;
				Intent i = new Intent(this, AsyncOutput.class);
				i.putExtra("ipAddress", this.mIpAddress);
				startActivity(i);
//				mChildMenuList = Main.currentMenu.addPrintItem(mChildMenuList);
			}
			// isPrintable has not been activated for now

			if (Main.currentMenu.isPrintable) {
				Log.v(TAG, "This menu is printable");
				mSecondLevelNav = Main.currentMenu.addPrintItem(mSecondLevelNav);
			}
			
		}
		
		MenuAdapter routerAdapter;						
		
		if (firstLaunch) {
			routerAdapter = new MenuAdapter(this, R.layout.device_row, Main.rootLevelNav);	
		} else {
			routerAdapter = new MenuAdapter(this, R.layout.device_row, mSecondLevelNav);
		}
		
		ListView menuListView = getListView();
		menuListView.setAdapter(routerAdapter);		
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		MenuObject menu = null;
		
		if (firstLaunch == true) {
			menu = Main.rootLevelNav.get(position);	
		} else {
			menu = mSecondLevelNav.get(position);
		}
		
		if (menu.getChildren(Main.menuList).size() == 0) {
			Main.currentMenu = menu;
			Log.d(TAG, "Menu has no children skipping to output");
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", this.mIpAddress);
			startActivity(i);				
		} else if (menu.getName() == "PRINT") {
			Log.d(TAG, "This is a PRINT node");
			// TODO menu and mCurrentMenu is used crossed here, why?
			Main.currentMenu = mCurrentMenu; // Remember current menu when navigating to print 
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", mIpAddress);
			startActivity(i);						
		} else if (menu.getFinalNode()) {
			Log.d(TAG, "This is a final node");
			Main.currentMenu = menu;
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", this.mIpAddress);
			startActivity(i);		
		} else { // Iterate to self		
			Log.d(TAG, "Iterating to self");
			Main.currentMenu = menu;
			Intent i = new Intent(this, Navigation.class);
			i.putExtra("ipAddress", this.mIpAddress);
			startActivity(i);	
		}				
		
	}
	
	private class MenuAdapter extends ArrayAdapter<MenuObject> {
		
		private ArrayList<MenuObject> menuList;

		public MenuAdapter(Context context, int textViewResourceId, ArrayList<MenuObject> menuList) {
			super(context, textViewResourceId, menuList);
			this.menuList = menuList;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.menu_row, null);			
			TextView menu = (TextView) v.findViewById(R.id.menu);			
			menu.setText(menuList.get(position).getFriendlyName());
			// menu.setText(menuList.get(position).getPath() + " " + menuList.get(position).getName());
			return v;
		}

	}

}