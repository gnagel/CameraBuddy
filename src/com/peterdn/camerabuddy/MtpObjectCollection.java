package com.peterdn.camerabuddy;

import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;

public class MtpObjectCollection {
	
	private Map<Integer, MtpObjectEntry> _objects = new LinkedHashMap<Integer, MtpObjectEntry>();	
	
	public MtpObjectCollection() {
		
	}
	
	public class MtpObjectEntry {
		private int _handle;
		private String _name;
		private Bitmap _bitmap;
		
		public MtpObjectEntry(int handle, String name) {
			this(handle, name, null);
		}
		
		public MtpObjectEntry(int handle, String name, Bitmap bitmap) {
			_handle = handle;
			_name = name;
			_bitmap = bitmap;
		}
		
		public int getHandle() { return _handle; }
		public String getName() { return _name; }
		public Bitmap getBitmap() { return _bitmap; }
	}

	public boolean contains(int i) {
		return _objects.containsKey((Integer) i);
	}

	public MtpObjectEntry getFromHandle(int i) {
		return _objects.get((Integer) i);
	}

	public void add(int object, String name) {
		MtpObjectEntry entry = new MtpObjectEntry(object, name);
		_objects.put((Integer) object, entry);
	}

	
}
