#ifndef IB_UTIL_LOG_COMMON_H_
#define IB_UTIL_LOG_COMMON_H_

// Required
#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

// IB API includes. See the CMakeList.txt file for the
// actual include directory under src/ib/api.
#include <Shared/Contract.h>
#include <Shared/EClient.h>
#include <Shared/EWrapper.h>
#include <Shared/Order.h>
#include <PosixSocketClient/EPosixClientSocket.h>


#include <glog/logging.h>

#include <sys/time.h>

// Verbose level.  Use flag --v=N where N >= VLOG_LEVEL_* to see.
#define VLOG_LEVEL_ECLIENT  1
#define VLOG_LEVEL_EWRAPPER 1

typedef uint64_t int64;
inline int64 now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<int64>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

#endif // IB_UTIL_LOG_COMMON_H_
