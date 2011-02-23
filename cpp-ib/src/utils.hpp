#ifndef UTILS_H_
#define UTILS_H_

// For random number generator
#include <ctime>
#include <cstdlib>
#include <unistd.h>

#include <sys/time.h>
#include <time.h>
#include <unistd.h>

namespace lab616 {
namespace utils {

/** Initializes the random number generator. */
inline static void init_random() {
  srand((unsigned)time(NULL));
}

/** Sleeps for the specified number of microseconds. */
inline void sleep_micros(int micros) {
  struct timespec st;
  struct timespec rt;
  st.tv_sec = micros / 1000000;
  st.tv_nsec = (micros % 1000000) * 1000;
  nanosleep(&st, &rt);
}

/** Current time in micros */
inline uint64_t now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<uint64_t>(tv.tv_sec) * 1000000 + tv.tv_usec;
}


} // utils
} // lab616


#endif // UTILS_H_
