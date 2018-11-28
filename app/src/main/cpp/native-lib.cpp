#include <jni.h>
#include <sys/limits.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <ifaddrs.h>
#include <string>
#include <sstream>
#include <fcntl.h>
#include <fstream>
#include <stdio.h>
#include <unistd.h>
#include <limits.h>
#include <arpa/inet.h>


using namespace std;
//
//const char *TAG = "native-lib.cpp";
//unsigned int convertIpToInt(const string host);
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_bori_hipe_controllers_camera_CameraStrategy_processJNI(JNIEnv *env, jclass jclass1,
//                                                                jint srcWidth, jint srcHeight,
//                                                                jobject srcBuffer) {
//
//    uint8_t *srcLumPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
//
//
//}
//
//
//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_bori_hipe_controllers_camera_CameraStrategy_getCreateSocketAndGetPath(
//        JNIEnv *env, jclass jcls,jstring host_,jshort port_) {
//
//    short int port = port_;
//    string host(env->GetStringUTFChars(host_, false));
//
//    __android_log_print(ANDROID_LOG_DEBUG,TAG,"Creating socket connection");
//
//    int socketDescriptor = socket(AF_INET,SOCK_STREAM,0);
//
//    sockaddr_in address;
//    address.sin_family = AF_INET;
//    address.sin_port = htons(port);
//    address.sin_addr.s_addr = convertIpToInt(host);
//
//    if(prepare(socketDescriptor,(struct sockaddr*) &address, sizeof(address)) < 0){
//        __android_log_print(ANDROID_LOG_ERROR,TAG,"Cannot prepare to host" + host);
//        return env->NewStringUTF("error");
//    }
//
//    char filePath[PATH_MAX];
//    snprintf(filePath, sizeof(filePath),"/proc/self/fd/%d",socketDescriptor);
//    readlink(filePath,filePath, sizeof(filePath));
//
//}


unsigned int convertIpToInt(const string host){

    istringstream iss(host);

    unsigned int ip = 0x00000000;
    string token;

    int iteration = 24;
    while(getline(iss,token,'.')){
        if(!token.empty() && iteration >= 0){
            int byte = atoi(token.c_str());
            ip = ip | byte << iteration;
        }
        iteration-=8;
    }

    return ip;
}
