#ifndef IB_INTERNAL_MARKETDATA_H_
#define IB_INTERNAL_MARKETDATA_H_

#include <ib/helpers.hpp>
#include <ib/services.hpp>

#include <glog/logging.h>

#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

#include <Shared/Contract.h>
#include <Shared/EClient.h>
#include <Shared/Order.h>

#define VLOG_MARKETDATA 3

using namespace std;

namespace ib {
namespace internal {

// TODO: Move to another file.
Contract CreateContractForStock(std::string symbol)
{
  VLOG(VLOG_MARKETDATA) << "Creating contract for " << symbol;

  Contract contract;
  contract.symbol = symbol;
  contract.secType = "STK";
  contract.exchange = "SMART";
  contract.currency = "USD";
  return contract;
}


class MarketDataImpl : public ib::services::MarketData
{
 public:
  MarketDataImpl(EClient* eclient) : eclient_(eclient)
  {
  }

  ~MarketDataImpl()
  {
  }

 private:
  EClient* eclient_;

 public:
  unsigned int requestTicks(const string& symbol)
  {
    Contract c = CreateContractForStock(symbol);
    TickerId id = to_ticker_id(symbol);
    eclient_->reqMktData(id, c, "", false);
    return id;
  }
};


}; // namespace internal
}; // namespace ib

#endif // IB_INTERNAL_MARKETDATA_H_
