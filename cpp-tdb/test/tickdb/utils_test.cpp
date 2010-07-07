#include <string.h>
#include <iostream>
#include <vector>
#include <unistd.h>

#include <boost/algorithm/string.hpp>
#include <boost/date_time.hpp>
#include <boost/date_time/local_time_adjustor.hpp>
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

using namespace boost::posix_time;
using namespace boost::gregorian;

// ptime = boost::posix_time::ptime
// us_dst = boost::posix_time::us_dst
// no_dst = boost::posix_time::no_dst

// U.S. Eastern UTC-5
typedef boost::date_time::local_adjustor<ptime, -5, us_dst> us_eastern;
// U.S. Central UTC-6
typedef boost::date_time::local_adjustor<ptime, -6, us_dst> us_central;
// U.S. Mountain UTC-7
typedef boost::date_time::local_adjustor<ptime, -7, us_dst> us_mountain;
typedef boost::date_time::local_adjustor<ptime, -7, no_dst> us_arizona;
// U.S. Pacific UTC-8
typedef boost::date_time::local_adjustor<ptime, -8, us_dst> us_pacific;

template<typename adjustor>
ptime Convert_UTC(adjustor adj, ptime local_time)
{
  return adj.local_to_utc(local_time);
}


TEST(UtilsTest, TestTimeFunctions)
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
  cout << "now PST = " << ptime_now
       << ", toUTC = " << Convert_UTC(us_pacific(), ptime_now) << endl;

  boost::gregorian::date today = ptime_now.date();
  boost::gregorian::date tomorrow = today + boost::gregorian::days(1);

  cout << "today = " << today << ", tomorrow = " << tomorrow << endl;

  using namespace boost::local_time;
  time_zone_ptr zone(new posix_time_zone("MST-07"));
  local_date_time ldt = local_microsec_clock::local_time(zone);

  cout << "zone = " << zone << ", ldt = " << ldt << endl;
}


} // namespace
