# //cpp-ib/src/ib/client/CMakeLists.txt
#
# Client code using the IB API directly -- for testing of API, etc.

# Process variables in headers with values set in top level cmake file:
configure_file(
  constants.h.in
  ${GEN_DIR}/ib/client/constants.h
)
#install(FILES ${GEN_DIR}/ib/client/SimpleClient.h DESTINATION ${INCLUDE_DIR})

set(SimpleClient_incs
  ${API_ROOT}/Shared 
  ${API_ROOT}/PosixSocketClient
  ${GEN_DIR}
  ${SRC_DIR}
)
set(SimpleClient_srcs
  SimpleClient.cpp
)
set(SimpleClient_libs
  ib_api
  gflags
)
# Client implementation:
cpp_library(SimpleClient)

########################################################
# Executable: SimpleClientMain
# _incs / _srcs / _libs
set(SimpleClientMain_incs
  ${API_ROOT}/Shared 
  ${GEN_DIR}
  ${SRC_DIR}
)
set(SimpleClientMain_srcs
  SimpleClientMain.cpp
)
set(SimpleClientMain_libs
  SimpleClient
  ib_api
  gflags
  glog
)
cpp_executable(SimpleClientMain)
