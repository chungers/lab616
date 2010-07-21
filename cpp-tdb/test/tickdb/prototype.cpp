#include <boost/format.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <kccompare.h>
#include <kctreedb.h>
#include <tickdb/tickdb_format.pb.h>
#include <tickdb/utils.hpp>


using namespace std;
using namespace kyotocabinet;
using namespace tickdb::record;

namespace {

typedef uint64_t Timestamp;

inline Timestamp now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<Timestamp>(tv.tv_sec) * 1000000 + tv.tv_usec;
}


static int populateDb(TreeDB* db, int records, const Key::Id& id)
{
  int count = 0;
  for (int i = 0; i < records; ++i) {

    Key::Timestamp ts(now_micros());
    Key rowKey(id, ts);

    string kk;
    rowKey.ToString(&kk);
    const string vv = str(boost::format("{{%s=%d}}") % rowKey % i);

    Key key2(kk.c_str(), kk.size());
    EXPECT_EQ(rowKey, key2);
    EXPECT_EQ(12, kk.size());

    db->add(kk.c_str(), kk.size(), vv.c_str(), vv.size());
    count++;
  }
  return count;
}


static int dumpDb(TreeDB* db)
{
  cout << "Count = " << db->count() << endl;
  int count = 0;
  DB::Cursor* cur = db->cursor();
  cur->jump();
  pair<string, string>* rec;
  while ((rec = cur->get_pair(true)) != NULL) {
    Key k(rec->first.c_str(), rec->first.size());
    cout << k
         << ":" << rec->second << endl;
    delete rec; // Per documentation. Caller must delete to prevent leak.
    count++;
  }
  delete cur;
  return count;
}

typedef std::map<std::string, std::string> StatusMap;

static StatusMap* printStatus(TreeDB* db) {
  StatusMap* status = new StatusMap();
  StatusMap::iterator pos;
  for (pos = status->begin(); pos != status->end(); ++pos) {
    cout << pos->first << " = " << pos->second << endl;
  }
  EXPECT_TRUE(db->status(status));
  return status;
}

TEST(Prototype, TreeDbOperations)
{
  // create the database object
  TreeDB db;

  class KeyComparator : public kyotocabinet::Comparator
  {
   public:
    KeyComparator() {}
    ~KeyComparator() {}

    int32_t compare(const char* a, size_t az, const char* b, size_t bz)
    {
      const Key k1(a, az);
      const Key k2(b, bz);
      int64_t diff = k1.get_timestamp() - k2.get_timestamp();
      if (diff > 0) return 1;
      else if (diff == 0) return 0;
      else return -1;
    }
  } kc;

  // open the database
  EXPECT_TRUE(db.tune_comparator(&kc));

  EXPECT_TRUE(db.open("prototype.db", TreeDB::OWRITER | TreeDB::OCREATE));

  StatusMap* status = printStatus(&db);
  EXPECT_EQ("external", (*status)["rcomp"]);
  EXPECT_TRUE(db.clear());
  delete status;

  Timestamp start = now_micros();

  tickdb::record::Key::Id id(10000);

  int records = 10;
  int inserted = populateDb(&db, records, id);
  EXPECT_EQ(records, inserted);
  EXPECT_EQ(records, db.count());

  uint64_t elapsed = now_micros() - start;
  double qps = (static_cast<double>(records)
                / static_cast<double>(elapsed)) * 1000000;

  cout << "Elapsed = " << elapsed << ", qps = " << qps
       << endl;


  int visited = dumpDb(&db);
  EXPECT_EQ(records, visited);

  // close the database
  EXPECT_TRUE(db.close());
}



} // namespace
