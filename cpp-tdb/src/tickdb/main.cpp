#include <string.h>
#include <unistd.h>
#include <boost/date_time.hpp>
#include <boost/format.hpp>
#include <gflags/gflags.h>
#include <glog/logging.h>

#include <kctreedb.h>

#include <tickdb/tickdb_format.pb.h>
#include <tickdb/utils.hpp>>

using namespace std;
using namespace kyotocabinet;

DEFINE_int32(records, 10000, "Number of records.");
DEFINE_string(dbfile, "test.db", "Database filename.");
DEFINE_int32(jump, -1, "K-th sample to jump to.");

inline uint64_t now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<uint64_t>(tv.tv_sec) * 1000000 + tv.tv_usec;
}


int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  // Jump index is either specified or the mid point.
  int jumpIndex = (FLAGS_jump >= 0) ? FLAGS_jump : FLAGS_records / 2;

  // create the database object
  TreeDB db;

  // open the database
  if (!db.open(FLAGS_dbfile, TreeDB::OWRITER | TreeDB::OCREATE)) {
    cerr << "open error: " << db.error().name() << endl;
  }

  cout << "********************** CLEAR ******************" << endl;
  if (!db.clear()) {
    cerr << "Clear error: " << db.error().name() << endl;
  }
  cout << "********************** STORE ******************" << endl;

  boost::format val_formatter("v-%d");

  // store records
  uint64_t start = now_micros();

  int overwritten = 0;
  tickdb::record::Key::Id id(10000);

  tickdb::record::Key jumpTo(id, start);
  cout << "Initial jumpTo = " << jumpTo << endl;

  for (int i = 0; i < FLAGS_records; i++) {

    tickdb::record::Key::Timestamp ts(now_micros());
    tickdb::record::Key rowKey(id, ts);

    string value = str(val_formatter % i);

    tickdb::file::RowKey keyProto;
    keyProto.set_id(i);
    keyProto.set_timestamp(ts);
    keyProto.set_typecode(1);

    tickdb::file::RowValue valueProto;
    valueProto.set_bytes(value.c_str());

    string k;
    rowKey.ToString(&k);
    int vsz = valueProto.ByteSize();
    const char* v = valueProto.SerializeAsString().c_str();
    db.add(k.c_str(), k.size(), v, vsz);

    if (jumpIndex == i) {
      jumpTo = rowKey;
      cout << "jumpTo = " << jumpTo << ", rowKey = " << rowKey
           << ", i = " << i
           << ", value = " << value
           << endl;
    }

    //    if (!db.set(key, value)) {
    //  cerr << "set error: " << db.error().name() << endl;
    //}

  }

  uint64_t elapsed = now_micros() - start;
  double qps = (static_cast<double>(FLAGS_records)
                / static_cast<double>(elapsed)) * 1000000;

  cout << "Elapsed = " << elapsed << ", qps = " << qps
       << ", overwritten = " << overwritten
       << endl;

  // traverse records
  cout << "********************** TRAVERSE ******************" << endl;

  DB::Cursor* cur = db.cursor();

  // Search by key.
  jumpTo = tickdb::record::Key(tickdb::record::Key::Id(1), start);
  cout << "Jumping to " << jumpTo << endl;
  string j;
  jumpTo.ToString(&j);
  cout << "Found = " << cur->jump(j.c_str(), j.size()) << endl;


  // cur->jump();
  // pair<string, string>* rec;
  // while ((rec = cur->get_pair(true)) != NULL) {
  //   cout << rec->first << ":" << rec->second << endl;
  //   delete rec; // Per documentation. Caller must delete to prevent leak.
  // }
  delete cur;

  cout << "********************** CLOSE ******************" << endl;

  // close the database
  if (!db.close()) {
    cerr << "close error: " << db.error().name() << endl;
  }


  return 0;

}
