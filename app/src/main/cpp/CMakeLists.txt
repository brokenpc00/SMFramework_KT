# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

set(LOCAL_PATH ${CMAKE_CURRENT_SOURCE_DIR})

set(APP_NAME SMFramework_ImgPrc)

project(${APP_NAME})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(LIB_SOURCE)
set(LIB_HEADER)

list(APPEND LIB_SOURCE
        SMFramework_ImgPrc.CPP

        )
list(APPEND LIB_HEADER

)


set(all_code_files
        ${LIB_HEADER}
        ${LIB_SOURCE}
        )

add_library(${APP_NAME} SHARED ${all_code_files})
#target_link_libraries(${APP_NAME} -Wl,--whole-archive cpp_android_spec -Wl,--no-whole-archive)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

find_library(jnigraphics-lib jnigraphics)

find_library(OPENGLES2_LIBRARY libGLESv2)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                        ${APP_NAME}
                        -ljnigraphics
                        -lGLESv2
                        -landroid
                        -lz
                        -lm

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )


target_include_directories(${APP_NAME}
        PRIVATE ${LOCAL_PATH}/libwebp/src/)