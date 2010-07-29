# //cpp-ib/src/ib/logreader
######################
set(logreader_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(logreader_srcs
  logreader_main.cpp
)
set(logreader_libs
  v964_adapter
  boost_thread
  gflags
  glog
  tcmalloc
)
cpp_executable(logreader)
