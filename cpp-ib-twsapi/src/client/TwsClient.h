#ifndef TWS_CLIENT_H
#define TWS_CLIENT_H

#include <string>

struct Contract {
  std::string symbol;
};
  
class TwsClient {
public:
  virtual ~TwsClient() {};
  virtual bool requestMarketData(const Contract& contract) = 0;
  virtual bool cancelMarketData(const Contract& contract) = 0;
};

#endif // TWS_CLIENT_H
