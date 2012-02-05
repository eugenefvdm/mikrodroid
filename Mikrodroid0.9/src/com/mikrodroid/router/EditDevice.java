package com.mikrodroid.router;

import com.mikrodroid.router.db.DevicesDbAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class EditDevice extends Activity {

	private EditText mNameText;
	private EditText mIpAddressText;    
    private String mStatus;
    private String mType;
    private EditText mUsernameText;
    private EditText mPasswordText;
    private RadioButton mMikrotikRadio;
    private RadioButton mOtherRadio;
    
    private DevicesDbAdapter mDbHelper;    
    
    private Long mRowId;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new DevicesDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.device_edit);
        
        setTitle(R.string.window_edit_device);

        mIpAddressText = (EditText) findViewById(R.id.text_ip_address);
        mNameText = (EditText) findViewById(R.id.text_name);
        mUsernameText = (EditText) findViewById(R.id.text_username);
        mPasswordText = (EditText) findViewById(R.id.text_password);
        
        
        // TODO Add code to get radio state so that mType can be set

        Button confirmButton = (Button) findViewById(R.id.button_confirm);
             
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID)
                                    : null;
        }
        
        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (mType != null) {
	        	    setResult(RESULT_OK);
	        	    finish();
        		} else {
        			// TODO Convert move "Choose a device type" to strings 
        			Toast.makeText(EditDevice.this, "Choose a device type", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        
        mMikrotikRadio = (RadioButton) findViewById(R.id.radio_mikrotik);
        mOtherRadio = (RadioButton) findViewById(R.id.radio_other);
        mMikrotikRadio.setOnClickListener(radio_listener);
        mOtherRadio.setOnClickListener(radio_listener);
        
    }
    
    private OnClickListener radio_listener = new OnClickListener() {
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            mType = (String) rb.getText();
        }
    };    
    
    private void populateFields() {
        if (mRowId != null) {
            Cursor device = mDbHelper.fetchDevice(mRowId);
            startManagingCursor(device);
            mIpAddressText.setText(device.getString(
                        device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS)));
            mNameText.setText(device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_NAME))); 
            mStatus = device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_STATUS));
            String type = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_TYPE));            
            mUsernameText.setText(device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_USERNAME)));
            mPasswordText.setText(device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_PASSWORD)));
            if (type != null && type.contains("MikroTik")) {
            	mType = "MikroTik";
            	mMikrotikRadio = (RadioButton) findViewById(R.id.radio_mikrotik);
            	mMikrotikRadio.toggle();
            	
            } else if (type != null) {
            	mType = "Other";
            	mOtherRadio = (RadioButton) findViewById(R.id.radio_other);
            	mOtherRadio.toggle();            	
            }             
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState() {
    	String name = mNameText.getText().toString();
    	String ipAddress = mIpAddressText.getText().toString();
    	String username = mUsernameText.getText().toString();
    	String password = mPasswordText.getText().toString();
                
        if (!(name.equals("") && ipAddress.equals(""))) {
        
	        // If new product
	        if (mRowId == null) {  
	        			    		    	
	            long id = mDbHelper.addDevice(ipAddress, name, mType, username, password);
	            if (id > 0) {
	                mRowId = id;
	            }
	        } else {
	            mDbHelper.updateDevice(mRowId, ipAddress, name, mType, username, password, mStatus);
	        }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
    
}
