
#include <gflags/gflags.h>
#include <glog/logging.h>
#include <boost/algorithm/string.hpp>
#include <boost/date_time.hpp>
#include <string.h>
#include <unistd.h>
#include <vector>

using namespace ib::client;
using namespace std;

class ThreadClient;

DEFINE_int32(max_attempts, 50, "Max number of attempts.");
DEFINE_int32(sleep_time, 10, "Sleep interval in seconds.");
DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");
DEFINE_string(symbols, "AAPL", "Symbols, comma-delimited.");
DEFINE_int32(client_id, 0, "Client Id.");
DEFINE_bool(book_data, false, "Book data.");
DEFINE_bool(tick_data, true, "Tick data.");
DEFINE_bool(realtime_bars, true, "Realtime bars");


int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  ThreadClient cl(FLAGS_host, FLAGS_port, FLAGS_client_id);
}
