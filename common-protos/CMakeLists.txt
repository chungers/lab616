# common-protos
#

cmake_minimum_required(VERSION 2.6)

project(COMMON_PROTOS)

option(OPTIMIZE "Compiler optimization" ON)

# Global include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

add_subdirectory(trading)
