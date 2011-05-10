# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-ib
project(IB)

option(OPTIMIZE "Compiler optimization" ON)
#set(OPTIMIZE_LEVEL 2)
# -O2 -fomit-frame-pointer for prod (no profiling)

# Build for debugging and profiling
# See http://gcc.gnu.org/onlinedocs/gcc/Option-Summary.html
#
set(CFLAGS "-pg -g -W")


# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

# IB API VERSION
# This corresponds to the name of the subdirectory in //cpp-ib/src/ib/api
set(IBAPI_VERSION "9.64")

list(APPEND emacs_includes
  /src/ib/api/${IBAPI_VERSION}
  /src/ib/api/${IBAPI_VERSION}/Shared
  /src/ib/api/${IBAPI_VERSION}/PosixSocketClient
)

set(LIBSIGC_PATH "/usr/local/include/sigc++-2.0")
set(THIRD_PARTY_PATH ${PROJECT_SOURCE_DIR}/../third_party/cpp)
set(LIBFASTFLOW_PATH ${PROJECT_SOURCE_DIR}/../third_party/cpp/fastflow)

list(APPEND emacs_sys_includes
  ${LIBSIGC_PATH}
  ${THIRD_PARTY_PATH}
  ${LIBFASTFLOW_PATH}
)
emacs_ide_project("${emacs_includes}" "${emacs_sys_includes}")

# Third party libraries used.
boost_libraries(date_time thread)

# Use the api version defined in the top level cmake file:
set(API_ROOT ${SRC_DIR}/ib/api/${IBAPI_VERSION})

# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(test/ib/mocks)
add_subdirectory(test/ib)
add_subdirectory(prototype)



