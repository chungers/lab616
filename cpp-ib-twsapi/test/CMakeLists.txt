# CMakeLists.txt for tests

include(${PROJECT_MACROS})
enable_testing()

# Use standard string library instead of mvc foundation classes:
option(IB_USE_STD_STRING "Use standard c++ string (see Shared/IBString.h)." ON)
if (IB_USE_STD_STRING)
  add_definitions('-DIB_USE_STD_STRING')
endif (IB_USE_STD_STRING)

#########################################
# Test:
set(SimpleClientTest_incs
  ${GEN_DIR}
  ${SRC_ROOT}
  ${API_ROOT}
  ${API_ROOT}/Shared
  ${TEST_ROOT}
  ${TEST_INCLUDES}
)
set(SimpleClientTest_srcs
  AllTests.cpp
  client/ClientTest.cpp
  client/SimpleClientTest.cpp
)
set(SimpleClientTest_libs
  twsapi
  twsclient
  gtest
)
build_gtest(SimpleClientTest)

		
