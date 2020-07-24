//
// Created by N15051 on 2020/07/08.
//
#include <jni.h>

#ifndef APP_SMFRAMEWORK_IMGPRC_H
#define APP_SMFRAMEWORK_IMGPRC_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT
jstring JNICALL Java_com_interpark_smframework_nativeImageProcess_ImageProcessing_stringFromJNI
        (JNIEnv *, jclass);

JNIEXPORT
void Java_com_interpark_smframework_nativeImageProcess_ImageProcessing_callTest
        (JNIEnv *, jclass);

JNIEXPORT
void Java_com_interpark_smframework_nativeImageProcess_ImageProcessing_exitApp
        (JNIEnv *, jclass);


JNIEXPORT
void JNICALL Java_com_interpark_smframework_nativeImageProcess_ImageProcessing_glGrabPixels
        (JNIEnv *, jclass, jint, jint, jobject, jboolean);

#ifdef __cplusplus
}
#endif
#endif //APP_SMFRAMEWORK_IMGPRC_H
