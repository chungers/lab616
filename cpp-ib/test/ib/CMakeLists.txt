# //cpp-ib/test/ib/CMakeLists.txt

build_tests()

#########################################
# Test:
set(all_tests_incs
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
  ${LIBSIGC_PATH}
)
set(all_tests_srcs
  AllTests.cpp
  adapter_test.cpp
  backplane_test.cpp
  helpers_test.cpp
)
set(all_tests_libs
  boost_thread
  v964_adapter
  gflags
  glog
  sigc-2.0
)
cpp_gtest(all_tests)

#########################################
# Test:
set(backplane_test_incs
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
  ${LIBSIGC_PATH}
)
set(backplane_test_srcs
  AllTests.cpp
  backplane_test.cpp
)
set(backplane_test_libs
  boost_thread
  v964_adapter
  gflags
  glog
  sigc-2.0
)
cpp_gtest(backplane_test)

#########################################
# Test:
set(utils_test_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
  ${LIBSIGC_PATH}
)
set(utils_test_srcs
  AllTests.cpp
  utils_test.cpp
)
set(utils_test_libs
  boost_thread
  gflags
  glog
)
cpp_gtest(utils_test)

#########################################
# Prototype:
set(ib_prototype_incs
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
  ${THIRD_PARTY_PATH}
  ${LIBSIGC_PATH}
  ${LIBFASTFLOW_PATH}
)
set(ib_prototype_srcs
  AllTests.cpp
  fastflow_prototype.cpp
  signals_prototype.cpp
)
set(ib_prototype_libs
  boost_system
  boost_thread
  pthread
  v964_adapter
  gflags
  glog
  sigc-2.0
)
cpp_gtest(ib_prototype)
