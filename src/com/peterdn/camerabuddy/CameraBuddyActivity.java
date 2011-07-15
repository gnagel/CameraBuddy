package com.peterdn.camerabuddy;

import java.io.File;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.mtp.MtpConstants;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.hardware.usb.*;

public class CameraBuddyActivity extends Activity {
	private MtpDevice _mtpDevice;
	private int _storageId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        ActionBar bar = this.getActionBar();
        
        bar.show();
        
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        
        if (!devices.isEmpty()) {        
	        UsbDevice usbDevice = devices.values().iterator().next();
	        UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
	        
	        _mtpDevice = new MtpDevice(usbDevice);
	        _mtpDevice.open(usbDeviceConnection);
	        
	        _storageId = (_mtpDevice.getStorageIds())[0];
	        
	        final MtpObjectAdapter adapter = new MtpObjectAdapter(this, _mtpDevice, _storageId);
	        final GridView gridView = (GridView) findViewById(R.id.gridview);
	        
	        final ProgressDialog pd = new ProgressDialog(this);
	        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        pd.setTitle("Connecting to ".concat(_mtpDevice.getDeviceInfo().getModel()).concat("..."));
    		pd.setCancelable(false);
    		pd.setCanceledOnTouchOutside(false);
    		pd.show();
	        
	        new Thread(new Runnable() {
	        	public void run() {
	        		adapter.loadObjects();
	        		gridView.post(new Runnable() {
	        			public void run() {
	        				gridView.setAdapter(adapter);
	        			}
	        		});
	        		pd.dismiss();
	        	}
	        }).start();
        }
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.main_action_transfer:
    		if (_mtpDevice != null) {
        		final ProgressDialog pd = new ProgressDialog(this);
        		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		pd.setCancelable(false);
        		pd.setCanceledOnTouchOutside(false);
        		pd.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
        		});
        		pd.setTitle("Copying files...");
        		pd.show();
        		new Thread(new Runnable() {
    				public void run() {
    					copyAllMtpObjects(pd);
    					pd.dismiss();
    				}
    			}).start();
    		}
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    
    private void copyAllMtpObjects(final ProgressDialog pd) {
    	if (_mtpDevice == null) {
    		return;
    	}
    	
    	int[] objectIds = _mtpDevice.getObjectHandles(_storageId, 0, 0);
    	
    	File rootDir = Environment.getExternalStorageDirectory();
    	File myDir = new File(rootDir.getPath().concat("/CameraBuddy"));
    	myDir.mkdir();
    	
    	for (int id : objectIds) {
    		final MtpObjectInfo info = _mtpDevice.getObjectInfo(id);
    		if (info != null && info.getAssociationType() != MtpConstants.ASSOCIATION_TYPE_GENERIC_FOLDER) {
    			final String newFile = myDir.getPath().concat("/").concat(info.getName());
    			runOnUiThread(new Runnable() {
    				public void run() {
    					pd.setMessage(info.getName());
    				}
    			});
    			_mtpDevice.importFile(id, newFile);
    		}
    	}
    }
}