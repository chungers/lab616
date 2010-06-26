#include <ib/adapters.hpp>
#include <ib/marketdata.hpp>
#include <ib/polling_client.hpp>
#include <ib/services.hpp>
#include <ib/session.hpp>

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/thread.hpp>

#include <glog/logging.h>

#define VLOG_LEVEL 2

using ib::adapter::LoggingEClientSocket;;
using ib::adapter::LoggingEWrapper;
using ib::services::MarketData;
using ib::Session;

using namespace std;

/////////////////////////////////////////////////////////////////////////////

namespace ib {
namespace internal {

class polling_implementation
    : public LoggingEWrapper, public EPosixClientSocketFactory
{
 public:
  polling_implementation(string host,
                         unsigned int port,
                         unsigned int connection_id)
      : LoggingEWrapper(host, port, connection_id)
      , previous_state_(Session::START)
      , current_state_(Session::START)
      , polling_client_(new PollingClient(this))
      , client_socket_(NULL)
      , marketdata_(NULL)
      , connected_(false)
  {
  }

  ~polling_implementation()
  {
  }

 private:

  Session::State previous_state_;
  Session::State current_state_;
  boost::scoped_ptr<PollingClient> polling_client_;

  boost::scoped_ptr<EPosixClientSocket> client_socket_;
  boost::scoped_ptr<MarketData> marketdata_;

  volatile bool connected_;
  boost::mutex connected_mutex_;
  boost::condition_variable connected_control_;

  friend class PollingClient;
  friend class market_data_implementation;

 private:

  inline void set_state(Session::State next)
  {
    previous_state_ = current_state_;
    current_state_ = next;
  }

  inline void set_state_if(Session::State last,
                           Session::State next)
  {
    if (previous_state_ == last) current_state_ = next;
  }

 public:

  bool ready(int timeout)
  {
    return wait_for_order_id(boost::posix_time::milliseconds(timeout));
  }

  void start()
  {
    // Immeidately returns as the thread starts.
    // Block only when trying to access services.
    polling_client_->start();
    return;
  }

  void stop()
  {
    // First disconnect.

    // The polling thread then stops.
    polling_client_->stop();
  }

  void join()
  {
    // Just delegate to the polling client.
    polling_client_->join();
  }

  const Session::State get_current_state()
  {
    return current_state_;
  }

  const Session::State get_previous_state()
  {
    return previous_state_;
  }

  MarketData* access_market_data()
  {
    bool ok = ready(5000);
    LOG_IF(WARNING, !ok) << "Timed out on next order id!!!";
    return (ok) ? marketdata_.get() : NULL;
  }

 private:

  EPosixClientSocket* Connect()
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

    // At this point, we really need to look for the nextValidId
    // event in EWrapper to confirm that a connection has been made.
    return client_socket_.get();
  }

  // Handles the various error codes and states from the
  // IB gateway.
  void error(const int id, const int errorCode, const IBString errorString)
  {
    LoggingEWrapper::error(id, errorCode, errorString);
    if (id == -1 && errorCode == 1100) {
      LOG(WARNING) << "Error code = " << errorCode << " disconnecting.";
      set_state(Session::STOPPING);
      return;
    }
    if (errorCode == 502) {
      set_state(Session::ERROR);
      LOG(INFO) << "Transitioned to state = " << get_current_state();
    }
  }

  // This method is invoked on initial connection.
  void nextValidId(OrderId orderId)
  {
    LoggingEWrapper::nextValidId(orderId);
    set_state_if(Session::START, Session::CONNECTED);
    VLOG(VLOG_LEVEL) << "Connected.  (" << previous_state_ << ")->("
                     << current_state_ << ")";

    boost::unique_lock<boost::mutex> lock(connected_mutex_);
    connected_ = true;
    connected_control_.notify_all();
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

const Session::State Session::get_current_state()
{ return impl_->get_current_state(); }

const Session::State Session::get_previous_state()
{ return impl_->get_previous_state(); }

MarketData* Session::access_market_data()
{ return impl_->access_market_data(); }

} // namespace ib
