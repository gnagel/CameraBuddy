package com.peterdn.camerabuddy;

import android.content.Context;
import android.mtp.MtpDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MtpObjectAdapter extends android.widget.BaseAdapter {

	private Context _context;
	private MtpDevice _mtpDevice;
	private int _storageId;
	
	private int[] _objectHandles;
	
	private String[] _names;
	
	public MtpObjectAdapter(Context context, MtpDevice mtpDevice, int storageId) {
		this._context = context;
		this._mtpDevice = mtpDevice;
		this._storageId = storageId;
	}
	
	public void loadObjects() {
		this._objectHandles = this._mtpDevice.getObjectHandles(this._storageId, 0, 0);
		this._names = new String[this._objectHandles.length];
		for (int i = 0; i < this._objectHandles.length; ++i) {
			this._names[i] = this._mtpDevice.getObjectInfo(this._objectHandles[i]).getName();
		}
	}
	
	@Override
	public int getCount() {
		return this._objectHandles.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if (arg1 == null) {
			TextView textView = new TextView(this._context);
			textView.setText(this._names[arg0]);
			return (View) textView;
		} else {
			return arg1;
		}
	}

}
