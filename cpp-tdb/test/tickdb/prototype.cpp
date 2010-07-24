#include <iostream>
#include <boost/format.hpp>
#include <boost/filesystem.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include <kccompare.h>
#include <kctreedb.h>
#include "tickdb/tickdb_format.pb.h"
#include "tickdb/utils.hpp"


using namespace std;
using namespace kyotocabinet;
using namespace tickdb::record;
using namespace tickdb::file;


namespace {

typedef uint64_t Timestamp;

inline Timestamp now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<Timestamp>(tv.tv_sec) * 1000000 + tv.tv_usec;
}


static bool insert(TreeDB* db, const Key::Id& id, int i)
{
  Key::Timestamp ts(now_micros());

  // Build the key
  Key* rowKey = new Key(id, ts);
  string* keyBuff = new string;
  rowKey->ToString(keyBuff);

  Payload* payload = new Payload();
  payload->set_id(id);
  payload->set_timestamp(ts);
  payload->set_value((double) i * 10. + 1000.);
  payload->set_type(Payload_Type_TYPE1);
  RowValue* rowValue = new RowValue();
  rowValue->set_bytes(payload->SerializeAsString());
  string* rowBuff = new string;
  rowValue->SerializeToString(rowBuff);

  bool status = true;

  if (db) {
    status = db->add(keyBuff->c_str(), keyBuff->size(),
                          rowBuff->c_str(), rowBuff->size());
    cout << " " << i;
  }

  delete keyBuff;
  delete rowBuff;
  delete rowKey;
  delete rowValue;
  return status;
}

// Allocates from stack
static bool insertStack(TreeDB* db, const Key::Id& id, int i)
{
  Key::Timestamp ts(now_micros());

  // Build the key
  Key rowKey(id, ts);
  string keyBuff;
  rowKey.ToString(&keyBuff);

  Payload payload;
  payload.set_id(id);
  payload.set_timestamp(ts);
  payload.set_value((double) i * 10. + 1000.);
  payload.set_type(Payload_Type_TYPE1);
  RowValue rowValue;
  rowValue.set_bytes(payload.SerializeAsString());
  string rowBuff;
  rowValue.SerializeToString(&rowBuff);

  bool status = true;

  if (db) {
    status = db->add(keyBuff.c_str(), keyBuff.size(),
                          rowBuff.c_str(), rowBuff.size());

    cout << " " << i;
  }
  return status;
}

static int MAX_ROWS_TO_PRINT = 20;
static int MAX_RECORDS = 100000;

static int dumpDb(TreeDB* db)
{
  cout << "Count = " << db->count() << endl;
  int count = 0;
  DB::Cursor* cur = db->cursor();
  cur->jump();
  pair<string, string>* rec;
  while ((rec = cur->get_pair(true)) != NULL) {
    Key key(rec->first.c_str(), rec->first.size());
    RowValue row;
    EXPECT_TRUE(row.ParseFromString(rec->second));
    Payload payload;
    EXPECT_TRUE(payload.ParseFromString(row.bytes()));

    if (count < MAX_ROWS_TO_PRINT) {
      cout << key
           << ":" << payload.ByteSize() << "," << row.ByteSize()
           << ":" << payload.id() << "," << payload.timestamp()
           << "," << payload.type() << "," << payload.value()
           << endl;
    }
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


TEST(Prototype, TestKeyIntegrity)
{
  for (int i = 0; i < 1000; ++i) {

    tickdb::record::Key::Id id(i * 1000);
    tickdb::record::Key::Timestamp ts(now_micros());

    Key rowKey1(id, ts);
    string keyBuff;
    rowKey1.ToString(&keyBuff);

    Key rowKey2(keyBuff.c_str(), keyBuff.size());
    EXPECT_EQ(rowKey1, rowKey2);
    EXPECT_EQ(12, keyBuff.size());
  }
}

class KeyComparator : public kyotocabinet::Comparator
{
 public:
  KeyComparator() {}
  ~KeyComparator() {}

  int32_t compare(const char* a, size_t az, const char* b, size_t bz)
  {
    Key k1(a, az);
    Key k2(b, bz);
    int64_t diff = k1.get_timestamp() - k2.get_timestamp();
    if (diff > 0) return 1;
    else if (diff == 0) return 0;
    else return -1;
  }
};

// Benchmark with encoded key (non-protobuff version)
TEST(Prototype, TreeDbEncodedKeyBenchmark)
{
  // create the database object
  TreeDB db;
  KeyComparator kc;

  // open the database
  //EXPECT_TRUE(db.tune_comparator(&kc));

  // Delete the old db file, if any.  This is because
  // kc stores metadata about the comparator in the file
  // such that if the db file was created using lexical
  // comparator, the comparator will be reset to lexical even
  // if we try to set the comparator.
  string file = "prototype.db";
  if (boost::filesystem::exists(file)) {
    boost::filesystem::remove_all(file);
    cout << "Removed old file.  Starting with new: " << file << endl;
  }
  EXPECT_TRUE(db.open(file, TreeDB::OWRITER | TreeDB::OCREATE));

  // Get the db configuration properties.
  StatusMap* status = printStatus(&db);

  // Assert that the comparator is custom.
  EXPECT_EQ("external", (*status)["rcomp"]);
  EXPECT_TRUE(db.clear());
  delete status;

  Timestamp start = now_micros();

  int records = MAX_RECORDS;
  cout << "Inserting" << endl;
  TreeDB* db_ptr = &db;
  for (int i = 0; i < MAX_RECORDS; ++i) {
    EXPECT_TRUE(insertStack(db_ptr, 1000 + i, i));
  }
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
