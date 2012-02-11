package com.mikrodroid.router;

import com.mikrodroid.router.db.DevicesDbAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class EditDevice extends Activity {
	
	private static final String TAG = "EditDevice";

	private EditText mName;
	private EditText mIpAddress; 
        
    private EditText mUsername;
    private EditText mPassword;
    private RadioButton mMikrotikRadio;
    private RadioButton mOtherRadio;
    private CheckBox mUseGlobalLogin;
    
    private String mStatus;
    private String mType;
    
    private DevicesDbAdapter mDbHelper;    
    
    private Long mRowId;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.edit_device);        
        setTitle(R.string.window_edit_device);
        
        mDbHelper = new DevicesDbAdapter(this);
        mDbHelper.open();
        
        mName = (EditText) findViewById(R.id.text_name);
        mIpAddress = (EditText) findViewById(R.id.text_ip_address);        
        mUseGlobalLogin = (CheckBox) findViewById(R.id.checkbox_use_global_login);
        mUsername = (EditText) findViewById(R.id.text_username);
        mPassword = (EditText) findViewById(R.id.text_password);
              
        // TODO Add code to get radio state so that mType can be set
        Button confirmButton = (Button) findViewById(R.id.button_confirm);
             
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DevicesDbAdapter.KEY_DEVICES_DEVICE_ID)
                                    : null;
        }
        
        // Seems that in onResume populateFields() is called anyway

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
        // Code to set the right button
        
    }
    
    private OnClickListener radio_listener = new OnClickListener() {
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            mType = (String) rb.getText();
        }
    };    
    
    private void populateFields() {
    	
    	int useGlobalLogin = 0;
    	
        if (mRowId != null) {
            Cursor device = mDbHelper.fetchDevice(mRowId);
            startManagingCursor(device);
            mName.setText(device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_NAME)));
            mIpAddress.setText(device.getString(
                        device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_IP_ADDRESS)));             
            mStatus = device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_STATUS));
            String type = device.getString(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_TYPE));            
            //
            useGlobalLogin = device.getInt(device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_USE_GLOBAL_LOGIN));            
            if (useGlobalLogin == 1) {
            	mUseGlobalLogin.setChecked(true);
            } else {
            	mUseGlobalLogin.setChecked(false);
            }            
            Log.v(TAG, "useGlobalLogin value:" + useGlobalLogin);
            //
            mUsername.setText(device.getString(
                    device.getColumnIndexOrThrow(DevicesDbAdapter.KEY_DEVICES_USERNAME)));
            mPassword.setText(device.getString(
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
    	String name = mName.getText().toString();
    	String ipAddress = mIpAddress.getText().toString();
    	String username = mUsername.getText().toString();
    	String password = mPassword.getText().toString();    	
    	boolean boolUseGlobalLogin = mUseGlobalLogin.isChecked();    	
    	int intUseGlobalLogin = (boolUseGlobalLogin == true)? 1:0;
    	                
        if (!(name.equals("") && ipAddress.equals(""))) {        
	        // If new product
	        if (mRowId == null) {  	        			    		    	
	            long id = mDbHelper.addDevice(name, ipAddress, mType, intUseGlobalLogin, username, password);
	            if (id > 0) {
	                mRowId = id;
	            }
	        } else {
	            mDbHelper.updateDevice(mRowId, name, ipAddress, mType, intUseGlobalLogin, username, password, mStatus);
	        }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
    
}
