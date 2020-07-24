//
// Created by N15051 on 2020/07/08.
//

#ifndef APP_SMFRAMEWORK_IMGPRC_DEF_H
#define APP_SMFRAMEWORK_IMGPRC_DEF_H
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <android/log.h>

#define DEBUG 0

#define PI 3.14159265

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
#endif //APP_SMFRAMEWORK_IMGPRC_DEF_H
