#ifndef TBB_PROTOTYPE_H_
#define TBB_PROTOTYPE_H_

namespace TbbPrototype {

struct Config {

  Config();

  int threads;
  int ticks;
  int tokens;
  int sleep;
  bool verbose;
  bool tbb_alloc;
};

// Function to return the configuration
const Config* GetConfig();

};

#endif // TBB_PROTOTYPE_H_
