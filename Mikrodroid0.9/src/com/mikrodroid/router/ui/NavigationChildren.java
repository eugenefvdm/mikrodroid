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
import android.widget.Toast;

/**
 * NavigationChildren gets launched from NavigationRoot as soon as you start browsing a root menu
 * It is called iteratively
 * Once you encounter a final or print menu it launches AsyncOutput
 * 
 * @author eugene
 *
 */
public class NavigationChildren extends ListActivity {	
	
	private static final String TAG = "NavigationChildren";

	/**
	 * 
	 */
	private ArrayList<MenuObject> mSecondLevelNav;
	
	/**
	 * Store current menu for when iterating to print because this it is overwritten when navigating
	 */
	private MenuObject mCurrentMenu = Main.currentMenu;
	
	private String mIpAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.menu_list);
		
		Bundle b = getIntent().getExtras();
		mIpAddress = b.getString("ipAddress");
						
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
//			mChildMenuList = Main.currentMenu.addPrintItem(mChildMenuList);
		}
		// isPrintable has not been activated for now

		if (Main.currentMenu.isPrintable) {
			Log.v(TAG, "This menu is printable");
			mSecondLevelNav = Main.currentMenu.addPrintItem(mSecondLevelNav);
		}
		
//		if (Main.currentMenu.isPrintable) {
//			mChildMenuList = Main.currentMenu.addPrintItem(mChildMenuList);
//		}
		
		MenuAdapter routerAdapter = new MenuAdapter(this, R.layout.device_row, mSecondLevelNav);
		
		ListView menuListView = getListView();
		menuListView.setAdapter(routerAdapter);		
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		MenuObject menu = mSecondLevelNav.get(position);
		
		if (menu.getName() == "PRINT") {
			Log.v(TAG, "This is a PRINT node");
			Main.currentMenu = mCurrentMenu; // Remember current menu when navigating to print 
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", mIpAddress);
			startActivity(i);			
		} else if (menu.getChildren(Main.menuList).size() == 0) {			
			Log.v(TAG, "This menu has no children");
			Main.currentMenu = menu;
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", this.mIpAddress);
			startActivity(i);
		} else if (menu.getFinalNode()) {
			Log.v(TAG, "This is a final node");
			Main.currentMenu = menu;
			Intent i = new Intent(this, AsyncOutput.class);
			i.putExtra("ipAddress", this.mIpAddress);
			startActivity(i);		
		} else { // Iterate to self		
			Log.v(TAG, "Iterating to self");
			Main.currentMenu = menu;
			Intent i = new Intent(this, NavigationChildren.class);
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