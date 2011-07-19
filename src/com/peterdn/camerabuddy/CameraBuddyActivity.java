/*
 * Copyright (c) 2011, Peter Nelson (http://peterdn.com)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
*/

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
        
        this.setTheme(R.style.CameraBuddy_Dark);
                
        ActionBar bar = this.getActionBar();
        
        bar.show();
        
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        
        MtpObjectCollection objects = new MtpObjectCollection();
        
        if (!devices.isEmpty()) {        
	        UsbDevice usbDevice = devices.values().iterator().next();
	        UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
	        
	        _mtpDevice = new MtpDevice(usbDevice);
	        _mtpDevice.open(usbDeviceConnection);
	        
	        _storageId = (_mtpDevice.getStorageIds())[0];
	        
	        MtpObjectAdapter adapter = new MtpObjectAdapter(this, _mtpDevice, _storageId, objects);
	        GridView gridView = (GridView) findViewById(R.id.gridview);
	        gridView.setAdapter(adapter);
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