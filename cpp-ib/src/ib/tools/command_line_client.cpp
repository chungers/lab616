#include "ib/tools/internal.hpp"

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <boost/date_time.hpp>
#include <string.h>
#include <unistd.h>

DEFINE_int32(max_attempts, 50, "Max number of attempts.");
DEFINE_int32(sleep_time, 10, "Sleep interval in seconds.");
DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 7496, "Port");

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);

  const unsigned MAX_ATTEMPTS = FLAGS_max_attempts;
  const unsigned SLEEP_TIME = FLAGS_sleep_time;

  const int clientId = 0;

  const std::string hostname = FLAGS_host;
  std::string name = "Name";

  unsigned attempt = 0;
  printf("Start of Simple Socket Client Test (%u.%u) attempts = %u\n",
         CLIENT_VERSION_MAJOR, CLIENT_VERSION_MINOR, attempt);

  for (;;) {
    ++attempt;
    printf( "Attempt %u, host:port=%s:%d\n", FLAGS_max_attempts,
            hostname.c_str(), FLAGS_port);

    IbClient client;

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

