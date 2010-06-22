# //cpp-ib/src/ib/CMakeLists.txt
#
# Referenced inside source files as ifdef constants.
set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)
set(BRIDGE_VERSION 9.64beta)

# Configure constants
configure_file(
  bridge_constants.h.in
  ${GEN_DIR}/ib/bridge_constants.h
)

# The macros here will first find any locally installed
# libraries.  If not, it will build them in build/third_party.
# Consider install these on the local host to ensure proper
# function and more efficient builds.
third_party(gflags)
set(glog_deps gflags)
third_party(glog)


# Generate protos
proto_library(ib_common_proto ib_common.proto ${SRC_DIR}/ib)
proto_library(ib_events_proto ib_events.proto ${SRC_DIR}/ib)
proto_library(ib_actions_proto ib_actions.proto ${SRC_DIR}/ib)

add_dependencies(ib_events_proto ib_common_proto)
add_dependencies(ib_actions_proto ib_common_proto)

set(v964_adapter_incs
  ${SRC_DIR}/ib
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
)
set(v964_adapter_srcs
  bridge.hpp
  bridge.cpp
  adapters.hpp
  adapters.cpp
  thread_client.cpp
)
set(v964_adapter_libs
  ib_api
  ib_common_proto
  ib_events_proto
  ib_actions_proto
  gflags
)
# Client implementation:
cpp_library(v964_adapter)

######################
set(cl_incs
  ${SRC_DIR}/ib
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
)
set(cl_srcs
  thread_client.cpp
)
set(cl_libs
  v964_adapter
  boost_thread
  glog
)
# Client implementation:
cpp_executable(cl)

# Include subdirectories here:
add_subdirectory(api) # For IB provided source files.
add_subdirectory(client)
add_subdirectory(util) # For prototyping, testing, command utilities.
