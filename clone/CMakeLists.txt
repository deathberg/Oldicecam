cmake_minimum_required(VERSION 3.22)
project(libvc_clone LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Requires Android NDK with platform libraries (binder, utils, log, dl).
# Link against same API level as target device (API 33+ tested).

set(CLONE_INCLUDE ${CMAKE_CURRENT_SOURCE_DIR}/include)

# ─── libvc_clone.so — deploy as /data/libvc.so ─────────────────────────────
add_library(libvc_clone SHARED
    native/libvc_inject.cpp
    native/FrameInjectQueue.cpp
)

target_include_directories(libvc_clone PRIVATE ${CLONE_INCLUDE})

target_link_libraries(libvc_clone
    log
    dl
)

set_target_properties(libvc_clone PROPERTIES
    OUTPUT_NAME "libvc_clone"
    CXX_VISIBILITY_PRESET default
)

# ─── vcplax_clone — deploy as /data/vcplax ─────────────────────────────────
add_executable(vcplax_clone
    native/libvc_clone.cpp
    native/media_pipeline.cpp
    native/vc_clone_loader.cpp
)

target_include_directories(vcplax_clone PRIVATE ${CLONE_INCLUDE})

target_link_libraries(vcplax_clone
    binder
    utils
    log
    dl
)

# Optional: copy artifacts next to build dir for adb push
add_custom_command(TARGET vcplax_clone POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        $<TARGET_FILE:libvc_clone>
        $<TARGET_FILE_DIR:vcplax_clone>/libvc_clone.so
    COMMENT "Copy libvc_clone.so beside vcplax_clone"
)
