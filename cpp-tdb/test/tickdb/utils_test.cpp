#include <string.h>
#include <iostream>
#include <vector>
#include <unistd.h>

#include <boost/algorithm/string.hpp>
#include <boost/date_time.hpp>
#include <boost/format.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <kctreedb.h>

#include <tickdb/tickdb_format.pb.h>



using namespace std;
using namespace kyotocabinet;

namespace {

inline uint64_t now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<uint64_t>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

TEST(UtilsTest, TestDbKey)
{
  // Db key is of the format
  // symbol-timestamp
  //boost::format stock_format("%4s");
  //boost::format option_format("%4s:%");
  boost::format key_format("%s:%018d");

  uint64_t now = now_micros();

  string ts = str(boost::format("%s") % now);
  cout << "ts = " << ts << ", len=" << ts.length() << endl;

  cout << "key = " << key_format % "AAPL" % now_micros() << endl;

  cout << "today = " << boost::gregorian::day_clock::local_day() << endl;

  boost::posix_time::ptime ptime_utc_now =
      boost::posix_time::microsec_clock::universal_time();
  boost::posix_time::ptime ptime_now =
      boost::posix_time::second_clock::local_time();
  cout << "now = " << ptime_now << ", utc = " << ptime_utc_now << endl;

  boost::gregorian::date today = ptime_now.date();
  boost::gregorian::date tomorrow = today + boost::gregorian::days(1);

  cout << "today = " << today << ", tomorrow = " << tomorrow << endl;
}


} // namespace
