#include "ib/util/internal.hpp"

#include "ib/util/log_client.hpp" // Remove these later.  This is testing only.
#include "ib/util/log_wrapper.hpp" // Remove later.

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <boost/algorithm/string.hpp>
#include <boost/date_time.hpp>
#include <string.h>
#include <unistd.h>
#include <vector>


using namespace ib::util;
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

  VLOG(1) << "======================================  STARTING UP.";

  const unsigned MAX_ATTEMPTS = FLAGS_max_attempts;
  const unsigned SLEEP_TIME = FLAGS_sleep_time;

  const int clientId = FLAGS_client_id;

  const string hostname = FLAGS_host;

  unsigned attempt = 0;
  printf("Start of Simple Socket Client Test (%u.%u) attempts = %u\n",
         CLIENT_VERSION_MAJOR, CLIENT_VERSION_MINOR, attempt);

  for (;;) {
    ++attempt;
    printf( "Attempt %u, host:port=%s:%d\n", FLAGS_max_attempts,
            hostname.c_str(), FLAGS_port);
    IbClient client(clientId);

    client.RequestTickData(FLAGS_tick_data);
    client.RequestBookData(FLAGS_book_data);
    client.RequestRealTimeBars(FLAGS_realtime_bars);

    // Split the string flag by comma:
    vector<string> tokens;
    boost::split(tokens, FLAGS_symbols, boost::is_any_of(","));
    vector<string>::iterator itr;
    for (itr = tokens.begin(); itr != tokens.end(); itr++) {
      client.AddSymbol(*itr);
    }

    client.connect(hostname.c_str(), FLAGS_port, clientId);

    while( client.isConnected()) {
      client.processMessages();
    }

    if( attempt >= MAX_ATTEMPTS) {
      break;
    }

    printf( "********** Sleeping %u seconds before next attempt\n", SLEEP_TIME);
    sleep( SLEEP_TIME);
  }

  printf ( "End of Simple Socket Client Test\n");
}

