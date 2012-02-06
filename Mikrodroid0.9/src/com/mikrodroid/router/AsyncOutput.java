/**
 * 
 * AsyncOutput.java
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
import android.widget.TextView;
import android.widget.Toast;

import com.mikrodroid.router.api.MikrotikApi;

/**
 * Asynchronous class that interrogates a MikroTik device and displays it's configuration information
 *
 */
public class AsyncOutput extends ListActivity {
	
	private static final String TAG = "AsyncOutput";
		
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
	
	private boolean isFinalMenu = false;
	
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
		
		setContentView(R.layout.output);
		
		Bundle b = getIntent().getExtras();
		ipAddress = b.getString("ipAddress");
		
		mCurrentMenu = Main.currentMenu;
		setTitle(ipAddress + mCurrentMenu.getBreadCrumb(mCurrentMenu));	
				
		if (mCurrentMenu.getMultiLine()) {
			this.isMultiLine = true;
		}
		
		// Is this menu has no children then add a flag to do an automatic print
		// The If statement was copied from NavigationChildren 
		if (mCurrentMenu.getChildren(Main.menuList).size() == 0 || mCurrentMenu.getPrintable() == true) {			
			this.isFinalMenu = true;
		}
		
		mItemListView = getListView(); 
		mCollectionListView = getListView();
		
		task = new AsyncReceiver(Main.apiConn);
		task.execute();
		
		//String menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu) + "/print";
		
		String propList = mCurrentMenu.getPropList();
		
		if (propList != null) {
			menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu) + "\n=.proplist=.id," + propList;	
		} else if (this.isFinalMenu ==true) {
			menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu) + "/print";
		} else {
			menuCommand = mCurrentMenu.getCommandHierarchy(mCurrentMenu);
		}
		
		
		Toast.makeText(this, menuCommand, Toast.LENGTH_SHORT).show();		
		menuCommand = menuCommand + "\n.tag=1";		
		// menuCommand = "/tool/ping\n=address=192.168.0.2";
		// menuCommand = "/interface/monitor-traffic\n=interface=uplink";
		Log.d(TAG, menuCommand);		
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
		ConfigList list = getConfigList(result);
		if (isMultiLine) {			
			ArrayList<ConfigCollection> mParamsList = list.getAllItems();			
			//AsyncOutput.this.mMultiRowAdapter = new ParamsAdapter(AsyncOutput.this, R.layout.multi_row_config, mParamsList);
			mMultiRowAdapter = new ParamsAdapter(AsyncOutput.this, R.layout.multi_row_config2, mParamsList);
			mCollectionListView.setAdapter(mMultiRowAdapter);			
		} else {
			ConfigCollection singleItem = list.getItem(0);
			mItemValueList = singleItem.getAllItems();			 
			AsyncOutput.this.mSingleRowAdapter = new ItemValueAdapter(AsyncOutput.this, R.layout.single_row_config, mItemValueList);
			mItemListView.setAdapter(mSingleRowAdapter);				
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
				// Log.d(TAG, "Starting new ConfigCollection");
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
			View v = convertView;

			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.multi_row_config2, null);
						
			TextView tv1 = (TextView) v.findViewById(R.id.item1);
			TextView tv2 = (TextView) v.findViewById(R.id.item2);
			TextView tv3 = (TextView) v.findViewById(R.id.item3);
			//TextView tv4 = (TextView) v.findViewById(R.id.item4);
			//TextView tv5 = (TextView) v.findViewById(R.id.item5);
									
			ConfigCollection collection = objectList.get(position);

			ArrayList<ConfigItem> itemList = collection.getAllItems();
			
			ConfigItem idParam = itemList.get(0);
			ConfigItem param1 = itemList.get(1);
			ConfigItem param2 = itemList.get(2);
			ConfigItem param3 = itemList.get(3);
			//ConfigItem param4 = itemList.get(4);
			//ConfigItem param5 = itemList.get(5);
						
			tv1.setText(param1.getValue());
			tv2.setText(param2.getValue());
			tv3.setText(param3.getValue());
			//tv4.setText(param4.getValue());
			//tv5.setText(param5.getValue());
			
			if (idParam.getName().equals(".id")) {
				tv1.setOnClickListener(new listViewClickListener(collection));
			}
					
			return v;
		}
		
		class listViewClickListener implements OnClickListener {

			ConfigCollection collection;
			
			public listViewClickListener(ConfigCollection collection) {
				this.collection = collection;				
			}
			
			@Override
			public void onClick(View v) {				
				Bundle b = new Bundle();
				b.putParcelable("collection", collection);				
				Intent i = new Intent(AsyncOutput.this, SingleLineOutput.class);
				i.putExtra("ipAddress", AsyncOutput.this.ipAddress);
				i.putExtras(b);				
				startActivity(i);
			}
		}

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