# //cpp-ib/src/ib/CMakeLists.txt
#

# Referenced inside source files as ifdef constants.
set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

# Third party libraries used.
boost_libraries(date_time thread)
third_party(gflags)
third_party(glog)

set(V964Bridge_incs
  api/964beta/Shared
  api/964beta/PosixSocketClient
  ${GEN_DIR}
  ${SRC_DIR}
)
set(V964Bridge_srcs
  bridge_v964.cpp
)
set(V964Bridge_libs
  ib_api
  gflags
)
# Client implementation:
cpp_library(V964Bridge)


# Include subdirectories here:
add_subdirectory(api)
add_subdirectory(client)
