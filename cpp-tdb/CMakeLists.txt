# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-tdb
project(TDB)

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

option(OPTIMIZE "Compiler optimization" ON)

emacs_ide_project("")


# Third party libraries used.
boost_libraries(date_time thread)
third_party(gflags)
set(glog_deps gflags) # glog depends on gflags
third_party(glog)

# Kyotocabinet storage engine.
third_party(kyotocabinet)


# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(test)


