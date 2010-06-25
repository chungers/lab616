
#include <ib/session.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>


using namespace ib::adapter;
using namespace std;

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

  const unsigned MAX_ATTEMPTS = FLAGS_max_attempts;
  const unsigned SLEEP_TIME = FLAGS_sleep_time;

  const string host = FLAGS_host;
  const int port = FLAGS_port;
  const int connection_id = FLAGS_client_id;

  ib::internal::Session session(host, port, connection_id);

  VLOG(1) << "Session created for " << host << ":" << port << " @ "
          << connection_id;

  session.start();

  VLOG(1) << "Main thread waiting.";

  session.join();
}
