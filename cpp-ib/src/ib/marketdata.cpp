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

static string* FormatOptionExpiry(int year, int month, int day, string* out)
{
  ostringstream s1;
  string fmt = (month > 9) ? "%4d%2d" : "%4d0%1d";
  string fmt2 = (day > 9) ? "%2d" : "0%1d";
  s1 << boost::format(fmt) % year % month << boost::format(fmt2) % day;
  out->assign(s1.str());
  return out;
}

// TODO: Move to another file.
static void CreateContractForIndex(const std::string& symbol,
                                   const std::string& exchange,
                                   Contract* contract)
{
  VLOG(VLOG_MARKETDATA) << "Creating IND contract for " << symbol
                        << " on " << exchange;

  contract->symbol = symbol;
  contract->secType = "IND";
  contract->exchange = exchange;
  contract->primaryExchange = exchange;
  contract->currency = "USD";
}

static void CreateContractForStock(const std::string& symbol,
                                   Contract* contract)
{
  VLOG(VLOG_MARKETDATA) << "Creating STK contract for " << symbol;

  contract->symbol = symbol;
  contract->secType = "STK";
  contract->exchange = "SMART";
  contract->currency = "USD";
}

static void CreateContractForOption(const std::string& symbol, bool call,
                                    const double strike,
                                    const int year, const int month,
                                    const int day,
                                    Contract* contract)
{
  VLOG(VLOG_MARKETDATA) << "Creating OPTION contract for " << symbol;

  contract->symbol = symbol;
  contract->secType = "OPT";
  contract->exchange = "SMART";
  contract->currency = "USD";
  contract->strike = strike;
  string formatted;
  FormatOptionExpiry(year, month, day, &formatted);
  contract->expiry = formatted;
  contract->right = (call) ? "CALL" : "PUT";
  contract->multiplier = 100;
}


MarketDataImpl::MarketDataImpl(EClient* eclient) : eclient_(eclient)
{
}

MarketDataImpl::~MarketDataImpl()
{
}


unsigned int MarketDataImpl::requestIndex(const string& symbol,
                                          const string& exchange)
{
  Contract c;
  CreateContractForIndex(symbol, exchange, &c);
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  return id;
}

unsigned int MarketDataImpl::requestTicks(const string& symbol,
                                          bool marketDepth)
{
  Contract c;
  CreateContractForStock(symbol, &c);
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  if (marketDepth) eclient_->reqMktDepth(id, c, 10);
  return id;
}

unsigned int MarketDataImpl::requestOptionData(const string& symbol,
                                               bool call,
                                               const double strike,
                                               const int year,
                                               const int month,
                                               const int day,
                                               bool marketDepth)
{
  // First stock
  TickerId id = requestTicks(symbol, marketDepth);

  // Option.  TickerId = TickerId(stk) + strike. (strike < 2^10).
  Contract optContract;
  CreateContractForOption(symbol, call, strike, year, month, day,
                          &optContract);

  // One sid of straddle
  TickerId id1 = id + 512 + (call ? +1 : -1) * strike;
  eclient_->reqMktData(id1, optContract, GENERIC_TICK_TAGS, false);
  if (marketDepth) eclient_->reqMktDepth(id1, optContract, 10);

  // Opposite side
  TickerId id2 = id + 512 + (!call ? +1 : -1) * strike;
  Contract optContract2;
  CreateContractForOption(symbol, !call, strike, year, month, day,
                          &optContract2);
  eclient_->reqMktData(id2, optContract2, GENERIC_TICK_TAGS, false);
  if (marketDepth) eclient_->reqMktDepth(id2, optContract2, 10);
  return id;
}


}; // namespace internal
}; // namespace ib

