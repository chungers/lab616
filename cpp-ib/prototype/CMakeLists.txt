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
  varz.cpp
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


#########################################
# Prototype: NTP Client
set(ntpclient_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(ntpclient_srcs
  NtpClientMain.cpp
)
set(ntpclient_libs
  boost_system
  boost_date_time
  gflags
  glog
)
cpp_executable(ntpclient)
set_target_properties(ntpclient PROPERTIES
  RUNTIME_OUTPUT_DIRECTORY "${BUILD_DIR}")


#########################################
# Prototype: VARZ
set(varz_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(varz_srcs
  varz.cpp
  varz_test.cpp
  VarzMain.cpp
)
set(varz_libs
  boost_system
  boost_date_time
  gflags
  glog
)
cpp_executable(varz)

#########################################
# Prototype: asio
set(asio_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(asio_srcs
  asio_main.cpp
)
set(asio_libs
  boost_system
  boost_date_time
  gflags
  glog
)
cpp_executable(asio)


#########################################
# Lib luabindstatic
#
# Note this is a locally compiled version
# of the luabind library.  Luabind library
# can also be built and installed using Boost
# Boost.Build / BJam.  Boost.Build needs to
# be installed in /usr/local/share/boost-build
# and BOOST_BUILD_DIR needs to be set to this
# before running bjam in the luabind directory.

set(luabindstatic_incs
  ${THIRD_PARTY_PATH}/../lua/luabind
)
set(luabindstatic_srcs
  ${THIRD_PARTY_PATH}/../lua/luabind/src/class.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/create_class.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/inheritance.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/pcall.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/wrapper_base.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/class_info.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/error.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/link_compatibility.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/scope.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/class_registry.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/exception_handler.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/object_rep.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/stack_content_by_name.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/class_rep.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/function.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/open.cpp
  ${THIRD_PARTY_PATH}/../lua/luabind/src/weak_ref.cpp
)
set(luabindstatic_libs
  lua
)
cpp_library(luabindstatic)
set_target_properties(luabindstatic PROPERTIES
  RUNTIME_OUTPUT_DIRECTORY "${BUILD_DIR}")
message(STATUS "Build directory = ${BUILD_DIR}")


#########################################
# Prototype: luahello

set(luahello_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
  ${SRC_DIR}/ib/api/9.64
  ${SRC_DIR}/ib/api/9.64/PosixSocketClient
  ${SRC_DIR}/ib/api/9.64/Shared
)
set(luahello_srcs
  luabind_main.cpp
)
set(luahello_libs
  v964_adapter
  boost_system
  boost_date_time
  lua
  luabind
  gflags
  glog
  dl
)
cpp_executable(luahello)

#########################################
# Prototype: quantlib
set(quantlib_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
)
set(quantlib_srcs
  quantlib_main.cpp
)
set(quantlib_libs
  Quantlib
  gflags
  glog
)
cpp_executable(quantlib)

#########################################
# Prototype: protocurses

set(protocurses_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${PROTOTYPE_DIR}
  ${THIRD_PARTY_PATH}
  ${SRC_DIR}/ib/api/9.64
  ${SRC_DIR}/ib/api/9.64/PosixSocketClient
  ${SRC_DIR}/ib/api/9.64/Shared
)
set(protocurses_srcs
  protocurses_main.cpp
)
set(protocurses_libs
  boost_system
  boost_date_time
  gflags
  glog
  ncurses
)
cpp_executable(protocurses)



