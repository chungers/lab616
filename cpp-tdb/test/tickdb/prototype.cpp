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


static void populateDb(TreeDB* db, int records, const Key::Id& id)
{
  // Add another row that's far away from the first
  {
    const string kk = str(boost::format("{%d}") % (now_micros() + 1 << 33));
    const string vv = str(boost::format("{{%d}}") % (now_micros() + 1 << 33));
    cout << "Adding " << kk << "," << vv << endl;
    db->add(kk.c_str(), kk.size(), vv.c_str(), vv.size());
  }
  for (int i = 0; i < records; i++) {

    Key::Timestamp ts(now_micros());
    Key rowKey(id, ts);

    const string kk = str(boost::format("{%d}") % ts);
    //const string kk = rowKey.ToString();
    const string vv = str(boost::format("{{%d}}") % i);

    cout << "Adding " << kk << "," << vv << endl;
    //    EXPECT_TRUE(db->add(k.first, k.second, v.first, v.second));
    db->add(kk.c_str(), kk.size(), vv.c_str(), vv.size());
  }

}


static void dumpDb(TreeDB* db)
{
  cout << "Count = " << db->count() << endl;

  DB::Cursor* cur = db->cursor();
  cur->jump();
  pair<string, string>* rec;
  while ((rec = cur->get_pair(true)) != NULL) {
    // cout << tickdb::record::Key(rec->first.c_str(), rec->first.size())
    //      << ":" << rec->second << endl;
    cout << rec->first
         << ":" << rec->second << endl;
    delete rec; // Per documentation. Caller must delete to prevent leak.
  }
  delete cur;
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
      cerr << "Compare " << k1 << " vs. " << k2 << endl;
      return (int32_t) k1.get_timestamp() - (int32_t) k2.get_timestamp();
    }
  } kc;
  //EXPECT_TRUE(db.tune_comparator(&kc));

  // open the database
  EXPECT_TRUE(db.open("prototype.db", TreeDB::OWRITER | TreeDB::OCREATE));
  EXPECT_TRUE(db.clear());


  Timestamp start = now_micros();

  tickdb::record::Key::Id id(10000);

  int records = 10;
  populateDb(&db, records, id);

  uint64_t elapsed = now_micros() - start;
  double qps = (static_cast<double>(records)
                / static_cast<double>(elapsed)) * 1000000;

  cout << "Elapsed = " << elapsed << ", qps = " << qps
       << endl;

  dumpDb(&db);

  // close the database
  EXPECT_TRUE(db.close());
}



} // namespace
