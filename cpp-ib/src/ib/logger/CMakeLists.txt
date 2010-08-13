# //cpp-ib/src/ib/logger

######################
set(logger_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(logger_srcs
  logger_main.cpp
)
set(logger_libs
  v964_adapter
  boost_thread
  gflags
  glog
  tcmalloc
)
cpp_executable(logger)
