# //cpp-ib/src/ib/util/CMakeLists.txt
#
# Client code using the IB API directly -- for testing of API, etc.

# Process variables in headers with values set in top level cmake file:
configure_file(
  constants.hpp.in
  ${GEN_DIR}/ib/util/constants.hpp
)

set(util_ib_client_incs
  ${API_ROOT}/Shared
  ${API_ROOT}/PosixSocketClient
  ${GEN_DIR}
  ${SRC_DIR}
)
set(util_ib_client_srcs
  print_wrapper.cpp
  ib_client.cpp
)
set(util_ib_client_libs
  ib_api
  gflags
)
# Client implementation:
cpp_library(util_ib_client)

########################################################
# Executable: ibcli
# _incs / _srcs / _libs
set(ibcli_incs
  ${API_ROOT}/Shared
  ${GEN_DIR}
  ${SRC_DIR}
)
set(ibcli_srcs
  command_line_client.cpp
)
set(ibcli_libs
  util_ib_client
  ib_api
  gflags
  glog
)
cpp_executable(ibcli)
