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
cpp_executable(zmq_prototype)
set_target_properties(zmq_prototype PROPERTIES
  RUNTIME_OUTPUT_DIRECTORY "${BUILD_DIR}")


#########################################
# Prototype: Mongoose Httpd
set(mongoose_prototype_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(mongoose_prototype_srcs
  mongoose_prototype.cpp
  ${THIRD_PARTY_PATH}/mongoose/mongoose.c
)
set(mongoose_prototype_libs
  boost_system
  boost_thread
  gflags
  glog
  tcmalloc
  zmq
)
cpp_executable(mongoose_prototype)
set_target_properties(mongoose_prototype PROPERTIES
  RUNTIME_OUTPUT_DIRECTORY "${BUILD_DIR}")
set_target_properties(mongoose_prototype PROPERTIES
  COMPILE_FLAGS "")  # -pedantic -std=c99 -O2 -fomit-frame-pointer for prod
set_target_properties(mongoose_prototype PROPERTIES
  LINK_FLAGS "-ldl -pthread")

