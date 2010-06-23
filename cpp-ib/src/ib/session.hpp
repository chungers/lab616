#ifndef IB_SESSION_H_
#define IB_SESSION_H_

#include <ib/adapters.hpp>
#include <boost/scoped_ptr.hpp>

using namespace ib::adapter;

namespace ib {
namespace internal {

// State of a session with the IB API/Gateway.
enum SessionState {
  START,
  CONNECTED,
  STOPPING,
  ERROR,
  DISCONNECTED
};

// Class that intercepts specific IB EWrapper events
// in order to implement a unified state machine that
// serves as the foundation of a stateful session with
// the IB API Gateway.
class EventReceiver
{

 public:

  EventReceiver(unsigned int connection_id);
  ~EventReceiver();

 private:
  class implementation;
  boost::scoped_ptr<implementation> impl_;

 public:

  EWrapper* as_wrapper();
  const SessionState get_current_state();
  const SessionState get_previous_state();

  // Overrides EWrapper methods:

  void error(const int id, const int errorCode, const IBString errorString);
  void nextValidId(OrderId orderId);
};

} // namespace internal
} // namespace ib
#endif // IB_SESSION_H_
