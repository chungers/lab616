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
  ${LIBSIGC_PATH}
)
set(adapter_test_srcs
  AllTests.cpp
  adapter_test.cpp
  helpers_test.cpp
  backplane_test.cpp
)
set(adapter_test_libs
  boost_thread
  v964_adapter
  glog
  sigc-2.0
)
cpp_gtest(adapter_test)

#########################################
# Test:
set(prototype_incs
  ${SRC_DIR}/ib/api/9.64beta
  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${TEST_DIR}
  ${LIBSIGC_PATH}
)
set(prototype_srcs
  AllTests.cpp
  signals_prototype.cpp
)
set(prototype_libs
  boost_thread
  v964_adapter
  glog
  sigc-2.0
)
cpp_gtest(prototype)
