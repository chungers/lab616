# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-tdb
project(TDB)

#option(OPTIMIZE "Compiler optimization" ON)
#set(OPTIMIZE_LEVEL 2)

# Build for debugging and profiling
set(CFLAGS "-pg -g -ggdb")


# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)


emacs_ide_project("" "")


# Third party libraries used.
boost_libraries(date_time thread filesystem system)
third_party(gflags)
set(glog_deps gflags) # glog depends on gflags
third_party(glog)

# Kyotocabinet storage engine.
third_party(kyotocabinet)


# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(test)


