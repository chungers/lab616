#include <string>
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

#include <tickdb/utils.hpp>


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
  cout << "Time functions." << endl;
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

TEST(UtilsTest, TestKeyEncodingTiming)
{
  uint MAX = 1000000;

  cout << "Encoding the key " << MAX << " times." << endl;

  ptime timer_start = microsec_clock::universal_time();
  for (uint i = 0 ; i < MAX; i++) {
    const tickdb::record::Key key(10002918, now_micros());
  }
  time_duration elapsed = microsec_clock::universal_time() - timer_start;
  uint64_t usec = elapsed.total_microseconds();

  cout << "total = " << usec
       << ", usec/key = "
       << static_cast<double>(usec) / static_cast<double>(MAX) << endl;
}

TEST(UtilsTest, TestKeyCompare)
{
  using namespace tickdb::record;

  Key::Id ticker_id = 40000;
  Key::Timestamp ts = now_micros();

  const Key key1(ticker_id, ts);
  const Key key2(ticker_id, ts + 1);
  const Key key3(ticker_id + 1, ts);

  cout << "key1 = " << key1 << endl;
  cout << "key2 = " << key2 << endl;
  cout << "key3 = " << key3 << endl;

  EXPECT_TRUE(key1 == key1);
  EXPECT_TRUE(key2 == key2);
  EXPECT_TRUE(key1 != key2);
  EXPECT_TRUE(key2 != key1);
  EXPECT_FALSE(key1 == key2);
  EXPECT_FALSE(key2 == key1);
  EXPECT_FALSE(key1 != key1);
  EXPECT_FALSE(key2 != key2);

  EXPECT_EQ(key1, key1);
  EXPECT_EQ(key2, key2);
  EXPECT_EQ(key3, key3);
  EXPECT_NE(key1, key2);
  EXPECT_NE(key1, key3);
  EXPECT_NE(key2, key3);

  // key1 and key2 have the same id so
  // comparisons are meaningful.
  EXPECT_EQ(key1.get_id(), key2.get_id());
  EXPECT_LT(key1, key2);
  EXPECT_GT(key2, key1);
  EXPECT_GE(key2, key1);
  EXPECT_LE(key1, key2);

  // key1 and key3 have different id
  // so they are different under all cases.
  EXPECT_NE(key1, key3);

  const Key key4(key1.get_id() + 1, key1.get_timestamp() + 1);
  EXPECT_NE(key1, key4);
  EXPECT_GT(key4, key1);  // Strictly in terms of time.
}




} // namespace
