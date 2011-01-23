# //cpp-ib/src/ib/logger

######################
set(logger_main_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(logger_main_srcs
  logger_main.cpp
)
set(logger_main_libs
  v964_adapter
  boost_thread
  gflags
  glog
  tcmalloc
)
cpp_executable(logger_main)
