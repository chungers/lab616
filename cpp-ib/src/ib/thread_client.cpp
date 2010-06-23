
#include <ib/adapters.hpp>
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
namespace client {

enum SessionState {
  RUNNING,
  ERROR,
  SHUTTING_DOWN,
  DISCONNECTED
};

class ThreadClient;

class EWrapperBase : public LoggingEWrapper {
 public:
  EWrapperBase(int connection_id)
      : LoggingEWrapper::LoggingEWrapper(connection_id)
      , last_state_(DISCONNECTED)
      , current_state_(DISCONNECTED)
  {
  }

  ~EWrapperBase()
  {
  }

 private:
  SessionState last_state_;
  SessionState current_state_;

 public:
  SessionState GetState()
  {
    return current_state_;
  }

 private:
  inline void set_state(SessionState next)
  {
    last_state_ = current_state_;
    current_state_ = next;
  }

 public:
  void error(const int id, const int errorCode, const IBString errorString)
  {
    LoggingEWrapper::error(id, errorCode, errorString);
    if (id == -1 && errorCode == 1100) {
      LOG(WARNING) << "Error code = " << errorCode << " disconnecting.";
      set_state(SHUTTING_DOWN);
      return;
    }
    if (errorCode == 502) {
      set_state(ERROR);
      LOG(INFO) << "Transitioned to state = " << GetState();
    }
  }
  void nextValidId(OrderId orderId)
  {
    LoggingEWrapper::nextValidId(orderId);
    set_state(RUNNING);
  }
};

typedef boost::scoped_ptr<EWrapperBase> EWrapperBasePtr;

class ThreadClient {

 public:
  ThreadClient(string host, int port, int connection_id)
      : host_(host)
      , port_(port)
      , connection_id_(connection_id)
        //    , ewrapper_(new EWrapperBase(connection_id))
      , stop_requested_(false)
      , max_retries_(50)
      , sleep_seconds_(10)
  {
    ewrapper_ = new EWrapperBase(connection_id);
  }

  ~ThreadClient()
  {
    delete ewrapper_;
  }

  void start()
  {
    assert(!polling_thread_);

    VLOG(LOG_LEVEL) << "Starting thread.";

    polling_thread_ = boost::shared_ptr<boost::thread>(
        new boost::thread(boost::bind(&ThreadClient::event_loop, this)));
  }

  void stop()
  {
    assert(polling_thread_);
    stop_requested_ = true;
    polling_thread_->join();
  }

 private :
  string host_;
  int port_;
  int connection_id_;
  EWrapperBase* ewrapper_;
  //const EWrapperBasePtr ewrapper_;

  volatile bool stop_requested_;
  boost::shared_ptr<boost::thread> polling_thread_;
  boost::mutex mutex_;

  const unsigned max_retries_;
  const unsigned sleep_seconds_; // seconds.

  void event_loop()
  {
    int tries = 0;
    while (!stop_requested_) {

      LoggingEClientSocket socket_client(connection_id_, ewrapper_);

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
        SessionState state = ewrapper_->GetState();
        switch (state) {
          default:
            break;
        }
        // Invoke the handler registered for the given
        // state.  The handler has a input parameter that
        // allows actions to be requested on the socket_client.
        // This interface uses the proto buffs instead.

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

} // namespace client
} // namespace ib

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  ib::client::ThreadClient cl("", 4001, 0);
  cl.start();

  VLOG(1) << "Main thread here.";
  while (true) {

  }
}
