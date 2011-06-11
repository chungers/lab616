extern "C"
{
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}

#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

#include <Shared/Contract.h>
#include <Shared/EClient.h>
#include <Shared/Order.h>

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


void print(int number) {
  cout << "print_int: " << number << endl;
}

void print(const string& message) {
  cout << "print_strs: " << message << std::endl;
}

void print(const Contract& c) {
  cout << "Contract: \n"
       << "\ncondId         = " << c.conId
       << "\nsymbol         = " << c.symbol
       << "\nstrike         = " << c.strike
       << "\ncurrency       = " << c.currency
       << "\nmultiplier     = " << c.multiplier
       << "\nincludeExpired = " << c.includeExpired
       << std::endl;
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
      luabind::def("print", (void(*)(int))print),
      luabind::def("print", (void(*)(const std::string&))print),
      luabind::def("print", (void(*)(const Contract&))print),
      luabind::class_<Contract>("Contract")
      .def(luabind::constructor<>())
      .def_readwrite("symbol", &Contract::symbol)
      .def_readwrite("strike", &Contract::strike)
      .def_readwrite("currency", &Contract::currency)
      .def_readwrite("secType", &Contract::secType)
      .def_readwrite("includeExpired", &Contract::includeExpired)
      .def_readonly("conId", &Contract::conId)
      .def_readonly("comboLegs", &Contract::comboLegs)
                      ];

  LOG(INFO) << "Starting interpreter." << std::endl;
  LOG(INFO) << "Try to run file " << FLAGS_src.c_str() << std::endl;

  luaL_dofile(L, FLAGS_src.c_str());

  lua_close(L);
}
