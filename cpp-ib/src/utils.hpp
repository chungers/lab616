#ifndef UTILS_H_
#define UTILS_H_

// For random number generator
#include <ctime>
#include <cstdlib>

namespace lab616 {
namespace utils {

/**
 * Initializes the random number generator.
 */
static void init_random() {
  srand((unsigned)time(NULL));
}

} // utils
} // lab616


#endif // UTILS_H_
