# //cpp-ib/src/ib/client/CMakeLists.txt
#
# Client code using the IB API directly.

# Referenced inside source files as ifdef constants.
set(IBClient_VERSION_MAJOR 0)
set(IBClient_VERSION_MINOR 1)

third_party(gflags)

# Process variables in headers with values set in top level cmake file:
configure_file(
  SimpleClient.h.in
  ${GEN_DIR}/ib/client/SimpleClient.h
)
configure_file(
  SimpleClient.cpp.in
  ${GEN_DIR}/ib/client/SimpleClient.cpp
)
install(FILES ${GEN_DIR}/ib/client/SimpleClient.h DESTINATION ${INCLUDE_DIR})


########################################################
# Executable: SimpleClientMain
# _incs / _srcs / _libs
set(SimpleClientMain_incs
  ${API_ROOT}/Shared 
  ${API_ROOT}/PosixSocketClient
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
)
# Client implementation:
add_library(SimpleClient
  ${GEN_DIR}/ib/client/SimpleClient.cpp
)
cpp_executable(SimpleClientMain)
