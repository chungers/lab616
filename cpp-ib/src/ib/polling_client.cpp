
#include <ib/adapters.hpp>
#include <ib/session.hpp>

#include <boost/thread.hpp>
#include <boost/scoped_ptr.hpp>
#include <glog/logging.h>

#include <arpa/inet.h>
#include <errno.h>
#include <sys/select.h>

using namespace ib::adapter;
using namespace std;

#define LOG_LEVEL 1

namespace ib {
namespace internal {

class PollingClient {

 public:
  PollingClient(string host, int port, int connection_id)
      : host_(host)
      , port_(port)
      , connection_id_(connection_id)
      , event_receiver_ptr_(new EventReceiver(connection_id))
      , stop_requested_(false)
      , max_retries_(50)
      , sleep_seconds_(10)
  {
  }

  ~PollingClient()
  {
  }

  void start()
  {
    assert(!polling_thread_);

    VLOG(LOG_LEVEL) << "Starting thread.";

    polling_thread_ = boost::shared_ptr<boost::thread>(
        new boost::thread(boost::bind(&PollingClient::event_loop, this)));
  }

  void stop()
  {
    assert(polling_thread_);
    stop_requested_ = true;
    polling_thread_->join();
  }

  void join()
  {
    polling_thread_->join();
  }

 private :
  string host_;
  int port_;
  int connection_id_;

  boost::scoped_ptr<EventReceiver> event_receiver_ptr_;

  volatile bool stop_requested_;
  boost::shared_ptr<boost::thread> polling_thread_;
  boost::mutex mutex_;

  const unsigned max_retries_;
  const unsigned sleep_seconds_; // seconds.

  void event_loop()
  {
    unsigned int tries = 0;
    while (!stop_requested_) {

      LoggingEClientSocket socket_client(
          connection_id_, event_receiver_ptr_->as_wrapper());

      VLOG(LOG_LEVEL) << "Connecting to " << host_ << ":" << port_ << "@"
                      << connection_id_;

      bool connected = socket_client.eConnect(
          host_.c_str(), port_, connection_id_);

      VLOG(LOG_LEVEL) << "Connected = " << connected;

      while (socket_client.isConnected()) {
        struct timeval tval;
        tval.tv_usec = 0;
        tval.tv_sec = 0;

        time_t now = time(NULL);

        // Handle different states.
        SessionState previous_state = event_receiver_ptr_->get_previous_state();
        SessionState current_state = event_receiver_ptr_->get_current_state();

        switch (current_state) {
          default:
            break;
        }
        // Invoke the handler registered for the given
        // state.  The handler has a input parameter that
        // allows actions to be requested on the socket_client.
        // This interface uses the proto buffs instead.

        // Now poll for the next event.
        poll_socket(tval, socket_client);
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

  void poll_socket(timeval tval, LoggingEClientSocket socket)
  {
    fd_set readSet, writeSet, errorSet;

    if(socket.fd() >= 0 ) {
      FD_ZERO(&readSet);
      errorSet = writeSet = readSet;

      FD_SET(socket.fd(), &readSet);

      if(!socket.isOutBufferEmpty())
        FD_SET(socket.fd(), &writeSet);

      FD_CLR(socket.fd(), &errorSet);

      int ret = select(socket.fd() + 1,
                       &readSet, &writeSet, &errorSet, &tval);

      if(ret == 0) {
        // timeout
        return;
      }

      if(ret < 0) {
        // error
        VLOG(LOG_LEVEL) << "Error. Disconnecting.";
        socket.eDisconnect();
        return;
      }

      if(socket.fd() < 0)
        return;

      if(FD_ISSET(socket.fd(), &errorSet)) {
        // error on socket
        socket.onError();
      }

      if(socket.fd() < 0)
        return;

      if(FD_ISSET(socket.fd(), &writeSet)) {
        // socket is ready for writing
        socket.onSend();
      }

      if(socket.fd() < 0)
        return;

      if(FD_ISSET(socket.fd(), &readSet)) {
        // socket is ready for reading
        socket.onReceive();
      }
    }
  }
};

} // namespace internal
} // namespace ib

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  ib::internal::PollingClient cl("", 4001, 0);
  cl.start();

  VLOG(1) << "Main thread here.";

  cl.join();
}
