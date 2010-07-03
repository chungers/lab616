# //cpp-ib/src/ib/api/CMakeLists.txt
# Build file for the IB api library.

# Use standard string library instead of mvc foundation classes:
option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
if (IB_USE_STD_STRING)
  add_definitions('-DIB_USE_STD_STRING')
endif (IB_USE_STD_STRING)

# Core classes in the IB api:
include_directories(${API_ROOT}/Shared ${API_ROOT}/PosixSocketClient)
ext_library(ib_api
  ${API_ROOT}/PosixSocketClient/EClientSocketBase.cpp
  ${API_ROOT}/PosixSocketClient/EPosixClientSocket.cpp
)
