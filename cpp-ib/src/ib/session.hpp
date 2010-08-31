#ifndef IB_SESSION_H_
#define IB_SESSION_H_

#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/scoped_ptr.hpp>

#include "ib/backplane.hpp"
#include "ib/services.hpp"

using namespace std;
using ib::services::MarketDataInterface;

namespace ib {

// A single session with the IB API Gateway, identified by
// the host, port, and connection id.
class Session
{
 public:

  Session(string host, unsigned int port, unsigned int connection_id);
  ~Session();

 public:

  void Start();
  void Stop();
  void Join();

  bool IsReady(int timeout = 0);


  // Callbacks
  typedef boost::function<void()> ConnectConfirmCallback;
  void RegisterCallbackOnConnect(ConnectConfirmCallback cb);

  typedef boost::function<void()> DisconnectCallback;
  void RegisterCallbackOnDisconnect(DisconnectCallback cb);

  // Interface for request market data (tick, book, etc.)
  MarketDataInterface* AccessMarketData();

  BackPlane* GetBackPlane();

 private:
  class implementation;
  boost::scoped_ptr<implementation> impl_;

};

} // namespace ib
#endif // IB_SESSION_H_
