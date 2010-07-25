#include <iostream>
#include <sstream>
#include <string>
#include <sys/stat.h>
#include <boost/bind.hpp>
#include <boost/format.hpp>
#include <boost/function.hpp>
#include <boost/filesystem/operations.hpp>

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

static int MAX_ROWS_TO_PRINT = 20;
static int MAX_RECORDS = 1000000;

static const Key::Id ID = 10000;

typedef uint64_t Timestamp;

inline Timestamp now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<Timestamp>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

typedef boost::function<void(const Key::Id& id, const Key::Timestamp& ts,
                             string* outstr)> KeyBuilder;
typedef boost::function<void(const char*, size_t, string* printstr)> KeyPrinter;

struct BuildRowKey {
  void operator()(const Key::Id& id, const Key::Timestamp& ts,
                  string* outstr)
  {
    RowKey rowKey;
    rowKey.set_id(static_cast<int32_t>(id));
    rowKey.set_timestamp(static_cast<int64_t>(ts));
    EXPECT_TRUE(rowKey.SerializeToString(outstr));
  }
};

struct PrintRowKey {
  void operator()(const char* buff, size_t sz, string* printstr)
  {
    RowKey rowKey;
    EXPECT_TRUE(rowKey.ParseFromString(string(buff, sz)));
    ostringstream ss;
    ss << "[" << rowKey.id() << "/" << rowKey.timestamp() << "]";
    printstr->assign(ss.str());
  }
};

struct BuildStringKey {
  void operator()(const Key::Id& id, const Key::Timestamp& ts,
                  string* outstr)
  {
    ostringstream ss;
    ss << id << '.' << ts;
    outstr->assign(ss.str());
  }
};

struct PrintStringKey {
  void operator()(const char* buff, size_t sz, string* printstr)
  {
    printstr->assign(string(buff, sz));
  }
};

static bool insertStack(TreeDB* db, KeyBuilder buildKey, int i,
                        Key::Timestamp* last)
{
  Key::Timestamp ts(now_micros());
  string keyBuff;
  buildKey(ID, ts, &keyBuff);

  Payload payload;
  payload.set_id(ID);
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
    if (!status) {
      cout << " (" << i << ",ts=" << ts << ",last=" << *last << ")";
    }
  }
  *last = ts;
  return status;
}

static int dumpDb(TreeDB* db, KeyPrinter printKey)
{
  cout << "Count = " << db->count() << endl;
  int count = 0;
  TreeDB::Cursor* cur = db->cursor();
  cur->jump();
  pair<string, string>* rec;
  while ((rec = cur->get_pair(true)) != NULL) {
    string k_str;
    printKey(rec->first.c_str(), rec->first.size(), &k_str);

    RowValue row;
    EXPECT_TRUE(row.ParseFromString(rec->second));
    Payload payload;
    EXPECT_TRUE(payload.ParseFromString(row.bytes()));

    if (count < MAX_ROWS_TO_PRINT) {
      cout << k_str << "/" << rec->first.size()
           << "=" << payload.id() << "," << payload.timestamp()
           << "," << payload.type() << "," << payload.value()
           << "@" << payload.ByteSize() << "/" << row.ByteSize()
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
    RowKey ak, bk;
    EXPECT_TRUE(ak.ParseFromString(string(a, az)));
    EXPECT_TRUE(bk.ParseFromString(string(b, bz)));
    // cout << "a(" << ak.id() << "/" << ak.timestamp() << ")"
    //      << "b(" << bk.id() << "/" << bk.timestamp() << ")" << endl;
    int64_t d = ak.timestamp() - bk.timestamp();
    int32_t dd = static_cast<int32_t>(d);
    if (dd != 0) return dd;
    else if (d > 0) return 1;
    else if (d < 0) return -1;
    return 0;
  }

  int32_t compare1(const char* a, size_t az, const char* b, size_t bz)
  {
    uint64_t t1(0), t2(0);
    for (size_t i = 0; i < 8; i++) {
      t1 |= ((uint64_t*)a)[i+4] << (64-(i+1)*8);
      t2 |= ((uint64_t*)b)[i+4] << (64-(i+1)*8);
    }
    cout << "t1 = " << t1 << ", t2 = " << t2 << endl;
    if (t1 > t2) return 1;
    else if (t1 == t2) return 0;
    else return -1;
  }
  int32_t compare0(const char* a, size_t az, const char* b, size_t bz)
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
  //EXPECT_EQ("external", (*status)["rcomp"]);
  EXPECT_TRUE(db.clear());
  delete status;

  KeyBuilder keyBuilder = BuildStringKey();
  KeyPrinter keyPrinter = PrintStringKey();
  // KeyBuilder keyBuilder = BuildRowKey();
  // KeyPrinter keyPrinter = PrintRowKey();

  Timestamp start = now_micros();
  Timestamp last;
  int records = MAX_RECORDS;
  cout << "Inserting" << endl;
  TreeDB* db_ptr = &db;
  for (int i = 0; i < MAX_RECORDS; i++) {
    EXPECT_TRUE(insertStack(db_ptr, keyBuilder, i, &last));
  }
  EXPECT_EQ(records, db.count());

  uint64_t elapsed = now_micros() - start;
  double qps = (static_cast<double>(records)
                / static_cast<double>(elapsed)) * 1000000;

  cout << "Elapsed = " << elapsed << ", qps = " << qps
       << endl;


  int visited = dumpDb(&db, keyPrinter);
  EXPECT_EQ(records, visited);

  // Note that before closing the file, the size is
  // smaller since db hasn't flushed memory to disk.
  struct stat fstat;
  stat(file.c_str(), &fstat);
  cout << "File size = " << db.size() << ","
       << ",stat=" << fstat.st_size
       << endl;

  // close the database
  EXPECT_TRUE(db.close());

  // Final file size
  namespace fs = boost::filesystem;
  fs::path p(file.c_str(), fs::native);
  cout << "File.exists=" << fs::exists(p)
       << ",regular=" << fs::is_regular(p)
       << ",size= " << fs::file_size(p) << endl;
}



} // namespace
