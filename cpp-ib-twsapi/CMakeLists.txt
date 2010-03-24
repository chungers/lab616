# CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

project(IBClient)

option(OPTIMIZE "Compiler optimization" ON)

set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

# cpplint.py tool
set(CPPLINT ${PROJECT_SOURCE_DIR}/../scripts/cpplint.py)

# Use 9.63 Beta version API.
set(IBAPI_DIR "api9.63beta")

# Compiler flags
set(CMAKE_CXX_FLAGS "-ggdb -Wall -Wno-switch")
if (OPTIMIZE) 
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2")
endif (OPTIMIZE)

# Macros to include
set(PROJECT_MACROS ${PROJECT_SOURCE_DIR}/CMakeLists.macros)
include(${PROJECT_MACROS})

# Build directory contains the output of builds.
set(BUILD_DIR ${PROJECT_SOURCE_DIR}/build)
set(INCLUDE_DIR ${BUILD_DIR}/include)

# Testing support - GTest / GMock
set(GMOCK_DIR ${PROJECT_SOURCE_DIR}/../third_party/cpp/gmock)
set(GTEST_DIR ${PROJECT_SOURCE_DIR}/../third_party/cpp/gtest)
set(TEST_INCLUDES 
  ${GTEST_DIR}
  ${GTEST_DIR}/include
  ${GMOCK_DIR}
  ${GMOCK_DIR}/include
)
include_directories(${TEST_INCLUDES})

# GoogleTest
build_static_lib(gtest
    ${GTEST_DIR}/src/gtest.cc
    ${GTEST_DIR}/src/gtest-death-test.cc
    ${GTEST_DIR}/src/gtest-filepath.cc
    ${GTEST_DIR}/src/gtest-port.cc
    ${GTEST_DIR}/src/gtest-test-part.cc
    ${GTEST_DIR}/src/gtest-typed-test.cc
)

# Directory for generated files:
set(GEN_DIR ${PROJECT_SOURCE_DIR}/genfiles)

# Root of source and tests
set(SRC_ROOT ${PROJECT_SOURCE_DIR}/src)
set(TEST_ROOT ${PROJECT_SOURCE_DIR}/test)

# Use the api version defined in the top level cmake file:
set(API_ROOT ${SRC_ROOT}/${IBAPI_DIR})

# Include the subdirectories required for this project:	 
add_subdirectory(src)
add_subdirectory(test)

