#include <sys/select.h>

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include <ib/adapters.hpp>
#include <ib/marketdata.hpp>
#include <ib/polling_client.hpp>
#include <ib/services.hpp>
#include <ib/session.hpp>


#define VLOG_LEVEL 2

using ib::adapter::LoggingEClientSocket;;
using ib::adapter::LoggingEWrapper;
using ib::services::IMarketData;
using ib::Session;

using namespace std;

/////////////////////////////////////////////////////////////////////////////

namespace ib {
namespace internal {

DEFINE_int32(max_wait_confirm_connection, 2000,
             "Max wait time in millis for connection confirmation.");

class polling_implementation
    : public LoggingEWrapper, public EPosixClientSocketAccess
{
 public:
  polling_implementation(string host,
                         unsigned int port,
                         unsigned int connection_id)
      : LoggingEWrapper(host, port, connection_id)
      , polling_client_(new PollingClient(this))
      , client_socket_(NULL)
      , marketdata_(NULL)
      , connected_(false)
      , connect_confirm_callback_(NULL)
      , disconnect_callback_(NULL)
  {
  }

  ~polling_implementation()
  {
  }

 private:

  boost::scoped_ptr<PollingClient> polling_client_;
  boost::scoped_ptr<EPosixClientSocket> client_socket_;
  boost::scoped_ptr<IMarketData> marketdata_;

  volatile bool connected_;
  boost::mutex connected_mutex_;
  boost::condition_variable connected_control_;

  // TODO: Use a struct
  int disconnects_;

  friend class PollingClient;
  friend class market_data_implementation;

 private:  // callbacks:

  Session::ConnectConfirmCallback connect_confirm_callback_;
  Session::DisconnectCallback disconnect_callback_;

 public:

  /** @implements Session */
  void start()
  {
    polling_client_->start();  // Start the thread.
  }

  /** @implements Session */
  void stop()
  {
    disconnect();
    polling_client_->stop();
  }

  /** @implements Session */
  void join()
  {
    // Just delegate to the polling client.
    polling_client_->join();
  }

  /** @implements Session */
  bool ready(int timeout = 0)
  {
    if (!timeout) timeout = FLAGS_max_wait_confirm_connection;
    return wait_for_order_id(boost::posix_time::milliseconds(timeout));
  }

  /** @implements Session */
  void register_callback_on_connect(Session::ConnectConfirmCallback cb)
  {
    connect_confirm_callback_ = cb;
  }

  /** @implements Session */
  void register_callback_on_disconnect(Session::DisconnectCallback cb)
  {
    disconnect_callback_ = cb;
  }

  /** @implements Session */
  IMarketData* access_market_data()
  {
    bool ok = ready();
    LOG_IF(WARNING, !ok) << "Connection not confirmed.  No market data.";
    return (ok) ? marketdata_.get() : NULL;
  }

 private:

  /** @implements EPosixClientSocketAccess */
  void connect()
  {
    const string host = get_host();
    const unsigned int port = get_port();
    const unsigned int connection_id = get_connection_id();

    // Deletes any previously allocated resource.
    client_socket_.reset(new LoggingEClientSocket(connection_id, this));
    marketdata_.reset(new MarketDataImpl(client_socket_.get()));

    LOG(INFO) << "Connecting to "
              << host << ":" << port << " @ " << connection_id;

    client_socket_->eConnect(host.c_str(), port, connection_id);
  }

  /** @implements EPosixClientSocketAccess */
  bool is_connected()
  {
    return (client_socket_.get())? client_socket_->isConnected() : false;
  }

  /** @implements EPosixClientSocketAccess */
  void disconnect()
  {
    if (client_socket_.get()) {
      client_socket_->eDisconnect();
      disconnects_++;
      polling_client_->received_disconnected();
      if (disconnect_callback_) disconnect_callback_();
    }
  }

  /** @implements EPosixClientSocketAccess */
  void ping()
  {
    if (client_socket_.get()) {
      client_socket_->reqCurrentTime();
    }
  }

  /**
   * See http://www.gnu.org/s/libc/manual/html_node/Waiting-for-I_002fO.html
   * This uses select with the timeout determined by the caller.
   * @implements EPosixClientSocketAccess
   */
  bool poll_socket(timeval tval)
  {
    if (!client_socket_.get()) return true;
    fd_set readSet, writeSet, errorSet;

    if(client_socket_->fd() >= 0 ) {
      FD_ZERO(&readSet);
      errorSet = writeSet = readSet;

      FD_SET(client_socket_->fd(), &readSet);

      if(!client_socket_->isOutBufferEmpty())
        FD_SET(client_socket_->fd(), &writeSet);

      FD_CLR(client_socket_->fd(), &errorSet);

      int ret = select(client_socket_->fd() + 1,
                       &readSet, &writeSet, &errorSet, &tval);

      if(ret == 0) return true; // expired

      if(ret < 0) {
        // error
        VLOG(LOG_LEVEL) << "Error. Disconnecting.";
        disconnect();
        return false; // Do not continue.
      }

      if(client_socket_->fd() < 0) return false;

      if(FD_ISSET(client_socket_->fd(), &errorSet)) {
        // error on socket
        client_socket_->onError();
      }

      if(client_socket_->fd() < 0) return false;

      if(FD_ISSET(client_socket_->fd(), &writeSet)) {
        // socket is ready for writing
        client_socket_->onSend();
      }

      if(client_socket_->fd() < 0) return false;

      if(FD_ISSET(client_socket_->fd(), &readSet)) {
        // socket is ready for reading
        client_socket_->onReceive();
      }
    }
    return true;  // Ok to continue.
  }

  /////////////////////////////////////////////////////////////////////////


  // Handles the various error codes and states from the
  // IB gateway.
  void error(const int id, const int errorCode, const IBString errorString)
  {
    LoggingEWrapper::error(id, errorCode, errorString);
    if (id == -1 && errorCode == 1100) {
      LOG(WARNING) << "Error code = " << errorCode << " disconnecting.";
      disconnect();
      return;
    }
    LOG(WARNING) << "Error code = " << errorCode
                 << ", message = " << errorString;
    switch (errorCode) {
      case 326:
        LOG(WARNING) << "Conflicting connection id. Disconnecting.";
        disconnect();
        // Update the connection id for connection retry.
        set_connection_id(get_connection_id() + 1);
        break;
      case 502:
        return;
      case 509:
        LOG(WARNING) << "Connection reset. Disconnecting.";
        disconnect();
      default:
        break;
    }
  }

  /** @implements EWrapper */
  void nextValidId(OrderId orderId)
  {
    LoggingEWrapper::nextValidId(orderId);
    LOG(INFO) << "Connection confirmed wth next order id = "
              << orderId;

    boost::unique_lock<boost::mutex> lock(connected_mutex_);
    connected_ = true;
    connected_control_.notify_all();

    // Notify the poll client too
    polling_client_->received_connected();
    if (connect_confirm_callback_) connect_confirm_callback_();
  }

  /** @implements EWrapper */
  void currentTime(long time)
  {
    LoggingEWrapper::currentTime(time);
    // Notify poll client that we have heartbeat.
    polling_client_->received_heartbeat(time);
  }

  // Returns false if timed out.
  bool wait_for_order_id(const boost::posix_time::time_duration& duration)
  {
    if (connected_) return true;

    boost::posix_time::ptime const start = boost::get_system_time();
    boost::system_time const timeout = start + duration;

    while (true && boost::get_system_time() <= timeout) {

      // Block here until nextValidId gets the order id.
      boost::unique_lock<boost::mutex> lock(connected_mutex_);

      if (connected_) return true;
    }
    return (connected_);;
  }

};

}; // namespace internal
}; // namespace ib


namespace ib {

/////////////////////////////////////////////////////////////////////

class Session::implementation
    : public ib::internal::polling_implementation
{
 public:
  implementation(string host, unsigned int port, unsigned int connection_id)
      : ib::internal::polling_implementation(host, port, connection_id)
  {
  }

  ~implementation()
  {
  }
};

/////////////////////////////////////////////////////////////////////
//
// Forward calls to the implementation
//
Session::Session(string host, unsigned int port, unsigned int connection_id)
    : impl_(new implementation(host, port, connection_id)) {}

Session::~Session() {}

void Session::start()
{ impl_->start(); }

void Session::stop()
{ impl_->stop(); }

void Session::join()
{ impl_->join(); }

void Session::register_callback_on_connect(Session::ConnectConfirmCallback cb)
{ impl_->register_callback_on_connect(cb); }

void Session::register_callback_on_disconnect(Session::DisconnectCallback cb)
{ impl_->register_callback_on_disconnect(cb); }

IMarketData* Session::access_market_data()
{ return impl_->access_market_data(); }

} // namespace ib
