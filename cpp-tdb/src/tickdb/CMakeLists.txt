# //cpp-tb/src/tickdb/CMakeLists.txt
#

# Generate protos
proto_library(tickdb_format_proto tickdb_format.proto ${SRC_DIR}/tickdb)

set(tickdb_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${THIRD_PARTY_BUILD_DIR}/include
)
set(tickdb_srcs
  tickdb.hpp
  tickdb.cpp
  utils.cpp
)
set(tickdb_libs
  tickdb_format_proto
)
cpp_library(tickdb)


set(tdb_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(tdb_srcs
  main.cpp
)
set(tdb_libs
  tickdb
  glog
  gflags
  protobuf
  kyotocabinet
)
cpp_executable(tdb)
