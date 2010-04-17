#include "ib/client/SimpleClient.h"

#include <gflags/gflags.h>
#include <unistd.h>

DEFINE_int32(max_attempts, 50, "Max number of attempts.");
DEFINE_int32(sleep_time, 10, "Sleep interval in seconds.");

const unsigned MAX_ATTEMPTS = 50;
const unsigned SLEEP_TIME = 10;

int main(int argc, char** argv)
{
  const char* host = argc > 1 ? argv[1] : "";
  unsigned int port = 7496;
  int clientId = 0;

  unsigned attempt = 0;
  printf("Start of Simple Socket Client Test (%u.%u) attempts = %u\n",
         CLIENT_VERSION_MAJOR, CLIENT_VERSION_MINOR, attempt);

  for (;;) {
    ++attempt;
    printf( "Attempt %u of %u\n", attempt, MAX_ATTEMPTS);

    SimpleClient client;

    client.connect( host, port, clientId);
     
    while( client.isConnected()) {
      client.processMessages();
    }

    if( attempt >= MAX_ATTEMPTS) {
      break;
    }

    printf( "Sleeping %u seconds before next attempt\n", SLEEP_TIME);
    sleep( SLEEP_TIME);
  }

  printf ( "End of Simple Socket Client Test\n");
}

