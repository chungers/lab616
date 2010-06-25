#ifndef IB_POLLING_CLIENT_H_
#define IB_POLLING_CLIENT_H_

#include <ib/session.hpp>

#include <boost/scoped_ptr.hpp>
#include <boost/thread.hpp>

using namespace ib::adapter;
using namespace std;

#define LOG_LEVEL 1

namespace ib {
namespace internal {


class EPosixClientSocketFactory
{
 public:
  virtual EPosixClientSocket* Connect() = 0;
};


// Manages a thread with which the events from the
// EPosixClientSocket is polled.
class PollingClient {

 public:
  PollingClient(EPosixClientSocketFactory* factory);
  ~PollingClient();

  void start();
  void stop();
  void join();

 private :

  EPosixClientSocketFactory* client_socket_factory_;

  volatile bool stop_requested_;
  boost::shared_ptr<boost::thread> polling_thread_;
  boost::mutex mutex_;

  const unsigned max_retries_;
  const unsigned sleep_seconds_; // seconds.

  void event_loop();
  void poll_socket(timeval tval, EPosixClientSocket* socket);

};

} // namespace internal
} // namespace ib


#endif // IB_POLLING_CLIENT_H_

