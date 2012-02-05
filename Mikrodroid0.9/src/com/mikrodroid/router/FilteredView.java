/*
 * Copyright (C) 2011 Snowball
 */

package com.mikrodroid.router;

import java.util.ArrayList;
import java.util.List;

import com.mikrodroid.router.R;
import com.mikrodroid.router.api.MikrotikApi;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Obtain favourite list of menus and break it down into configuration lists, e.g. /ip firewall print
 * 
 * This code is based on an example from the API
 * 
 */
public class FilteredView extends ExpandableListActivity {

    ExpandableListAdapter mAdapter;
    
    private AsyncReceiver task = null;
    
    private String ipAddress;
    
    private static final int MENU_FILTER = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle b = getIntent().getExtras();
		ipAddress = b.getString("ipAddress");
        
        mAdapter = new MyExpandableListAdapter();
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {    	  
        // menu.setHeaderTitle("Sample menu");
        menu.add(0, 0, 0, "Print");    	        
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        String title = ((TextView) info.targetView).getText().toString();
        
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {        	
        	int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
            Toast.makeText(this, title + ": Child " + childPos + " clicked in group " + groupPos, Toast.LENGTH_SHORT).show();
            return true;
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            //int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);            
            //Toast.makeText(this, title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();            
            task = new AsyncReceiver(Main.apiConn);
    		task.execute();
            title = title.replace(" ", "/");
            Main.apiConn.sendCommand(title + "/print");
            
            return true;
        }
        
        return false;
    }
    
    private class AsyncReceiver extends AsyncTask<Void, Void, String> {
    	
    	private static final String TAG = "AsyncReceiver.java";

    	private String currentResult = "";		
    	private String allResults = "";
    	private MikrotikApi apiConn; 
    	
    	AsyncReceiver(MikrotikApi apiConn) {			
    		this.apiConn = apiConn;		
    	}
    	
    	@Override
    	protected String doInBackground(Void... unused) {
    		
    		while (true) {
    			try {
    				currentResult = apiConn.getData();
    				if (currentResult != null) {
    					Log.d(TAG, currentResult);					
    					allResults = allResults + currentResult;						
    					if (currentResult.contains("!done")) {
    						return allResults;							
    					}
    				}
    			} catch (InterruptedException e) {
    				Log.e(TAG + " exception in AsyncReceiver doInBackground()", e.getMessage());
    			}
    		}
    	}
    	
    	protected void onPostExecute(String result) {
    		processAsyncTask(result);
    	}
    	
    }
    
    public void processAsyncTask(String result) {		
    			
		Bundle b = new Bundle();
		b.putParcelable("collection", MikrotikApi.getCollection(result));				
		Intent i = new Intent(this, SingleLineOutput.class);
		i.putExtra("ipAddress", ipAddress);		
		i.putExtras(b);				
		startActivity(i);
		
	}
    
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    	private String [] groups;
    	private String[][] children;
                        
    	public MyExpandableListAdapter() {
        	ArrayList<String> groups = Main.menuList.getFavouriteMenusFullPath();
			this.groups = groups.toArray(new String[groups.size()]);
			ArrayList<MenuObject> favouriteMenus = Main.menuList.getFavouriteMenus();
			this.children = new String[favouriteMenus.size()][];
			for (int i = 0; i < favouriteMenus.size(); i++) {
				List<String> favouriteItems = favouriteMenus.get(i).getFavouriteItems();
				children[i] = favouriteItems.toArray(new String[favouriteItems.size()]);
			}
		}

		public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 44); // Was 64

            TextView textView = new TextView(FilteredView.this);
            textView.setLayoutParams(lp);
            // Centre the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0); // Was 36
            textView.setTextSize(24); // New
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            textView.setTextSize(16); // New
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

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
