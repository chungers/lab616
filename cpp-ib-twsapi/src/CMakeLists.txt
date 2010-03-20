# CMakeLists.txt
# Directory: //src

cmake_minimum_required(VERSION 2.6)

set(API_ROOT ./api9.63beta)

#option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
add_definitions('-DIB_USE_STD_STRING')

# For -I includes
include_directories("${API_ROOT}/Shared" "${API_ROOT}/PosixSocketClient")

add_library(twsclient 
  ${API_ROOT}/PosixSocketClient/EClientSocketBase.cpp
  ${API_ROOT}/PosixSocketClient/EPosixClientSocket.cpp
)

