#include <ib/adapters.hpp>
#include <ib/polling_client.hpp>
#include <ib/session.hpp>
#include <glog/logging.h>

#define VLOG_LEVEL 2

using namespace ib::adapter;
using namespace ib::internal;
using namespace std;

/////////////////////////////////////////////////////////////////////////////

class polling_implementation
    : public LoggingEWrapper, public EPosixClientSocketFactory
{
 public:
  polling_implementation(string host,
                         unsigned int port,
                         unsigned int connection_id)
      : LoggingEWrapper::LoggingEWrapper(host, port, connection_id)
      , previous_state_(Session::START)
      , current_state_(Session::START)
      , socket_(NULL)
      , polling_client_(new PollingClient(this))
  {
  }

  ~polling_implementation()
  {
    if (socket_) delete socket_;
  }

 private:

  Session::State previous_state_;
  Session::State current_state_;
  EPosixClientSocket* socket_;
  boost::scoped_ptr<PollingClient> polling_client_;

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

  const Session::State get_current_state()
  {
    return current_state_;
  }

  const Session::State get_previous_state()
  {
    return previous_state_;
  }


  void start()
  {
    //polling_client_ = (new PollingClient(this));
    // Start the polling client, which will call the Connect method to
    // get a client socket for polling for events on the socket.
    polling_client_->start();
  }

  void join()
  {
    // Just delegate to the polling client.
    polling_client_->join();
  }

  EPosixClientSocket* Connect()
  {
    if (socket_) delete socket_;

    const string host = get_host();
    const unsigned int port = get_port();
    const unsigned int connection_id = get_connection_id();

    socket_ = new LoggingEClientSocket(connection_id, this);


    LOG(INFO) << "Connecting to "
              << host << ":" << port << " @ " << connection_id;

    socket_->eConnect(host.c_str(), port, connection_id);

    // At this point, we really need to look for the nextValidId
    // event in EWrapper to confirm that a connection has been made.
    return socket_;
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
  }
};



/////////////////////////////////////////////////////////////////////
class Session::implementation : public polling_implementation
{
 public:
  implementation(string host, unsigned int port, unsigned int connection_id)
      : polling_implementation(host, port, connection_id)
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

void Session::join()
{ impl_->join(); }

const Session::State Session::get_current_state()
{ return impl_->get_current_state(); }

const Session::State Session::get_previous_state()
{ return impl_->get_previous_state(); }
