extern "C"
{
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
}

#include <iostream>
#include <luabind/luabind.hpp>

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

int main() {
  // Create a new lua state
  lua_State *myLuaState = lua_open();

  // Connect LuaBind to this lua state
  luabind::open(myLuaState);

  // Add our function to the state's global scope
  luabind::module(myLuaState) [
      luabind::def("print_hello", print_hello)
                               ];

  // Now call our function in a lua script
  luaL_dostring(
      myLuaState,
      "print_hello(123)\n"
                );

  lua_close(myLuaState);
}
