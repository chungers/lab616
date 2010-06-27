#ifndef IB_SESSION_H_
#define IB_SESSION_H_

#include <ib/services.hpp>
#include <boost/scoped_ptr.hpp>

using namespace std;
using ib::services::IMarketData;

namespace ib {

// A single session with the IB API Gateway, identified by
// the host, port, and connection id.
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
  void stop();
  void join();

  // These are public for now.  Not really necessary.
  const State get_current_state();
  const State get_previous_state();

  bool ready(int timeout = 0);

  // Interface for request market data (tick, book, etc.)
  IMarketData* access_market_data();

 private:
  class implementation;
  boost::scoped_ptr<implementation> impl_;

};

} // namespace ib
#endif // IB_SESSION_H_
