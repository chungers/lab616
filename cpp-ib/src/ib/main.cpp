

#include <ib/services.hpp>
#include <ib/session.hpp>

#include <boost/date_time/posix_time/posix_time.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>


using namespace std;

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

  const string host = FLAGS_host;
  const int port = FLAGS_port;
  const int connection_id = FLAGS_client_id;

  ib::Session session(host, port, connection_id);

  VLOG(1) << "Session created for " << host << ":" << port << " @ "
          << connection_id;

  session.start();

  ib::services::IMarketData* md = NULL;
  while (!md) {
    VLOG(1) << "Trying to access market data.";
    md = session.access_market_data();
    LOG_IF(WARNING, !md) << "No market data.  Connection not confirmed.";

    if (md) break;
    // Sleep for a bit.
  }

  VLOG(1) << "Got market data. Requesting feed.";
  VLOG(1) << "Requested AAPL = " << md->requestTicks("AAPL");

  session.join();
}
