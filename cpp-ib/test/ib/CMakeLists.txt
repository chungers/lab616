# //cpp-ib/test/ib/CMakeLists.txt

build_tests()

#########################################
# Test:
set(adapter_test_incs
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
)
set(adapter_test_srcs
  AllTests.cpp
  adapter_test.cpp
  helpers_test.cpp
  backplane_test.cpp
)
set(adapter_test_libs
  v964_adapter
  glog
)
cpp_gtest(adapter_test)
