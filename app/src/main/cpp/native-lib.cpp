#include <jni.h>
#include <android/log.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_bori_hipe_controllers_camera_CameraStrategy_processJNI(JNIEnv *env, jclass jcls, jint srcWidth, jint srcHeight,
                                                        jobject srcBuffer, jobject dstSurface){

    __android_log_print(ANDROID_LOG_DEBUG,"CPP","Loaded");
    return env->NewStringUTF("Hello from JNI");
}