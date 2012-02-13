/*
 * Copyright (C) 2011-2012 Snowball
 * 
 */

package com.mikrodroid.router.ui;

import java.util.ArrayList;

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
 * Navigate the root menu file system of a MikroTik router
 *
 * Launched as soon as you successfully log into a MikroTik router from Main
 * 
 * @author eugene
 *
 */
public class NavigationRoot extends ListActivity {
	
	private static final String TAG = "NavigationRoot";
	
	private String mIpAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.menu_list);
				
		Bundle b = getIntent().getExtras();		
		mIpAddress = b.getString("ipAddress");		
		String name = b.getString("name");		
		setTitle(mIpAddress + " (" + name + ")");
				
		MenuAdapter routerAdapter = new MenuAdapter(this, R.layout.device_row, Main.rootLevelNav);				
		getListView().setAdapter(routerAdapter);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Main.currentMenu = Main.rootLevelNav.get(position);

		// If this menu has no children then jump strait to output
		if (Main.currentMenu.getChildren(Main.menuList).size() == 0) {
			Log.d(TAG, "In constructor menu has no children");
		}
				
		Intent i = new Intent(this, Navigation.class);
		i.putExtra("ipAddress", mIpAddress);
		startActivity(i);		
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
			return v;
		}

	}

}