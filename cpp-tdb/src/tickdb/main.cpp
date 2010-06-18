#include <gflags/gflags.h>
#include <glog/logging.h>
#include <boost/date_time.hpp>
#include <string.h>
#include <unistd.h>

#include <tickdb/tickdb_format.pb.h>

using namespace tickdb::file;

DEFINE_int32(sleep_time, 10, "Sleep interval in seconds.");
DEFINE_string(host, "", "Hostname to connect.");

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

}
