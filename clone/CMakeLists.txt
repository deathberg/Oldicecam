cmake_minimum_required(VERSION 3.22)
project(libvc_clone LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Requires Android NDK with platform libraries (binder, utils, log).
# Link against same API level as target device (API 33+ tested).

add_executable(vcplax_clone native/libvc_clone.cpp)

target_include_directories(vcplax_clone PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)

target_link_libraries(vcplax_clone
    binder
    utils
    log
)

# Future: split injection layer
# add_library(libvc_clone SHARED native/libvc_inject.cpp)
# target_link_libraries(libvc_clone shadowhook mediandk)
