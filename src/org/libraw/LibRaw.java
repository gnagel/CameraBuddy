package org.libraw;

public class LibRaw {

	public static native String version();
	
	static {
		System.loadLibrary("raw");
	}
}
