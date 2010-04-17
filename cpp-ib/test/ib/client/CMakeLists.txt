# //cpp-ib/test/ib/client/CMakeLists.txt

build_tests()

#########################################
# Test:
set(SimpleClientTest_incs
  ${API_ROOT}/Shared 
  ${API_ROOT}/PosixSocketClient
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
)
set(SimpleClientTest_srcs
  AllTests.cpp
  ClientTest.cpp
  SimpleClientTest.cpp
)
set(SimpleClientTest_libs
  ib_api
  SimpleClient
)
cpp_gtest(SimpleClientTest)

		
