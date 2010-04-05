# CMakeLists.txt
# Directory: //src

cmake_minimum_required(VERSION 2.6)

include(${PROJECT_MACROS})

# Use standard string library instead of mvc foundation classes:
option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
if (IB_USE_STD_STRING)
  add_definitions('-DIB_USE_STD_STRING')
endif (IB_USE_STD_STRING)

# Process variables in headers with values set in top level cmake file:
configure_file(
  client/SimpleClient.h.in
  ${GEN_DIR}/client/SimpleClient.h
)
install(FILES ${GEN_DIR}/client/SimpleClient.h DESTINATION ${INCLUDE_DIR})

# Core classes in the IB api:
cpp_library(tws_socket_impl 
  ${API_ROOT}/PosixSocketClient/EClientSocketBase.cpp
  ${API_ROOT}/PosixSocketClient/EPosixClientSocket.cpp
)

# Client implementation:
add_library(twsclient
  client/SimpleClient.cpp
)

########################################################
# Executable: SimpleClientMain
# _incs / _srcs / _libs
set(SimpleClientMain_incs
  ${API_ROOT}/Shared 
  ${API_ROOT}/PosixSocketClient
  ${GEN_DIR}/client
)
set(SimpleClientMain_srcs
  client/SimpleClientMain.cpp
)
set(SimpleClientMain_libs
  tws_socket_impl
  twsclient
)
build_executable(SimpleClientMain)
