#include <jni.h>
#include <android/log.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <algorithm>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <android/bitmap.h>


using namespace std;

const char *TAG = "native-lib.cpp";

void transformMatrix(uint8_t *src, size_t destWidth, size_t destHeight);

inline int32_t toInt(int8_t value);

inline int32_t decode(uint8_t *src, uint32_t *dst, int32_t width, int32_t height);

inline int32_t max(int32_t value1, int32_t value2);

inline int32_t clamp(int32_t value, int32_t lowest, int32_t highest);

inline int32_t color(uint32_t colorR, uint32_t colorG, uint32_t colorB);

inline void postOnMatrix(uint8_t *src, uint32_t *dst, size_t width, size_t height);

inline void transformAndPost(uint8_t *src, uint32_t *dst, size_t width, size_t height);

extern "C"
JNIEXPORT void JNICALL
Java_com_bori_hipe_controllers_camera_CameraStrategy_processJNI(JNIEnv *env, jclass jcls,
                                                                jint srcWidth, jint srcHeight,
                                                                jobject srcBuffer,
                                                                jobject dstSurface) {

    uint8_t *srcLumPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    if (srcLumPtr == nullptr)
        __android_log_print(ANDROID_LOG_ERROR, TAG, "blit ponter is null");

    ANativeWindow *win = ANativeWindow_fromSurface(env, dstSurface);
    ANativeWindow_acquire(win);

    ANativeWindow_Buffer buffer;

    int dstWidth = srcHeight;
    int dstHeight = srcWidth;

    ANativeWindow_setBuffersGeometry(win, dstWidth, dstHeight, 0);

    if (int32_t err = ANativeWindow_lock(win, &buffer, NULL)) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "ANativeWindow_lock(win,&buffer,NULL) failed");
        ANativeWindow_release(win);
        return;
    }


//    transformMatrix(srcLumPtr, buffer.stride, dstHeight);
    uint32_t *dstLumaPtr = reinterpret_cast<uint32_t *>(buffer.bits);
//    decode(srcLumPtr,dstLumaPtr,dstWidth,dstHeight);

//    postOnMatrix(srcLumPtr,dstLumaPtr,dstWidth,dstHeight);

    transformAndPost(srcLumPtr,dstLumaPtr,dstWidth,dstHeight);

    ANativeWindow_unlockAndPost(win);
    ANativeWindow_release(win);

}

inline void transformMatrix(uint8_t *src, size_t destWidth, size_t destHeight) {
    __android_log_print(ANDROID_LOG_DEBUG,TAG,"starting transfroming matrix");

    if (src != nullptr) {
        uint8_t tmp = 0;
        for (int x = 0; x < destWidth; ++x) {
            for (int y = 0; y < destHeight; ++y) {
                tmp = src[destWidth * y + destWidth - x];
                src[destWidth * y + destWidth - x] = src[destHeight * x + y];
                src[destHeight * x + y] = tmp;
            }
        }

    } else {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "image source is null");
    }

    __android_log_print(ANDROID_LOG_DEBUG,TAG,"Ending transfroming method");

}

inline int32_t decode(uint8_t *src, uint32_t *dst, int32_t width, int32_t height) {

    __android_log_print(ANDROID_LOG_DEBUG,TAG,"starting decode method");

    int32_t frameSize = width * height;
    int32_t lYIndex, lUVIndex;
    int32_t lX, lY;
    int32_t lColorY, lColorU, lColorV;
    int32_t lColorR, lColorG, lColorB;
    int32_t y1192;

    for (lY = 0, lYIndex = 0; lY < height; ++lY) {
        lColorU = 0;
        lColorV = 0;

        lUVIndex = frameSize + (lY >> 1) * width;

        for (lX = 0; lX < width; ++lX, ++lYIndex) {

            lColorY = max(toInt(src[lYIndex]) - 16, 0);
            if (!(lX % 2)) {
                lColorV = toInt(src[lUVIndex++]) - 128;
                lColorU = toInt(src[lUVIndex++]) - 128;
            }

            y1192 = 1109 * lColorY;

            lColorR = (y1192 + 1634 * lColorV);
            lColorG = (y1192 - 833 * lColorV - 400 * lColorU);
            lColorB = (y1192 + 2066 * lColorU);

            lColorR = clamp(lColorR,0,262143);
            lColorG = clamp(lColorG,0,262143);
            lColorB = clamp(lColorB,0,262143);

            dst[lYIndex] = color(lColorR,lColorG,lColorB);
        }

    }

//    __android_log_print(ANDROID_LOG_DEBUG,TAG,"Ending decode method");
}

inline int32_t toInt(int8_t value) {
    return (0xff & (int32_t) value);
}

inline int32_t max(int32_t value1, int32_t value2) {
    return value1 > value2 ? value1 : value2;
}

inline int32_t clamp(int32_t value, int32_t lowest, int32_t highest) {
    return (value < 0) ? lowest : (value > highest ? highest : value);
}

inline int32_t color(uint32_t colorR, uint32_t colorG, uint32_t colorB) {
    return 0xff000000
           | ((colorB << 6) & 0x00ff0000)
           | ((colorG >> 2) & 0x0000ff00)
           | ((colorR >> 10) & 0x000000ff);
}

void postOnMatrix(uint8_t *src, uint32_t *dst, size_t width, size_t height) {

    if(src == nullptr || dst == nullptr)
        return;

    for (int i = 0; i < width*height; ++i) {
        dst[i] = src[i];
    }

}

void transformAndPost(uint8_t *src,uint32_t *dest, size_t destWidth, size_t destHeight) {

    if (src != nullptr){

        for (int x = 0; x < destWidth; ++x) {
            for (int y = 0; y < destHeight; ++y) {
                dest[destWidth*y + destWidth-x] = src[destHeight*x + y];
            }
        }

    } else {
        __android_log_print(ANDROID_LOG_ERROR,TAG,"image source is null");
    }


}
