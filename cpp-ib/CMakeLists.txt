# //cpp-ib/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-ib
project(IB)

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

option(OPTIMIZE "Compiler optimization" ON)

# IB API VERSION
# This corresponds to the name of the subdirectory in //cpp-ib/src/ib/api
set(IBAPI_VERSION "9.63beta")

# Configure the project file for loading into emacs.
configure_file(
  project.el.in
  ${PROJECT_SOURCE_DIR}/project.el
)


# Use the api version defined in the top level cmake file:
set(API_ROOT ${SRC_DIR}/ib/api/${IBAPI_VERSION})

# Include the subdirectories required for this project:	 
add_subdirectory(src/ib/api)
add_subdirectory(src/ib/client)
add_subdirectory(test/ib/mocks)
add_subdirectory(test/ib/client)


