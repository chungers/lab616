

#include <ib/polling_client.hpp>

#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

using namespace ib::adapter;
using namespace std;

namespace ib {
namespace internal {

const int VLOG_LEVEL = 2;

DEFINE_int32(retry_sleep_seconds, 10,
             "Sleep time in seconds before retry connection.");
DEFINE_int32(max_attempts, 50,
             "Max number of connection attempts.");
DEFINE_int32(heartbeat_deadline, 2,
             "Heartbeat deadline in seconds.");
DEFINE_int32(heartbeat_interval, 10,
             "Heartbeat interval in seconds.");


///////////////////////////////////////////////////////////////
// Polling client that polls the socket in a dedicated thread.
//
PollingClient::PollingClient(EPosixClientSocketAccess* f)
    : client_socket_access_(f)
    , stop_requested_(false)
    , connected_(false)
    , pending_heartbeat_(false)
    , heartbeat_deadline_(0)
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

void PollingClient::received_connected()
{
  boost::unique_lock<boost::mutex> lock(mutex_);
  connected_ = true;
}

void PollingClient::received_disconnected()
{
  boost::unique_lock<boost::mutex> lock(mutex_);
  connected_ = false;
}

void PollingClient::received_heartbeat(long time)
{
  time_t t = (time_t)time;
  struct tm * timeinfo = localtime (&t);
  VLOG(VLOG_LEVEL + 2) << "The current date/time is: " << asctime(timeinfo);

  // Lock then update the next heartbeat deadline. In case it's called
  // from another thread to message this object that heartbeat was received.
  boost::unique_lock<boost::mutex> lock(mutex_);
  heartbeat_deadline_ = 0;
  pending_heartbeat_ = false;
}

void PollingClient::event_loop()
{
  int tries = 0;
  while (!stop_requested_) {

    // First connect.
    client_socket_access_->connect();

    time_t next_heartbeat = time(NULL);

    while (client_socket_access_->is_connected()) {
      struct timeval tval;
      tval.tv_usec = 0;
      tval.tv_sec = 0;

      time_t now = time(NULL);
      if (connected_ && now >= next_heartbeat) {
        // Do heartbeat
        boost::unique_lock<boost::mutex> lock(mutex_);
        client_socket_access_->ping();
        heartbeat_deadline_ = now + FLAGS_heartbeat_deadline;
        next_heartbeat = now + FLAGS_heartbeat_interval;
        pending_heartbeat_ = true;
        lock.unlock();
      }

      // Check for heartbeat timeouts
      if (pending_heartbeat_ && now > heartbeat_deadline_) {
        LOG(WARNING) << "No heartbeat in " << FLAGS_heartbeat_deadline
                     << " seconds.";
        client_socket_access_->disconnect();
        LOG(WARNING) << "Disconnected because of no heartbeat.";
        break;
      }

      if (!poll_socket(tval)) {
        VLOG(LOG_LEVEL) << "Error on socket. Try later.";
        break;
      }
    }

    // Push the heartbeat deadline into the future since right now we
    // are not connected.
    boost::unique_lock<boost::mutex> lock(mutex_);
    next_heartbeat = 0;
    pending_heartbeat_ = false;
    lock.unlock();

    if (tries++ >= FLAGS_max_attempts) {
      LOG(WARNING) << "Retry attempts exceeded: " << tries << ". Exiting.";
      break;
    }
    LOG(INFO) << "Sleeping " << FLAGS_retry_sleep_seconds
              << " sec. before reconnect.";
    sleep(FLAGS_retry_sleep_seconds);
    VLOG(LOG_LEVEL) << "Reconnecting.";
  }
  VLOG(LOG_LEVEL) << "Stopped.";
}

// Returns false on error on the socket.
bool PollingClient::poll_socket(timeval tval)
{
  // Update the select() call timeout if we have a heartbeat coming.
  // The timeout is set to the next expected heartbeat.  If the timeout
  // value is too small (or == 0), there will be excessive polling because
  // the poll_socket() will return right away.  This is usually observed
  // with a spike in cpu %.
  time_t  now = time(NULL);
  if (pending_heartbeat_) {
    tval.tv_sec = heartbeat_deadline_ - now;
  } else {
    tval.tv_sec = FLAGS_heartbeat_interval;
  }
  VLOG(VLOG_LEVEL+10) << "select() timeout=" << tval.tv_sec;
  return client_socket_access_->poll_socket(tval);
}


} // namespace internal
} // namespace ib
