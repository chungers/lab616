# CMakeLists.txt for tests

include(${PROJECT_MACROS})
enable_testing()

# For -I includes
include_directories(
  ${GEN_DIR}
  ${API_ROOT}/Shared
  ${TEST_INCLUDES}
)


# Use standard string library instead of mvc foundation classes:
option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
if (IB_USE_STD_STRING)
  add_definitions('-DIB_USE_STD_STRING')
endif (IB_USE_STD_STRING)

add_library(client_tests 
  SimpleClientTest.cpp
)

set(SimpleClientTest_srcs
  AllTests.cpp
  ClientTest.cpp
  SimpleClientTest.cpp
)
set(SimpleClientTest_libs
  client_tests
  twsapi
  twsclient
  gtest
)
build_executable(SimpleClientTest)
add_test(Test1 ${TEST_ROOT}/SimpleClientTest)
		
