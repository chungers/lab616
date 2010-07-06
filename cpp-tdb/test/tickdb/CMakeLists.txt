build_tests()

#########################################
# Test:
set(tickdb_test_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
)
set(tickdb_test_srcs
  AllTests.cpp
  utils_test.cpp
)
set(tickdb_test_libs
  tickdb_format
  glog
  gflags
  protobuf
  kyotocabinet
)
cpp_gtest(tickdb_test)
