#include <ib/adapters.hpp>
#include <ib/session.hpp>
#include <glog/logging.h>

#define VLOG_LEVEL 2

using namespace ib::adapter;
using namespace ib::internal;
using namespace std;

//////////////////////////////////////////////////////////

class polling_implementation : public LoggingEWrapper
{
 public:
  polling_implementation(string host,
                         unsigned int port,
                         unsigned int connection_id)
      : LoggingEWrapper::LoggingEWrapper(connection_id)
      , host_(host)
      , port_(port)
      , previous_state_(Session::START)
      , current_state_(Session::START)
  {
  }

  ~polling_implementation()
  {
    if (socket_) delete socket_;
  }

 private:

  string host_;
  unsigned int port_;
  Session::State previous_state_;
  Session::State current_state_;
  EPosixClientSocket* socket_;

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

  EPosixClientSocket* Connect()
  {
    if (socket_) delete socket_;
    socket_ = new LoggingEClientSocket(get_connection_id(), this);
    socket_->eConnect(host_.c_str(), port_, get_connection_id());
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

EPosixClientSocket* Session::Connect()
{ return impl_->Connect(); }

const Session::State Session::get_current_state()
{ return impl_->get_current_state(); }

const Session::State Session::get_previous_state()
{ return impl_->get_previous_state(); }
