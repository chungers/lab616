# //experimental/src/tbb/CMakeLists.txt

third_party(gflags)
set(glog_deps gflags)
third_party(glog)

build_tests()

#########################################
# Prototype:
set(tbb_prototype_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${THIRD_PARTY_PATH}
  ${LIBSIGC_PATH}
  ${LIBTBB_PATH}
)
set(tbb_prototype_srcs
  AllTests.cpp
#  context_pipeline.cpp
  tbb_prototype.cpp
)
set(tbb_prototype_libs
  data_protobuffers
  boost_system
  boost_thread
  protobuf
  pthread
  gflags
  glog
  sigc-2.0
  tbb_debug
)
cpp_gtest(tbb_prototype)
