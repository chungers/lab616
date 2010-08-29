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
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/PosixSocketClient
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${LIBSIGC_PATH}
)
set(v964_adapter_srcs
  bridge.hpp
  bridge.cpp
  adapters.hpp
  adapters.cpp
  backplane.hpp
  backplane.cpp
  helpers.hpp
  marketdata.cpp
  polling_client.hpp
  polling_client.cpp
  services.hpp
  session.hpp
  session.cpp
  ticker_id.cpp
)
set(v964_adapter_libs
  ib_api
  ib_common_proto
  ib_events_proto
  ib_actions_proto
  protobuf
)
# Client implementation:
cpp_library(v964_adapter)

# Include subdirectories here:
add_subdirectory(api)
add_subdirectory(logger)
add_subdirectory(logreader)
add_subdirectory(util)
