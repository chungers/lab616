# //cpp-ib/prototype


#########################################
# Prototype: ZeroMQ
set(zmq_prototype_incs
#  ${SRC_DIR}/ib/api/9.64beta
#  ${SRC_DIR}/ib/api/9.64beta/Shared
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(zmq_prototype_srcs
  zmq_prototype.cpp
)
set(zmq_prototype_libs
  boost_system
  boost_thread
#  v964_adapter
  gflags
  glog
  tcmalloc
  zmq
)
cpp_gtest(zmq_prototype)
