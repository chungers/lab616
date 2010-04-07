# CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

project(IBClient)

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

option(OPTIMIZE "Compiler optimization" ON)

set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

# Use 9.63 Beta version API.
set(IBAPI_DIR "api9.63beta")

# Use the api version defined in the top level cmake file:
set(API_ROOT ${SRC_ROOT}/${IBAPI_DIR})

# Include the subdirectories required for this project:	 
add_subdirectory(src)
add_subdirectory(test)

