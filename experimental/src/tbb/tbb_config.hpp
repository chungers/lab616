#ifndef TBB_PROTOTYPE_H_
#define TBB_PROTOTYPE_H_

namespace TbbPrototype {

struct Config {

  Config();

  int threads;
  int ticks;
  int tokens;
  bool verbose;


};

// Function to return the configuration
const Config* GetConfig();

};

#endif // TBB_PROTOTYPE_H_
