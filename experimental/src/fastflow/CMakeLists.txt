# //experimental/src/fastflow/CMakeLists.txt

third_party(gflags)
set(glog_deps gflags)
third_party(glog)

build_tests()

#########################################
# Prototype:
set(fastflow_prototype_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${THIRD_PARTY_PATH}
  ${LIBSIGC_PATH}
  ${LIBFASTFLOW_PATH}
)
set(fastflow_prototype_srcs
  AllTests.cpp
  fastflow_prototype.cpp
)
set(fastflow_prototype_libs
  data_protobuffers
  boost_system
  boost_thread
  protobuf
  pthread
  glog
  sigc-2.0
)
cpp_gtest(fastflow_prototype)
