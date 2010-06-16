#include "ib/util/internal.hpp"

#include "ib/util/log_client.hpp" // Remove these later.  This is testing only.
#include "ib/util/log_wrapper.hpp" // Remove later.

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <boost/date_time.hpp>
#include <string.h>
#include <unistd.h>

using namespace ib::util;

DEFINE_int32(max_attempts, 50, "Max number of attempts.");
DEFINE_int32(sleep_time, 10, "Sleep interval in seconds.");
DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");
DEFINE_int32(client_id, 0, "Client Id.");


int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  VLOG(1) << "======================================  STARTING UP.";

  const unsigned MAX_ATTEMPTS = FLAGS_max_attempts;
  const unsigned SLEEP_TIME = FLAGS_sleep_time;

  const int clientId = FLAGS_client_id;

  const std::string hostname = FLAGS_host;
  std::string name = "Name";

  unsigned attempt = 0;
  printf("Start of Simple Socket Client Test (%u.%u) attempts = %u\n",
         CLIENT_VERSION_MAJOR, CLIENT_VERSION_MINOR, attempt);

  for (;;) {
    ++attempt;
    printf( "Attempt %u, host:port=%s:%d\n", FLAGS_max_attempts,
            hostname.c_str(), FLAGS_port);
    IbClient client(clientId);

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
