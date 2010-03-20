# CMakeLists.txt
# Directory: //src

cmake_minimum_required(VERSION 2.6)

include(${PROJECT_SOURCE_DIR}/CMakeLists.macros)

# Use the api version defined in the top level cmake file:
set(API_ROOT ./${IBAPI_DIR})

# Use standard string library instead of mvc foundation classes:
option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
if (IB_USE_STD_STRING)
  add_definitions('-DIB_USE_STD_STRING')
endif (IB_USE_STD_STRING)

# For -I includes
include_directories(
  .
  ${API_ROOT}/Shared 
  ${API_ROOT}/PosixSocketClient
  ${GEN_DIR}/client
)

# Process variables in headers with values set in top level cmake file:
configure_file(
  client/SimpleClient.h.in
  ${GEN_DIR}/client/SimpleClient.h
)
install(FILES ${GEN_DIR}/client/SimpleClient.h DESTINATION ${INCLUDE_DIR})

# Core classes in the IB api:
add_library(twsclient 
  ${API_ROOT}/PosixSocketClient/EClientSocketBase.cpp
  ${API_ROOT}/PosixSocketClient/EPosixClientSocket.cpp
)

########################################################
# Executable: SimpleClientMain
#
set(SimpleClientMain_srcs 
  client/SimpleClient.cpp
  client/SimpleClientMain.cpp
)
set(SimpleClientMain_libs
  twsclient
)
runlint(SimpleClientMain)
build_executable(SimpleClientMain)

