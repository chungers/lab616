# CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project of market data client using IB API.
project(IB_MDF)

option(OPTIMIZE "Compiler optimization" ON)

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

option(OPTIMIZE "Compiler optimization" ON)

emacs_ide_project("" "")

# Include the subdirectories required for this project:
add_subdirectory(src)

