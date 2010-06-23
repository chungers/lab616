#include <ib/session.hpp>
#include <glog/logging.h>

#define VLOG_LEVEL 2

using namespace ib::internal;

//////////////////////////////////////////////////////////


class EventReceiver::implementation : public LoggingEWrapper
{
 public:
  implementation(unsigned int connection_id)
      : LoggingEWrapper::LoggingEWrapper(connection_id)
      , previous_state_(START)
      , current_state_(START)
  {
  }

  ~implementation()
  {
  }

 private:
  SessionState previous_state_;
  SessionState current_state_;

  inline void set_state(SessionState next)
  {
    previous_state_ = current_state_;
    current_state_ = next;
  }

  inline void set_state_if(SessionState last, SessionState next)
  {
    if (previous_state_ == last) current_state_ = next;
  }

 public:

  const SessionState get_current_state()
  {
    return current_state_;
  }

  const SessionState get_previous_state()
  {
    return previous_state_;
  }

  // Handles the various error codes and states from the
  // IB gateway.
  void error(const int id, const int errorCode, const IBString errorString)
  {
    LoggingEWrapper::error(id, errorCode, errorString);
    if (id == -1 && errorCode == 1100) {
      LOG(WARNING) << "Error code = " << errorCode << " disconnecting.";
      set_state(STOPPING);
      return;
    }
    if (errorCode == 502) {
      set_state(ERROR);
      LOG(INFO) << "Transitioned to state = " << get_current_state();
    }
  }

  // This method is invoked on initial connection.
  void nextValidId(OrderId orderId)
  {
    LoggingEWrapper::nextValidId(orderId);
    set_state_if(START, CONNECTED);
    VLOG(VLOG_LEVEL) << "Connected.  (" << previous_state_ << ")->("
                     << current_state_ << ")";
  }
};


/////////////////////////////////////////////////////////////////////
//
// Forward calls to the implementation
//
EventReceiver::EventReceiver(unsigned int connection_id)
    : impl_(new implementation(connection_id)) {}

EventReceiver::~EventReceiver() {}

EWrapper* EventReceiver::as_wrapper()
{ return impl_.get(); }

const SessionState EventReceiver::get_current_state()
{ return impl_->get_current_state(); }

const SessionState EventReceiver::get_previous_state()
{ return impl_->get_previous_state(); }

void EventReceiver::error(const int id, const int errorCode,
                          const IBString errorString)
{ impl_->error(id, errorCode, errorString); }

void EventReceiver::nextValidId(OrderId orderId)
{ impl_->nextValidId(orderId); }

