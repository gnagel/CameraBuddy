#include <libraw/libraw.h>

#include <string.h>
#include <jni.h>

jstring Java_org_libraw_LibRaw_version(JNIEnv* env, jclass clazz) {
	return (*env)->NewStringUTF(env, libraw_version());
}