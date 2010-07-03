#ifndef IB_POLLING_CLIENT_H_
#define IB_POLLING_CLIENT_H_

#include <boost/scoped_ptr.hpp>
#include <boost/thread.hpp>

#include <ib/adapters.hpp>
#include <ib/session.hpp>

using namespace ib::adapter;
using namespace std;

#define LOG_LEVEL 1

namespace ib {
namespace internal {

// Interface through which the polling client interacts
// with the underlying socket client.
class EPosixClientSocketAccess
{
 public:
  virtual ~EPosixClientSocketAccess() {}
  virtual void connect() = 0;
  virtual bool is_connected() = 0;
  virtual void disconnect() = 0;
  virtual void ping() = 0;
  virtual bool poll_socket(timeval tval) = 0;
};


// Manages a thread with which the events from the
// EPosixClientSocket is polled.
class PollingClient {

 public:
  PollingClient(EPosixClientSocketAccess* access);
  ~PollingClient();

  void start();
  void stop();
  void join();

  void received_connected();
  void received_disconnected();
  void received_heartbeat(long time);

 private :

  EPosixClientSocketAccess* client_socket_access_;

  volatile bool stop_requested_;
  volatile bool connected_;
  volatile bool pending_heartbeat_;

  boost::shared_ptr<boost::thread> polling_thread_;
  boost::mutex mutex_;

  volatile time_t heartbeat_deadline_;

  void event_loop();
  bool poll_socket(timeval tval);
};

} // namespace internal
} // namespace ib


#endif // IB_POLLING_CLIENT_H_

