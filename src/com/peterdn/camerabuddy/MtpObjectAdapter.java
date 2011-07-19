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

import android.content.Context;
import android.mtp.MtpDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MtpObjectAdapter extends android.widget.BaseAdapter {

	private static LayoutInflater _inflater;

	private Context _context;
	private MtpDevice _mtpDevice;
	private int _storageId;
	
	private int[] _objectHandles;
	
	private MtpObjectLoader _objectLoader;
	
	
	public MtpObjectAdapter(Context context, MtpDevice mtpDevice, int storageId, MtpObjectCollection objects) {
		_context = context;
		_mtpDevice = mtpDevice;
		_storageId = storageId;
		_objectHandles = _mtpDevice.getObjectHandles(_storageId, 0, 0);
		_objectLoader = new MtpObjectLoader(_mtpDevice, _storageId, objects);
	}
		
	@Override
	public int getCount() {
		return _objectHandles.length;
	}

	@Override
	public Object getItem(int arg0) {
		Log.d("getItem", "");
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		Log.d("getItemId", "");
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if (_inflater == null) {
			_inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		View view;
		if (arg1 == null) {
			view = _inflater.inflate(R.layout.mtp_object_view, null);
		} else {
			view = arg1;
		}
		
		_objectLoader.loadObject(_objectHandles[arg0], view);
		
		return view;
	}

}
