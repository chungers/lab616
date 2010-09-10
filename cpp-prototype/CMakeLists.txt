# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-prtotype
project(PROTOTYPE)

option(OPTIMIZE "Compiler optimization" ON)
#set(OPTIMIZE_LEVEL 2)

# Build for debugging and profiling
set(CFLAGS "-pg -g")

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)



########################################################################
# Emacs Setup.

list(APPEND emacs_includes 
  ${SRC_DIR}
)

set(THIRD_PARTY_PATH ${PROJECT_SOURCE_DIR}/../third_party/cpp)
set(LIBFASTFLOW_PATH ${PROJECT_SOURCE_DIR}/../third_party/cpp/fastflow)
set(LIBSIGC_PATH "/usr/local/include/sigc++-2.0")
set(LIBTBB_PATH ${THIRD_PARTY_PATH}/tbb/include)

list(APPEND emacs_sys_includes
  ${LIBSIGC_PATH}
  ${THIRD_PARTY_PATH}
  ${LIBFASTFLOW_PATH}
  ${LIBTBB_PATH}
)
emacs_ide_project("${emacs_includes}" "${emacs_sys_includes}")


########################################################################
# Libraries Setup.

# Third party libraries used.
boost_libraries(date_time thread)


########################################################################
# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(src/fastflow)
add_subdirectory(src/signals)
add_subdirectory(src/tbb)



