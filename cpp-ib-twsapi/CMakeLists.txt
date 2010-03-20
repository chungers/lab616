# CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

project(IBClient)

option(OPTIMIZE "Compiler optimization" ON)

set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

# Use 9.63 Beta version API.
set(IBAPI_DIR "api9.63beta")

# Build directory contains the output of builds.
set(BUILD_DIR ${PROJECT_SOURCE_DIR}/build)
set(INCLUDE_DIR ${PROJECT_SOURCE_DIR}/include)

# Directory for generated files:
set(GEN_DIR ${PROJECT_SOURCE_DIR}/genfiles)

# Compiler flags
set(CMAKE_CXX_FLAGS "-ggdb -Wall -Wno-switch")

if (OPTIMIZE) 
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2")
endif (OPTIMIZE)
		
# Include the subdirectories required for this project:	 
add_subdirectory(src)
#add_subdirectory(test)