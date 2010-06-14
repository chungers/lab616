# //cpp-lua/CMakeLists.txt
#

cmake_minimum_required(VERSION 2.6)

# Project //cpp-lua
project(LUA)

# Macros to include
include(${PROJECT_SOURCE_DIR}/../cmake-common/Init.cmk)

option(OPTIMIZE "Compiler optimization" ON)

# Configure the project file for loading into emacs.
configure_file(
  project.el.in
  ${PROJECT_SOURCE_DIR}/project.el
)

# Include the subdirectories required for this project:
add_subdirectory(src)
add_subdirectory(test)


