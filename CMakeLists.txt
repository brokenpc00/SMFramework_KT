make_minimum_required(VERSION 3.4.1)

set(APP_NAME SMFramework_ImgPrc)

project(${APP_NAME})

set(CPP_SOURCE)
set(CPP_HEADER)

list(APPEND CPP_SOURCE
            src/main/jni/SMFramework_ImgProc.cpp
            )

list(APPEND CPP_HEADER
            app/src/main/jni/SMFramework_ImgProc.h
            app/src/main/jni/SMFramework_ImgProc_Def.h
            )


set(all_code_files
    ${CPP_HEADER}
    ${CPP_SOURCE}
    )

add_library(${APP_NAME} SHARED ${all_code_files})

include_directories(app/src/main/jni/)
