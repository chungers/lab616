# common-protos
#

cmake_minimum_required(VERSION 2.6)

project(COMMON_PROTOS)

option(OPTIMIZE "Compiler optimization" ON)

# Global include
set(PROJECT_MACROS ${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmake)
include(${PROJECT_MACROS})

# Proto library: trades.proto
#include_directories(${PROTOBUF_INCLUDE_DIRS})
#protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS trades.proto)
#add_library(trades ${PROTO_SRCS} ${PROTO_HDRS})

proto_library(trades_proto trades.proto)
