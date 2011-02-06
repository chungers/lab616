
#include <map>
#include <iostream>
#include <string>
#include <sstream>
#include <sys/time.h>
#include <vector>

#include <boost/algorithm/string.hpp>
#include <boost/thread.hpp>

#include <gmock/gmock.h>
#include <gflags/gflags.h>
#include <glog/logging.h>
#include <gtest/gtest.h>


using namespace std;
using namespace boost;

DEFINE_int32(iter, 1000000, "Iterations");

namespace {

typedef uint64_t int64;
inline int64 now_micros()
{
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<int64>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

inline void now_micros(string* str)
{
  struct timeval tv;
  gettimeofday(&tv, NULL);
  stringstream ss;
  ss << tv.tv_sec << tv.tv_usec;
  str->assign(ss.str());
}

TEST(UtilsTest, Test1)
{
  int iterations = FLAGS_iter;
  uint64_t now = now_micros();

  for (int i = 0; i < iterations; ++i) {
    string n1;
    now_micros(&n1);
  }

  uint64_t elapsed = now_micros() - now;
  LOG(INFO) << "CREATE AS STRING:"
            << " iterations=" << iterations
            << " dt=" << elapsed
            << " qps=" << (iterations / elapsed * 1000000ULL)
            << endl;

  ////////////////////////////////////////
  now = now_micros();
  for (int i = 0; i < iterations; ++i) {
    now_micros();
  }

  elapsed = now_micros() - now;
  LOG(INFO) << "CREATE AS UINT64:"
            << " iterations=" << iterations
            << " dt=" << elapsed
            << " qps=" << (iterations / elapsed * 1000000ULL)
            << endl;
}


TEST(UtilsTest, TestTsParsing)
{
  int iterations = FLAGS_iter;

  ////////////////////////////////////////
  // First compute cost of creating timestamps.
  uint64_t start = now_micros();
  for (int i = 0; i < iterations; ++i) {
    const char *timestamp;
    {
      uint64_t ts = now_micros();
      stringstream ss;
      ss << ts;
      const string ts_copy(ss.str()); // make a copy of the temp string from ss
      timestamp = ts_copy.c_str();
    }
  }
  uint64_t cost = now_micros() - start;
  LOG(INFO) << "Cost of creating timestamps:"
            << " cost=" << cost
            << endl;

  // Now generate timestamps and perform conversion:
  start = now_micros();
  for (int i = 0; i < iterations; ++i) {
    const char *timestamp;
    uint64_t ts = 0;
    {
      ts = now_micros();
      stringstream ss;
      ss << ts;
      const string ts_copy(ss.str()); // make a copy of the temp string from ss
      timestamp = ts_copy.c_str();
    }

    char* end_ptr;
    uint64_t ts2 = static_cast<uint64_t>(strtoll(timestamp, &end_ptr, 10));
    EXPECT_EQ(ts, ts2);
  }

  uint64_t dt = now_micros() - start;
  uint64_t actual = dt - cost;
  LOG(INFO) << "Conversion:"
            << " iterations=" << iterations
            << " cost=" << cost
            << " dt=" << dt
            << " actual=" << actual
            << " qps=" << (iterations / actual * 1000000)
            << endl;
}


} // Namespace
