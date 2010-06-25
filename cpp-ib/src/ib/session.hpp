#ifndef IB_SESSION_H_
#define IB_SESSION_H_

#include <ib/adapters.hpp>
#include <boost/scoped_ptr.hpp>

using namespace ib::adapter;
using namespace std;

namespace ib {
namespace internal {

// Class that intercepts specific IB EWrapper events
// in order to implement a unified state machine that
// serves as the foundation of a stateful session with
// the IB API Gateway.
class Session
{

 public:

  // State of a session with the IB API/Gateway.
  enum State {
    START = 0,
    CONNECTED,
    STOPPING,
    ERROR,
    DISCONNECTED
  };

  Session(string host, unsigned int port, unsigned int connection_id);
  ~Session();

 public:

  void start();
  void join();

  const State get_current_state();
  const State get_previous_state();


 private:
  class implementation;
  boost::scoped_ptr<implementation> impl_;

};

} // namespace internal
} // namespace ib
#endif // IB_SESSION_H_
