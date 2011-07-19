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

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MtpObjectLoader {

	private MtpDevice _mtpDevice;
	
	private Map<View, Integer> _objectViews;
	
	private Queue<View> _objectQueue;
	
	private Object _sync;
	
	private MtpObjectCollection _objects;
	
	public MtpObjectLoader(MtpDevice mtpDevice, int storageId, MtpObjectCollection objects) {
		_mtpDevice = mtpDevice;
		_objectViews = Collections.synchronizedMap(new WeakHashMap<View, Integer>());
		_sync = new Object();
		_objectQueue = new LinkedList<View>();
		_objects = objects;
		
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						if (_objectQueue.isEmpty()) {
							synchronized(_sync) {
								_sync.wait();
							}
						}
						if (!_objectQueue.isEmpty()) {
							View view;
							synchronized(_sync) {
								view = _objectQueue.remove();
							}
							int object = _objectViews.get(view);
							MtpObjectInfo info = _mtpDevice.getObjectInfo(object);
							String name = info.getName();
							int format = info.getThumbFormat();
							TextView tv = (TextView) view.findViewById(R.id.mtp_object_view_label);
							tv.post(new UpdatePost(tv, name));
							ImageView iv = (ImageView) view.findViewById(R.id.mtp_object_view_image);
							if (format != 0) {
								byte[] thumbnail = _mtpDevice.getThumbnail(object);
								iv.post(new UpdatePost(iv, thumbnail));
							} else {
								iv.post(new UpdatePost(iv, R.drawable.icon));
							}
							
							_objects.add(object, name);
						}
						if (Thread.interrupted()) {
							break;
						}
					}
				} catch (InterruptedException ex) {
					return;
				}
			}
		}).start();
	}

	public void loadObject(int i, View view) {
		_objectViews.put(view, i);
		ImageView imageView = (ImageView) view.findViewById(R.id.mtp_object_view_image);
		TextView textView = (TextView) view.findViewById(R.id.mtp_object_view_label);
		Bitmap thumbnail;
		String text;
		if (!_objects.contains(i)) {
			synchronized(_sync) {
				_objectQueue.add(view);
				_sync.notifyAll();
			}
			thumbnail = null;
			text = "";
		} else {
			MtpObjectCollection.MtpObjectEntry entry = _objects.getFromHandle(i);
			thumbnail = entry.getBitmap();
			text = entry.getName();
		}
		textView.setText(text);
		imageView.setImageBitmap(thumbnail);
	}
	
	private class UpdatePost implements Runnable {
		private String _text;
		private TextView _textView;
		private ImageView _imageView;
		private byte[] _data;
		private int _icon;
		
		public UpdatePost(TextView textView, String text) {
			_textView = textView;
			_text = text;
		}
		
		public UpdatePost(ImageView imageView, byte[] data) {
			_data = data;
			_imageView = imageView;
		}
		
		public UpdatePost(ImageView imageView, int icon) {
			_imageView = imageView;
			_icon = icon;
		}

		public void run() {
			if (_textView != null) {
				_textView.setText(_text);
			} else if (_imageView != null) {
				if (_data != null) {
					Bitmap bmp = BitmapFactory.decodeByteArray(_data, 0, _data.length);
					_imageView.setImageBitmap(bmp);
				} else {
					_imageView.setImageResource(_icon);
				}
			}
		}
	}
}
