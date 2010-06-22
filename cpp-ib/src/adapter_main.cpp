
#include <ib/adapters_v964.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include <string.h>

using namespace ib::adapter;
using namespace std;

DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");
DEFINE_int32(connection_id, 0, "Connection id");

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  VLOG(1) << "Starting up.";

  LoggingEWrapper ew(FLAGS_connection_id);
}

