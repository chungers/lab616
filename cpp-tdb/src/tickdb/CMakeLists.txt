# //cpp-tb/src/tickdb/CMakeLists.txt
#
# Third party libraries used.
boost_libraries(date_time thread)
third_party(gflags)

set(glog_deps gflags) # glog depends on gflags
third_party(glog)


# Generate protos
proto_library(tickdb_format_proto tickdb_format.proto ${SRC_DIR}/tickdb)

set(tickdb_format_incs
  ${GEN_DIR}
  ${SRC_DIR}
)
set(tickdb_format_srcs
  main.cpp
)
set(tickdb_format_libs
  tickdb_format_proto
  gflags
)
# Client implementation:
cpp_library(tickdb_format)
