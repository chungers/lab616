// 2010, lab616.com
// Internal header file for package-private declarations.
// This shouldn't be exposed to outside world.

#ifndef IB_UTIL_INTERNAL_H_
#define IB_UTIL_INTERNAL_H_

// Required
#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

// From CMake project variables:
#include "Shared/Contract.h"
#include "Shared/Order.h"
#include "Shared/EWrapper.h"
#include "PosixSocketClient/EPosixClientSocket.h"

#include "ib/util/constants.hpp"
#include "ib/util/log_wrapper.hpp"
#include <memory>


class EPosixClientSocket;

namespace ib {
namespace util {

enum State {
  ST_CONNECT,
  ST_PLACEORDER,
  ST_PLACEORDER_ACK,
  ST_CANCELORDER,
  ST_CANCELORDER_ACK,
  ST_PING,
  ST_PING_ACK,
  ST_IDLE
};

class IbClient
{
public:

  IbClient(int id);
  ~IbClient();

  void processMessages();

public:

  bool connect(const char * host, unsigned int port, int clientId = 0);
  void disconnect() const;
  bool isConnected() const;

 public: // Overrides EWrapper
  void orderStatus(OrderId orderId, const IBString &status, int filled,
                   int remaining, double avgFillPrice, int permId, int parentId,
                   double lastFillPrice, int clientId, const IBString& whyHeld);
  void nextValidId(OrderId orderId);
  void currentTime(long time);
  void error(const int id, const int errorCode,
             const IBString errorString);

 private:

  void reqCurrentTime();
  void placeOrder();
  void cancelOrder();

private:
  int connection_id;
  std::auto_ptr<EWrapper> m_eWrapper;
  std::auto_ptr<EPosixClientSocket> m_pClient;
  State m_state;
  time_t m_sleepDeadline;

  OrderId m_orderId;
};

}
}

#endif // IB_UTIL_INTERNAL_H_
