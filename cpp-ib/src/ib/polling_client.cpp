

#include <ib/polling_client.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <sys/select.h>

using namespace ib::adapter;
using namespace std;

namespace ib {
namespace internal {

DEFINE_int32(sleep_time, 10,
             "Sleep time in seconds before retry connection.");
DEFINE_int32(max_attempts, 50,
             "Max number of connection attempts.");


PollingClient::PollingClient(EPosixClientSocketFactory* f)
    : client_socket_factory_(f)
    , stop_requested_(false)
    , max_retries_(FLAGS_max_attempts)
    , sleep_seconds_(FLAGS_sleep_time)
{
}

PollingClient::~PollingClient()
{
}


void PollingClient::start()
{
  assert(!polling_thread_);
  VLOG(LOG_LEVEL) << "Starting thread.";

  polling_thread_ = boost::shared_ptr<boost::thread>(
      new boost::thread(boost::bind(&PollingClient::event_loop, this)));
}

void PollingClient::stop()
{
  assert(polling_thread_);
  stop_requested_ = true;
  polling_thread_->join();
}

void PollingClient::join()
{
  polling_thread_->join();
}

void PollingClient::event_loop()
{
  unsigned int tries = 0;
  while (!stop_requested_) {

    EPosixClientSocket* socket =
        client_socket_factory_->Connect();

    while (socket->isConnected()) {
      struct timeval tval;
      tval.tv_usec = 0;
      tval.tv_sec = 0;

      time_t now = time(NULL);

      // Now poll for the event.
      if (!poll_socket(tval, socket)) {
        VLOG(LOG_LEVEL) << "Error on socket. Try later.";
        break;
      }
    }

    if (tries++ >= max_retries_) {
      LOG(WARNING) << "Retry attempts exceeded: " << tries << ". Exiting.";
      break;
    }
    LOG(INFO) << "Sleeping " << sleep_seconds_ << " sec. before reconnect.";
    sleep(sleep_seconds_);
    VLOG(LOG_LEVEL) << "Reconnecting.";
  }
  VLOG(LOG_LEVEL) << "Stopped.";
}

// Returns false on error on the socket.
bool PollingClient::poll_socket(timeval tval, EPosixClientSocket* socket)
{
  fd_set readSet, writeSet, errorSet;

  if(socket->fd() >= 0 ) {
    FD_ZERO(&readSet);
    errorSet = writeSet = readSet;

    FD_SET(socket->fd(), &readSet);

    if(!socket->isOutBufferEmpty())
      FD_SET(socket->fd(), &writeSet);

    FD_CLR(socket->fd(), &errorSet);

    int ret = select(socket->fd() + 1,
                     &readSet, &writeSet, &errorSet, &tval);

    if(ret == 0) {
      // timeout
      return false;
    }

    if(ret < 0) {
      // error
      VLOG(LOG_LEVEL) << "Error. Disconnecting.";
      socket->eDisconnect();
      return false;
    }

    if(socket->fd() < 0)
      return false;

    if(FD_ISSET(socket->fd(), &errorSet)) {
      // error on socket
      socket->onError();
    }

    if(socket->fd() < 0)
      return false;

    if(FD_ISSET(socket->fd(), &writeSet)) {
      // socket is ready for writing
      socket->onSend();
    }

    if(socket->fd() < 0)
      return false;

    if(FD_ISSET(socket->fd(), &readSet)) {
      // socket is ready for reading
      socket->onReceive();
    }
  }
  return true;
}


} // namespace internal
} // namespace ib
