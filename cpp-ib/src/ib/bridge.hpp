// Bridge header file.

#ifndef IB_BRIDGE_H_
#define IB_BRIDGE_H_

#include <ib/ib_events.pb.h>

using namespace std;

namespace ib {




class Bridge {

 public:
  Bridge(string host, int port, int connection_id);
  ~Bridge();

 public:
  void Connect();
  bool IsConnected();
  void Disconnect();
};
} // namespace ib
#endif // IB_BRIDGE_H_
