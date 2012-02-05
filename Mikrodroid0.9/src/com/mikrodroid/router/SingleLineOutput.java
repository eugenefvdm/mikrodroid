/**
 * Copyright (C) 2011 Snowball
 * 
 *  Display router configuration single line parameters
 * 
 */
package com.mikrodroid.router;

import java.util.ArrayList;

import com.mikrodroid.router.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SingleLineOutput extends ListActivity {
	
	private static final String TAG = "SingleLineOutput.java";
	
	private ListView mCollectionListView = null;
	
	private MenuObject mCurrentMenu;
	
	/**
	 * Used for single configuration screen
	 */
	private ItemValueAdapter mSingleRowAdapter;
	
	ArrayList<ConfigItem> mItemValueList;
	
	private static final int MENU_FILTER = Menu.FIRST;
	
	private String ipAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.output);
		
		Bundle bundle = getIntent().getExtras(); //Get the intent's extras	
		
		ipAddress = bundle.getString("ipAddress");	
		mCurrentMenu = Main.currentMenu;
		setTitle(ipAddress + mCurrentMenu.getBreadCrumb(mCurrentMenu));		
		
		mCollectionListView = getListView(); 
		ConfigCollection singleItem = bundle.getParcelable("collection");						
		mItemValueList = singleItem.getAllItems();			 
		SingleLineOutput.this.mSingleRowAdapter = new ItemValueAdapter(SingleLineOutput.this, R.layout.single_row_config, mItemValueList);		
		mCollectionListView.setAdapter(mSingleRowAdapter);
		
		registerForContextMenu(getListView());
		
	}
	
	private class ItemValueAdapter extends ArrayAdapter<ConfigItem> {

		public ItemValueAdapter(Context context, int textViewResourceId, ArrayList<ConfigItem> paramList) {
			super(context, textViewResourceId, paramList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.single_row_config, null);			
			ConfigItem param = mItemValueList.get(position);
			TextView item = (TextView) v.findViewById(R.id.item);			
			TextView value = (TextView) v.findViewById(R.id.value);			
			item.setText(param.getName());
			value.setText(param.getValue());			
			if (mCurrentMenu.isFavouriteParam(param)) {
				item.setTextColor(Color.CYAN);
				value.setTextColor(Color.CYAN);	
			}						
			return v;
		}
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);				
		ConfigItem param = mItemValueList.get((int)id);
		if (!mCurrentMenu.isFavouriteParam(param)) {
			mCurrentMenu.addFavouriteParam(param);			
			Log.d(TAG, param.name + " added to favourites");
		} else {
			mCurrentMenu.removeFavouriteParam(param);			
			Log.d(TAG, param.name + " removed from favourites");			
		}
		mSingleRowAdapter.notifyDataSetChanged();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);		
		menu.add(0, MENU_FILTER, 0, R.string.menu_filter);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {		
		case MENU_FILTER:
			Intent menuFilterIntent = new Intent(this, FilteredView.class);		
			menuFilterIntent.putExtra("ipAddress", ipAddress);
			startActivity(menuFilterIntent);			
			return true;	
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
}