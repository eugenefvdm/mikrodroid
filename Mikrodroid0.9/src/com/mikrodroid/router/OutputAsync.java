/**
 * 
 * OutputAsync.java
 * 
 * TODO Implement menuCommand = "/tool/ping\n=address=192.168.0.2";
 * TODO Implement menuCommand = "/interface/monitor-traffic\n=interface=uplink";
 * TODO Both commands can run
 * 
 */
package com.mikrodroid.router;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
//import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.mikrodroid.router.api.MikrotikApi;

/**
 * Asynchronous class that queries the MikroTik API device and displays it's configuration information
 *
 */
public class OutputAsync extends ListActivity {
	
	private static final String TAG = "OutputAsync";
		
	private AsyncReceiver task = null;
	
	private ListView mItemListView = null;
	private ListView mCollectionListView = null;
		
	/**
	 * Parameters adapter with custom view with two columns for param and value
	 * Used for list of configuration parameters
	 */
	private ParamsAdapter mMultiRowAdapter;
	
	/**
	 * Used for single configuration screen
	 */
	private ItemValueAdapter mSingleRowAdapter;
	
	/**
	 * Determine if multi-line or single line configuration must be outputted
	 */
	private boolean isMultiLine = false;
	
	private boolean isPrintable = false;
	
	private String ipAddress;
	
	private ArrayList<ConfigItem> mItemValueList;
	
	/**
	 * Convenience field that is assigned to current menu
	 */
	private MenuObject mCurrentMenu;
	
	private static final int MENU_FILTER = Menu.FIRST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String menuCommand;
				
		//setContentView(R.layout.config_multi_row);
		setContentView(R.layout.output);
		
		Bundle b = getIntent().getExtras();
		ipAddress = b.getString("ipAddress");
		
		mCurrentMenu = Main.currentMenu;
		setTitle(ipAddress + mCurrentMenu.getBreadCrumb(mCurrentMenu));	
				
		if (mCurrentMenu.getMultiLine()) {
			this.isMultiLine = true;
		}
		
		// If this menu has no children then add a flag to do an automatic print 
		if (mCurrentMenu.getChildren(Main.menuList).size() == 0 || mCurrentMenu.getPrintable() == true) {			
			this.isPrintable = true;
		}
		
		mItemListView = getListView(); 
		mCollectionListView = getListView();
		
		task = new AsyncReceiver(Main.apiConn);
		task.execute();
		
		// Temporary assigning menu to variable for later manipulation
		menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu);
		
		if (this.isPrintable == true) {
			menuCommand = menuCommand + "/print";
		}
		
		// Filter properties
		String propList = mCurrentMenu.getPropList();		
		if (propList != null) {
			Log.v(TAG, "Filtering on .proplist");			
			menuCommand = menuCommand + "\n=.proplist=.id," + propList;
		}		
				
		Toast.makeText(this, menuCommand, Toast.LENGTH_SHORT).show();
		
		menuCommand = menuCommand + "\n.tag=1";				
		Log.v(TAG, menuCommand);		
		Main.apiConn.sendCommand(menuCommand);
		
		registerForContextMenu(getListView());
	
	}
	
	private class AsyncReceiver extends AsyncTask<Void, Void, String> {

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
						Log.v(TAG, currentResult);					
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
		ConfigList list = getConfigList(result);
		if (isMultiLine) {			
			ArrayList<ConfigCollection> mParamsList = list.getAllItems();
			// Once we have a list of parameters we build the layout according to size
			//buildLayout(mParamsList.get(0).getAllItems().size());
			mMultiRowAdapter = new ParamsAdapter(this, R.layout.config_multi_row_2, mParamsList);			
			mCollectionListView.setAdapter(mMultiRowAdapter);			
		} else {
			ConfigCollection singleItem = list.getItem(0);
			mItemValueList = singleItem.getAllItems();			 
			OutputAsync.this.mSingleRowAdapter = new ItemValueAdapter(OutputAsync.this, R.layout.config_single_row, mItemValueList);
			mItemListView.setAdapter(mSingleRowAdapter);				
		}
	}
	
	/**
	 * Custom Array Adapter with a view that displays multi-line item/value pairs 
	 */
	private class ParamsAdapter extends ArrayAdapter<ConfigCollection> {

		private ArrayList<ConfigCollection> objectList;		

		public ParamsAdapter(Context context, int textViewResourceId, ArrayList<ConfigCollection> objectList) {
			super(context, textViewResourceId, objectList);
			this.objectList = objectList;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {						
			TextView tv1 = null, tv2, tv3;			
			ConfigCollection collection = objectList.get(position);
			ArrayList<ConfigItem> itemList = collection.getAllItems();				
			int numItems = itemList.size();
			ConfigItem[] param = new ConfigItem[numItems];
			
			//Log.v(TAG, "Number of items to inflate: " + numItems);			
			for (int i = 0; i < numItems; i++) {
				param[i] = itemList.get(i);
			}						
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// TODO Temporary fix to cater for less than 4 fields
			if (numItems > 4) {
				numItems = 4;
			}
			switch (numItems - 1) {
			case 1:				
				v = vi.inflate(R.layout.config_multi_row_1, null);
				tv1 = (TextView) v.findViewById(R.id.item1);
				tv1.setText(param[1].getValue());				
				break;
			case 2:				
				v = vi.inflate(R.layout.config_multi_row_2, null);
				tv1 = (TextView) v.findViewById(R.id.item1);
				tv2 = (TextView) v.findViewById(R.id.item2);
				tv1.setText(param[1].getValue());
				tv2.setText(param[2].getValue());				
				break;
			case 3:				
				v = vi.inflate(R.layout.config_multi_row_3, null);
				tv1 = (TextView) v.findViewById(R.id.item1);
				tv2 = (TextView) v.findViewById(R.id.item2);
				tv3 = (TextView) v.findViewById(R.id.item3);
				tv1.setText(param[1].getValue());
				tv2.setText(param[2].getValue());
				tv3.setText(param[3].getValue());								
				break;
			}
			
			tv1.setOnClickListener(new listViewClickListener(collection, param[0].getValue()));
					
			return v;
		}
		
		class listViewClickListener implements OnClickListener {

			ConfigCollection collection;
			
			/**
			 * MikroTik Internal ID of list view item 
			 */
			String id;
			
			public listViewClickListener(ConfigCollection collection, String id) {
				this.collection = collection;
				this.id = id;
			}
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Obtaining information for " + id);
				Bundle b = new Bundle();
				b.putParcelable("collection", collection);				
				Intent i = new Intent(OutputAsync.this, OutputPreferences.class);
				i.putExtra("ipAddress", OutputAsync.this.ipAddress);
				i.putExtra("id", id);
				i.putExtras(b);				
				startActivity(i);
			}
		}

	}
	
	public ConfigList getConfigList(String data) {

		ConfigList list = new ConfigList();
		ConfigCollection collection = null;		
		ConfigItem item = null;
		
		String paramName, paramValue;
		int start_of_value;

		String lines[] = data.split("\\n");		
		for (String line : lines) {
			
			if (line.equals("!trap")) {				
				// Do something errors for example invalid commands
			}			
			
			// Start of result
			if (line.equals("!re") || line.equals("!trap")) {				
				collection = new ConfigCollection();
			}
			
			// If there are two or more = signs then this line should contain a parameter
			if (StringUtils.countMatches(line, "=") >= 2 && line.charAt(0) == '=') {
				start_of_value = line.indexOf('=', 1);
				paramName = line.substring(1, start_of_value);
				paramValue = line.substring(start_of_value + 1, line.length());
				item = new ConfigItem();				
				item.setName(paramName);
				item.setValue(paramValue);
				collection.addItem(item);
				item = null;
			}
			
			// End of result for this round, time to start another collection 
			if (line.contains(".tag=") == true) {
				list.addItem(collection);
				collection = null;
			}
			
			// End of configuration request
			if (line.equals("!done") == true) {
				return list;
			}			
			
		}
		return null;
	}
		
	private class ItemValueAdapter extends ArrayAdapter<ConfigItem> {

		public ItemValueAdapter(Context context, int textViewResourceId, ArrayList<ConfigItem> paramList) {
			super(context, textViewResourceId, paramList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.config_single_row, null);			
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

		//Toast.makeText(this, "Item #" + mApiId[(int) id] + " clicked.", Toast.LENGTH_SHORT).show();
		
//		ConfigItem param = mItemValueList.get((int)id);
//		if (!mCurrentMenu.isFavouriteParam(param)) {
//			mCurrentMenu.addFavouriteParam(param);			
//			Log.d(TAG, param.name + " added to favourites");
//		} else {
//			mCurrentMenu.removeFavouriteParam(param);			
//			Log.d(TAG, param.name + " removed from favourites");			
//		}
//		mSingleRowAdapter.notifyDataSetChanged();
		
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