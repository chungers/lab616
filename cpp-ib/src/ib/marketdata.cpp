#include <ib/helpers.hpp>
#include <ib/marketdata.hpp>

#include <glog/logging.h>

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


MarketDataImpl::MarketDataImpl(EClient* eclient) : eclient_(eclient)
{
}

MarketDataImpl::~MarketDataImpl()
{
}

unsigned int MarketDataImpl::requestTicks(const string& symbol)
{
  Contract c = CreateContractForStock(symbol);
  TickerId id = to_ticker_id(symbol);
  eclient_->reqMktData(id, c, "", false);
  return id;
}


}; // namespace internal
}; // namespace ib

