# CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

project(IBClient)

set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

# Use 9.63 Beta version API.
set(IBAPI_DIR "api9.63beta")

set(SRC_ROOT ${PROJECT_SOURCE_DIR}/src)
set(GEN_ROOT ${PROJECT_SOURCE_DIR}/genfiles)
set(API_ROOT ${SRC_ROOT}/${IBAPI_DIR})

add_definitions(-Wall -W -ggdb)

# Pass CMake settings to files below:
configure_file(
  "${SRC_ROOT}/client/SimpleClient.h.in"
  "${GEN_ROOT}/client/SimpleClient.h"
)

include_directories("${GEN_ROOT}" 
		    "${SRC_ROOT}" 
                    "${GEN_ROOT}/client" 
                    "${SRC_ROOT}/client" 
		    "${API_ROOT}/Shared" 
		    "${API_ROOT}/PosixSocketClient")

option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
			 
if (IB_USE_STD_STRING)
  set_source_files_properties(
    ${SRC_ROOT}/client/SimpleClientMain.cpp
    PROPERTIES
    COMPILE_FLAGS -DIB_USE_STD_STRING)
endif (IB_USE_STD_STRING)

add_subdirectory(${SRC_ROOT})

add_executable(SimpleClientMain ${SRC_ROOT}/client/SimpleClientMain.cpp)

install(TARGETS SimpleClientMain DESTINATION bin)
install(FILES SimpleClient.h DESTINATION include)
