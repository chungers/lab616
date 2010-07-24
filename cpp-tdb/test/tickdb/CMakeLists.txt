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
  prototype.cpp
)
set(tickdb_test_libs
  tickdb
  glog
  gflags
  protobuf
  kyotocabinet
  boost_system
  boost_filesystem
)
cpp_gtest(tickdb_test)
