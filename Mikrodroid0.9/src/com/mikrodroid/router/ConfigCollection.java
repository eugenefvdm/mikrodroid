/**
 * Parcelable collection of configuration objects
 * http://developer.android.com/reference/android/os/Parcelable.html
 * http://www.anddev.org/simple_tutorial_passing_arraylist_across_activities-t9996.html
 * 
 */

package com.mikrodroid.router;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigCollection implements Parcelable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2081073713765521092L;
	
	public ConfigCollection() {
		
	}
	
	private ConfigCollection(Parcel in) {
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<ConfigCollection> CREATOR = new Parcelable.Creator<ConfigCollection>() {

		public ConfigCollection createFromParcel(Parcel in) {
			return new ConfigCollection(in);
		}
	
		public ConfigCollection[] newArray(int size) {
		    return new ConfigCollection[size];
		}
		
	};
	
	private void readFromParcel(Parcel in) {
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			ConfigItem c = new ConfigItem();
			c.setName(in.readString());
			c.setValue(in.readString());
			this.addItem(c);
		}
	}
	
	ArrayList<ConfigItem> collection = new ArrayList<ConfigItem>();

	@Override
	public int describeContents() {		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {		
		int size = this.collection.size();
		dest.writeInt(size);		
		for (int i = 0; i < size; i++) {			
			ConfigItem c = this.collection.get(i);
			dest.writeString(c.getName());
			dest.writeString(c.getValue());
		}				
	}
	
	public void addItem(ConfigItem c) {
		collection.add(c);
	}
	
	public ConfigItem getItem(int index) {
		return collection.get(index);
	}
	
	public ArrayList<ConfigItem> getAllItems() {		
		return collection;				
	}
	
}