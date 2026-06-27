cmake_minimum_required(VERSION 3.22)
project(vcamera_native LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Reuse the clone injection library sources (NDK-buildable: only log + dl).
# The full vcplax daemon (binder/utils) requires an AOSP build and is NOT built
# here; it must be cross-compiled separately and dropped into jniLibs.
set(CLONE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../clone)

add_library(vc_clone SHARED
    ${CLONE_DIR}/native/libvc_inject.cpp
    ${CLONE_DIR}/native/FrameInjectQueue.cpp
)

target_include_directories(vc_clone PRIVATE ${CLONE_DIR}/include)
target_link_libraries(vc_clone log dl)

# Emit as libvc_clone.so (App.java copies it to /data/libvc.so on device).
set_target_properties(vc_clone PROPERTIES OUTPUT_NAME "vc_clone")
