#include <sstream>
#include <boost/format.hpp>
#include <glog/logging.h>
#include <ib/helpers.hpp>
#include <ib/ticker_id.hpp>
#include <ib/marketdata.hpp>


#define VLOG_MARKETDATA 3

using namespace std;

namespace ib {
namespace internal {


static const string GENERIC_TICK_TAGS =
    "100,101,104,105,106,107,165,221,225,233,236,258";

string FormatOptionExpiry(int year, int month, int day)
{
  ostringstream s1;
  string fmt = (month > 9) ? "%4d%2d" : "%4d0%1d";
  string fmt2 = (day > 9) ? "%2d" : "0%1d";
  s1 << boost::format(fmt) % year % month << boost::format(fmt2) % day;
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
                                 const int year, const int month,
                                 const int day)
{
  VLOG(VLOG_MARKETDATA) << "Creating OPTION contract for " << symbol;

  Contract contract;
  contract.symbol = symbol;
  contract.secType = "OPT";
  contract.exchange = "SMART";
  contract.currency = "USD";
  contract.strike = strike;
  contract.expiry = FormatOptionExpiry(year, month, day);
  contract.right = (call) ? "CALL" : "PUT";
  contract.multiplier = 100;
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
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  eclient_->reqMktDepth(id, c, 10);
  return id;
}

unsigned int MarketDataImpl::requestOptionData(
    const string& symbol,
    bool call,
    const double strike,
    const int year, const int month, const int day)
{
  // First stock
  TickerId id = requestTicks(symbol);

  // Option.  TickerId = TickerId(stk) + strike. (strike < 2^10).
  Contract optContract = CreateContractForOption(
      symbol, call, strike, year, month, day);

  // One sid of straddle
  TickerId id1 = id + 512 + (call ? +1 : -1) * strike;
  eclient_->reqMktData(id1, optContract, GENERIC_TICK_TAGS, false);
  eclient_->reqMktDepth(id1, optContract, 10);

  // Opposite side
  TickerId id2 = id + 512 + (!call ? +1 : -1) * strike;
  Contract optContract2 = CreateContractForOption(
      symbol, !call, strike, year, month, day);
  eclient_->reqMktData(id2, optContract2, GENERIC_TICK_TAGS, false);
  eclient_->reqMktDepth(id2, optContract2, 10);
  return id;
}


}; // namespace internal
}; // namespace ib

