#include <string.h>
#include <iostream>
#include <sstream>
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

TEST(UtilsTest, TestDateTimeFunctions)
{
  using namespace boost::posix_time;

  ptime now_utc = microsec_clock::universal_time();
  ptime now_local = microsec_clock::local_time();

  // UTC is ahead of Pacific.
  EXPECT_GE(now_utc, now_local);

  // We reasonably expect accuracy up to the seconds.
  EXPECT_EQ(us_pacific::local_to_utc(now_local).time_of_day().seconds(),
            now_utc.time_of_day().seconds());

  EXPECT_EQ(us_pacific::utc_to_local(now_utc).time_of_day().seconds(),
            now_local.time_of_day().seconds());

  time_duration offset1 = now_local - now_utc; // about 7 hours behind.

  cout << "offset1 = " << offset1 << endl;

  ptime epoch(date(1970, 1, 1)); // UTC start
  time_duration t = now_utc - epoch;
  time_duration t2 = now_local - epoch;

  time_duration offset2 = t2 - t;

  cout << "offset2 = " << offset2 << endl;

  EXPECT_EQ(offset1.total_milliseconds(), offset2.total_milliseconds());

  EXPECT_EQ(8, sizeof(t.total_microseconds())); // 8 bytes for microseconds.

}

// http://stackoverflow.com/questions/1466756/c-equivalent-of-java-bytebuffer
template <typename T>
std::stringstream& put( std::stringstream& str, const T& value )
{
  union coercion { T value; char   data[ sizeof ( T ) ]; };
  coercion    c;
  c.value = value;
  str.write ( c.data, sizeof ( T ) );
  return str;
}

template <typename T>
std::stringstream& get( std::stringstream& str, T& value )
{
  union coercion { T value; char   data[ sizeof ( T ) ]; };
  coercion    c;
  c.value = value;
  str.read ( c.data, sizeof ( T ) );
  value = c.value;
  return str;
}

TEST(UtilsTest, TestEncodeKey)
{
  using namespace boost::posix_time;
  ptime now_utc = microsec_clock::universal_time();
  ptime epoch(date(1970, 1, 1)); // UTC start
  time_duration t = now_utc - epoch;

  // Encode database key.
  std::stringstream bytes;
  string symbol("AAPL");
  string::iterator itr;
  for (itr = symbol.begin(); itr < symbol.end(); itr++) {
    put(bytes, *itr);
  }
  put(bytes, t.total_microseconds());
  cout << "ts = " << t.total_microseconds() << endl;

  const char* p = bytes.str().c_str();  // kyotocabinet key buffer type
  cout << "symbol = " << string(p, 4) << endl;

  EXPECT_EQ(symbol, string(p, 4));

  std::stringstream ts_bytes(string(&p[4], 8));
  uint64_t ts;
  get(ts_bytes, ts);
  cout << "ts = " << ts << endl;

  EXPECT_EQ(t.total_microseconds(), ts);
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
