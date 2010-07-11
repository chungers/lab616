# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-ib
project(IB)

option(OPTIMIZE "Compiler optimization" ON)
#set(OPTIMIZE_LEVEL 2)

# Build for debugging and profiling
set(CFLAGS "-pg -g")

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

# IB API VERSION
# This corresponds to the name of the subdirectory in //cpp-ib/src/ib/api
set(IBAPI_VERSION "9.64beta")

list(APPEND includes
  /src/ib/api/${IBAPI_VERSION}
  /src/ib/api/${IBAPI_VERSION}/Shared
  /src/ib/api/${IBAPI_VERSION}/PosixSocketClient
)
emacs_ide_project("${includes}")

# Third party libraries used.
boost_libraries(date_time thread)

# Use the api version defined in the top level cmake file:
set(API_ROOT ${SRC_DIR}/ib/api/${IBAPI_VERSION})

# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(test/ib/mocks)
add_subdirectory(test/ib)



