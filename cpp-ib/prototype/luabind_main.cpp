extern "C"
{
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}

#include <iostream>
#include <luabind/luabind.hpp>
#include <glog/logging.h>
#include <gflags/gflags.h>


DEFINE_string(src, "ilua.lua", "Source to run.");


void greet()
{
  std::cout << "hello world!\n";
}

using namespace std;

extern "C" int init(lua_State* L)
{
  using namespace luabind;

  open(L);

  module(L)
      [
          def("greet", &greet)
       ];

  return 0;
}


void print_hello(int number) {
  cout << "hello world " << number << endl;
}

void message(const string& message) {
  cout << "The message is: " << message << std::endl;
}

int main(int argc, char* argv[]) {

  google::SetUsageMessage("Prototype for the mongoose httpd.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  // Create a new lua state
  lua_State *L = lua_open();

  luaL_openlibs(L);

  // Connect LuaBind to this lua state
  luabind::open(L);

  // Add our function to the state's global scope
  luabind::module(L) [
      luabind::def("print_hello", print_hello),
      luabind::def("show", message)
                      ];

  LOG(INFO) << "Starting interpreter." << std::endl;

  // Now call our function in a lua script
  luaL_dostring(L,
                "print_hello(123)\n"
                );

  LOG(INFO) << "Try to run file " << FLAGS_src.c_str() << std::endl;

  luaL_dofile(L, FLAGS_src.c_str());

  lua_close(L);
}
