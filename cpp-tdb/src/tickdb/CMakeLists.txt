# //cpp-tb/src/tickdb/CMakeLists.txt
#

# Generate protos
proto_library(tickdb_format_proto tickdb_format.proto ${SRC_DIR}/tickdb)

set(tickdb_format_incs
  ${GEN_DIR}
  ${SRC_DIR}
  ${THIRD_PARTY_BUILD_DIR}/include
)
set(tickdb_format_srcs
  main.cpp
)
set(tickdb_format_libs
  tickdb_format_proto
)
# Client implementation:
cpp_library(tickdb_format)


set(tdb_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(tdb_srcs
  main.cpp
)
set(tdb_libs
  tickdb_format
  glog
  gflags
  protobuf
  kyotocabinet
)
cpp_executable(tdb)
