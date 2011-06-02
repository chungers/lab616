#include <sstream>
#include <boost/format.hpp>
#include <glog/logging.h>
#include <ib/helpers.hpp>
#include <ib/ticker_id.hpp>
#include <ib/marketdata.hpp>


#define VLOG_MARKETDATA 3

using namespace std;
using namespace ib::services;


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
static void CreateContractForIndex(const string& symbol,
                                   const string& exchange,
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

static void CreateContractForStock(const string& symbol,
                                   Contract* contract)
{
  VLOG(VLOG_MARKETDATA) << "Creating STK contract for " << symbol;

  contract->symbol = symbol;
  contract->secType = "STK";
  contract->exchange = "SMART";
  contract->currency = "USD";
}

static void CreateContractForOption(const string& symbol,
                                    MarketDataInterface::OptionType option_type,
                                    double strike,
                                    int year, int month,
                                    int day,
                                    Contract* contract)
{
  VLOG(VLOG_MARKETDATA) << "Creating OPTION contract for " << symbol;
  contract->symbol = symbol;
  contract->secType = "OPT";
  contract->exchange = "SMART";
  contract->currency = "USD";
  if (strike > 0.0) {
    contract->strike = strike;
    contract->multiplier = 100;
  }
  if (year && month && day) {
    string formatted;
    FormatOptionExpiry(year, month, day, &formatted);
    contract->expiry = formatted;
  }
  contract->right = (option_type == MarketDataInterface::CALL) ? "C" : "P";
}


MarketDataImpl::MarketDataImpl(EClient* eclient) : eclient_(eclient)
{
}

MarketDataImpl::~MarketDataImpl()
{
}


unsigned int MarketDataImpl::RequestIndex(const string& symbol,
                                          const string& exchange)
{
  Contract c;
  CreateContractForIndex(symbol, exchange, &c);
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  return id;
}

unsigned int MarketDataImpl::RequestContractDetails(const string& symbol)
{
  Contract c;
  CreateContractForStock(symbol, &c);
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqContractDetails(id, c);
  return id;
}

unsigned int MarketDataImpl::RequestTicks(const string& symbol,
                                          bool marketDepth)
{
  Contract c;
  CreateContractForStock(symbol, &c);
  TickerId id = SymbolToTickerId(symbol);
  eclient_->reqMktData(id, c, GENERIC_TICK_TAGS, false);
  if (marketDepth) eclient_->reqMktDepth(id, c, 10);
  return id;
}

unsigned int MarketDataImpl::RequestOptionChain(
    const string& symbol,
    MarketDataInterface::OptionType option_type,
    int year, int month, int day)
{
  // Option.  TickerId = TickerId(stk) + strike. (strike < 2^10).
  TickerId id = SymbolToTickerId(symbol);
  Contract optContract;
  CreateContractForOption(symbol, option_type, 0.0, year, month, day,
                          &optContract);
  eclient_->reqContractDetails(id, optContract);
  return id;
}

unsigned int MarketDataImpl::RequestOptionData(
    const string& symbol,
    MarketDataInterface::OptionType option_type,
    double strike,
    int year,
    int month,
    int day,
    bool marketDepth)
{
  // Option.  TickerId = TickerId(stk) + strike. (strike < 2^10).
  bool call = (option_type == CALL);
  TickerId id = SymbolToTickerId(symbol, call, strike);
  Contract optContract;
  CreateContractForOption(symbol, option_type, strike, year, month, day,
                          &optContract);

  eclient_->reqMktData(id, optContract, GENERIC_TICK_TAGS, false);
  if (marketDepth) eclient_->reqMktDepth(id, optContract, 10);
  return id;
}

void MarketDataImpl::CancelMarketData(unsigned int id)
{
  if (ib::internal::IsTickerIdForOption(id)) {
    VLOG(VLOG_MARKETDATA) << "Id " << id << " is option contract.";
  }
  eclient_->cancelMktData(id);
}

}; // namespace internal
}; // namespace ib

