#ifndef IB_SESSION_H_
#define IB_SESSION_H_

#include <ib/services.hpp>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/scoped_ptr.hpp>

using namespace std;
using ib::services::IMarketData;

namespace ib {

// A single session with the IB API Gateway, identified by
// the host, port, and connection id.
class Session
{
 public:

  Session(string host, unsigned int port, unsigned int connection_id);
  ~Session();

 public:

  void start();
  void stop();
  void join();

  bool ready(int timeout = 0);


  // Callbacks
  typedef boost::function<void()> DisconnectCallback;
  void register_callback(DisconnectCallback cb);


  // Interface for request market data (tick, book, etc.)
  IMarketData* access_market_data();

 private:
  class implementation;
  boost::scoped_ptr<implementation> impl_;

};

} // namespace ib
#endif // IB_SESSION_H_
