#include <ib/helpers.hpp>
#include <ib/marketdata.hpp>

#include <glog/logging.h>

#include <boost/format.hpp>
#include <sstream>

#define VLOG_MARKETDATA 3

using namespace std;

namespace ib {
namespace internal {


static const string GENERIC_TICK_TAGS =
    "100,101,104,105,106,107,165,221,225,233,236,258";

string FormatOptionExpiry(int year, int month)
{
  ostringstream s1;
  string fmt = (month > 9) ? "%4d%2d" : "%4d0%1d";
  s1 << boost::format(fmt) % year % month;
  return s1.str();
}

// TODO: Move to another file.
Contract CreateContractForStock(std::string symbol)
{
  VLOG(VLOG_MARKETDATA) << "Creating STK contract for " << symbol;

  Contract contract;
  contract.symbol = symbol;
  contract.secType = "STK";
  contract.exchange = "SMART";
  contract.currency = "USD";
  return contract;
}

Contract CreateContractForOption(const std::string& symbol, bool call,
                                 const double strike,
                                 const int year, const int month)
{
  VLOG(VLOG_MARKETDATA) << "Creating OPTION contract for " << symbol;

  Contract contract;
  contract.symbol = symbol;
  contract.secType = "OPT";
  contract.exchange = "SMART";
  contract.currency = "USD";
  contract.strike = strike;
  contract.expiry = FormatOptionExpiry(year, month);
  contract.right = (call) ? "CALL" : "PUT";
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
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  eclient_->reqMktDepth(id, c, 10);
  return id;
}

unsigned int MarketDataImpl::requestOptionData(
    const string& symbol,
    bool call,
    const double strike,
    const int year, const int month)
{
  // First stock
  TickerId id = requestTicks(symbol);

  // Option.  TickerId = TickerId(stk) + strike. (strike < 2^10).
  Contract optContract = CreateContractForOption(
      symbol, call, strike, year, month);

  id += strike;
  eclient_->reqMktData(id, optContract, GENERIC_TICK_TAGS, false);
  eclient_->reqMktDepth(id, optContract, 10);
  return id;
}


}; // namespace internal
}; // namespace ib

