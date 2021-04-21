//
// Created by N15051 on 2020/07/08.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/api-level.h>

#include <android/bitmap.h>
#include <GLES2/gl2.h>

#include <cstdio>
#include <cstdlib>
#include <cmath>


typedef uint8_t BYTE;
typedef uint32_t DWORD;
typedef uint8_t BOOL;

#define TRUE  (1)
#define FALSE (0)

#define GetRValue(p) ((p)&0x000000FF)
#define GetGValue(p) ((p)&0x0000FF00)>>8
#define GetBValue(p) ((p)&0x00FF0000)>>16
#define GetAValue(p) ((p)&0xFF000000)>>24

#define GET_R_VALUE(p) ((p)&0x000000FF)
#define GET_G_VALUE(p) (((p)&0x0000FF00)>>8)
#define GET_B_VALUE(p) (((p)&0x00FF0000)>>16)
#define GET_A_VALUE(p) (((p)&0xFF000000)>>24)
#define MAKE_RGB_VALUE(r,g,b) (0xFF000000|(b<<16)|(g<<8)|r)
#define MAKE_RGBA_VALUE(r,g,b,a) ((a<<24)|(b<<16)|(g<<8)|r)
#define SET_ALPHA_VALUE(pixel, a) ((a<<24)|(pixel&0x00FFFFFF))

#define SAFE(x) (((x) < 0) ? 0 : (((x) > 255) ? 255 : (x)))
#define CLAMP(v)(BYTE) (((v) >= 255) ? 255 : (((v) < 0) ? 0 : (v)))

#define _MAX(a, b) (((a)>(b))?(a):(b))
#define _MIN(a, b) (((a)<(b))?(a):(b))

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define  LOG_TAG    "SMFramework_ImgProc"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

extern "C" JNIEXPORT
jstring JNICALL Java_com_brokenpc_smframework_nativeImageProcess_ImageProcessing_stringFromJNI(
        JNIEnv *env,
        jclass thiz
)
{
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT
void Java_com_brokenpc_smframework_nativeImageProcess_ImageProcessing_callTest(
        JNIEnv *env,
        jclass thiz
)
{
    LOGD("[[[[[ callTest!!!!");
}

extern "C" JNIEXPORT
void Java_com_brokenpc_smframework_nativeImageProcess_ImageProcessing_exitApp(
        JNIEnv *env,
        jclass thiz
)
{
    exit(0);
}


//extern "C" JNIEXPORT
//void JNICALL Java_com_brokenpc_smframework_nativeImageProcess_ImageProcessing_glGrabPixels(
//        JNIEnv *env,
//        jclass obj,
//        jint x,
//        jint y,
//        jobject bitmap,
//        jboolean zeroNonVisiblePixels
//)
//{
////    LOGD("[[[[[ glGrabPixels 1!!!!");
//
//    AndroidBitmapInfo info;
//    uint32_t  * pixels = nullptr;
//
//    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
//        // failed to get bitmap info
////        LOGE("AndroidBitmap_getInfo(env, bitmap, &info) failed !!!");
//        return;
//    }
//
//    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
//        // made bitmap... for RGBA8888... but...?
//        return;
//    }
//
//    if (AndroidBitmap_lockPixels(env, bitmap, (void**)&pixels) < 0) {
////        LOGE("AndroidBitmap_lockPixels() failed !!!");
//        return;
//    }
//
////    LOGD("[[[[[ glGrabPixels 2!!!!");
//
//    glReadPixels(x, y, info.width, info.height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
//
//    if (zeroNonVisiblePixels) {
//        int length = info.width * info.height;
//        for (int i = 0; i < length; ++i) {
//            if (!(*pixels & 0xFF000000)) {
//                *pixels = 0;
//            }
//            pixels++;
//        }
//    }
//
////    LOGD("[[[[[ glGrabPixels 3 width : %d, height %d", info.width, info.height);
//    AndroidBitmap_unlockPixels(env, bitmap);
//}


//JNIEXPORT
//jbyteArray JNICALL Java_com_brokenpc_smframework_nativeImageProcess_ImageProcessing_decodeRGBAnative( JNIEnv *jenv, jclass jcls,
//		jbyteArray jencoded, jlong jencodedLength,
//		jintArray jwidth, jintArray jheight)
//{
//	jbyteArray jresult = 0 ;
//	uint8_t *encoded = (uint8_t *) 0 ;
//	size_t encodedLength ;
//	int *width = (int *) 0 ;
//	int *height = (int *) 0 ;
//	jbyte *jarr1 ;
//	int temp3 ;
//	int temp4 ;
//	uint8_t *result = 0 ;
//
//    return 0;
//
//
//	if (!SWIG_JavaArrayInSchar(jenv, &jarr1, &encoded, jencoded)) return 0;
//	encodedLength = (size_t)jencodedLength;
//	{
//		if (!jwidth) {
//			ThrowException(jenv, SWIG_JavaNullPointerException, "array null");
//			return 0;
//		}
//		if ((*jenv)->GetArrayLength(jenv, jwidth) == 0) {
//			ThrowException(jenv, SWIG_JavaIndexOutOfBoundsException, "Array must contain at least 1 element");
//			return 0;
//		}
//		width = &temp3;
//	}
//	{
//		if (!jheight) {
//			ThrowException(jenv, SWIG_JavaNullPointerException, "array null");
//			return 0;
//		}
//		if ((*jenv)->GetArrayLength(jenv, jheight) == 0) {
//			ThrowException(jenv, SWIG_JavaIndexOutOfBoundsException, "Array must contain at least 1 element");
//			return 0;
//		}
//		height = &temp4;
//	}
//	result = (uint8_t *)WebPDecodeARGB((uint8_t const *)encoded,encodedLength,width,height);
//	jresult = SWIG_JavaArrayOutSchar(jenv, result, result ? (*width) * (*height) * 4 : 0);
//	SWIG_JavaArrayArgoutSchar(jenv, jarr1, encoded, jencoded);
//	{
//		jint jvalue = (jint)temp3;
//		(*jenv)->SetIntArrayRegion(jenv, jwidth, 0, 1, &jvalue);
//	}
//	{
//		jint jvalue = (jint)temp4;
//		(*jenv)->SetIntArrayRegion(jenv, jheight, 0, 1, &jvalue);
//	}
//	free(encoded);
//
//	free(result);
//	return jresult;
//
//	return 0;
//}