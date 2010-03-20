# CMakeLists.txt
# Directory: //src

cmake_minimum_required(VERSION 2.6)

# Use the api version defined in the top level cmake file:
set(API_ROOT ./${IBAPI_DIR})

# Process variables in headers with values set in top level cmake file:
configure_file(
  client/SimpleClient.h.in
  ${GEN_DIR}/client/SimpleClient.h
)

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

# Core classes in the IB api:
add_library(twsclient 
  ${API_ROOT}/PosixSocketClient/EClientSocketBase.cpp
  ${API_ROOT}/PosixSocketClient/EPosixClientSocket.cpp
)

# Executable: a simple client.
add_executable(SimpleClientMain 
  client/SimpleClient.cpp
  client/SimpleClientMain.cpp
)

target_link_libraries(SimpleClientMain twsclient)

# Finally install the final artifacts.
# This installs to /usr/local/bin and /usr/local/include
install(TARGETS SimpleClientMain DESTINATION ${BUILD_DIR})
install(FILES ${GEN_DIR}/client/SimpleClient.h DESTINATION ${INCLUDE_DIR})
