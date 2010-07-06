#include <string.h>
#include <unistd.h>
#include <boost/date_time.hpp>
#include <boost/format.hpp>
#include <gflags/gflags.h>
#include <glog/logging.h>

#include <kctreedb.h>

#include <tickdb/tickdb_format.pb.h>

using namespace std;
using namespace kyotocabinet;

DEFINE_int32(records, 1000, "Number of records.");
DEFINE_string(dbfile, "test.db", "Database filename.");
DEFINE_string(jump, "", "Cursor to x");

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

  // create the database object
  TreeDB db;

  // open the database
  if (!db.open(FLAGS_dbfile, TreeDB::OWRITER | TreeDB::OCREATE)) {
    cerr << "open error: " << db.error().name() << endl;
  }

  cout << "********************** STORE ******************" << endl;

  boost::format key_formatter("k-%d");
  boost::format val_formatter("v-%d");
  // store records
  uint64_t start = now_micros();

  int overwritten = 0;
  for (int i = 0; i < FLAGS_records; i++) {


    string key = str(key_formatter % i);
    string value = str(val_formatter % i);

    tickdb::file::Key keyProto;
    keyProto.set_row_type(tickdb::file::DATA);
    keyProto.set_time_stamp(start++);
    keyProto.set_ticker_id(2000000);

    tickdb::file::Value valueProto;
    valueProto.set_message_type_code(1);
    valueProto.set_value_bytes(value.c_str());
    valueProto.set_str_value(value);

    cout << "key = " << keyProto.SerializeAsString() << ", value = "
         << valueProto.SerializeAsString()
         << ", kByteSize = " << keyProto.ByteSize()
         << ", kAsStringSize = " << keyProto.SerializeAsString().size()
         << ", ksize = " << (keyProto.SerializeAsString().size() + 1) * sizeof(char)
         << endl;

    int ksz = keyProto.ByteSize();
    const char* k = keyProto.SerializeAsString().c_str();
    int vsz = valueProto.ByteSize();
    const char* v = valueProto.SerializeAsString().c_str();
    db.add(k, ksz, v, vsz);

    //    if (!db.set(key, value)) {
    //  cerr << "set error: " << db.error().name() << endl;
    //}



  }
  uint64_t elapsed = now_micros() - start;
  cout << "Elapsed = " << elapsed << ", qps = "
       << (static_cast<double>(FLAGS_records)/static_cast<double>(elapsed)) * 1000000
       << ", overwritten = " << overwritten
       << endl;

  // traverse records
  cout << "********************** TRAVERSE ******************" << endl;

  DB::Cursor* cur = db.cursor();
  cur->jump(FLAGS_jump);
  pair<string, string>* rec;
  while ((rec = cur->get_pair(true)) != NULL) {
    cout << rec->first << ":" << rec->second << endl;
    //delete rec;
  }
  delete cur;

  cout << "********************** CLOSE ******************" << endl;

  // close the database
  if (!db.close()) {
    cerr << "close error: " << db.error().name() << endl;
  }


  return 0;

}
