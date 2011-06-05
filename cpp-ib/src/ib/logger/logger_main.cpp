
#include <algorithm>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <vector>
#include <unistd.h>
#include <boost/algorithm/string.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/format.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include "common.hpp"
#include "ib/ib_events.pb.h"
#include "ib/backplane.hpp"
#include "ib/services.hpp"
#include "ib/session.hpp"



using namespace std;
using namespace ib::events;

DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");

DEFINE_bool(request_index, true, "True to request index feed.");
DEFINE_string(tickdata_symbols, "",
              "Symbols for tickdata only, comma-delimited.");
DEFINE_string(bookdata_symbols, "",
              "Symbols for bookdata only, must be in tickdata_symbols");

DEFINE_int32(client_id, 0, "Client Id.");

DEFINE_string(option_symbol, "SPY", "Option underlying symbol");
DEFINE_bool(option_call, true, "True for Calls.");
DEFINE_bool(option_straddle, true, "True if straddle -- both sides of strike.");
DEFINE_int32(option_day, 21, "month 1 - 31");
DEFINE_int32(option_month, 8, "month 1 - 12");
DEFINE_int32(option_year, 2010, "YYYY");
DEFINE_double(option_strike, 112.0, "DEFINE");

DEFINE_string(option_chain_symbol, "", "Request contract symbol");
DEFINE_bool(option_chain_call, true, "True for Calls.");
DEFINE_int32(option_chain_day, 0, "month 1 - 31");
DEFINE_int32(option_chain_month, 0, "month 1 - 12");
DEFINE_int32(option_chain_year, 0, "YYYY");


ib::Session* session;
vector<string> tickdata_tokens;
vector<string> bookdata_tokens;

vector<unsigned int> live_marketdata; // For clean up

struct Print {
  Print(const BidAsk& b) : bid_ask(b) {}
  const BidAsk& bid_ask;

  inline friend ostream& operator<<(ostream& out, const Print& p)
  {
    const BidAsk& bid_ask = p.bid_ask;
    bool bid = bid_ask.has_bid();
    double price = bid ? bid_ask.bid().price() : bid_ask.ask().price();
    int size = bid ? bid_ask.bid().size() : bid_ask.ask().size();
    std::string symbol;
    ib::signal::GetSymbol(bid_ask.id(), &symbol);
    out << "BidAsk[id=" << bid_ask.id()
        << ",symbol=" << symbol
        << ",type=" << (bid ? "BID" : "ASK")
        << ",price=" << price
        << ",size=" << size
        << "]";
    return out;
  }
};

struct BidAskReceiver : public ib::Receiver<BidAsk>
{
  BidAskReceiver(int id) : id(id), received(0) {}
  int id;
  int received;
  inline void operator()(const BidAsk& bid_ask)
  {
    cout << endl << "\t\tBidAskReceiver[" << id << "] @" << ++received << ": "
         << Print(bid_ask);
   }
};

static void RequestIndexData(ib::services::MarketDataInterface* md)
{

  if (FLAGS_request_index) {
    live_marketdata.push_back(md->RequestIndex("INDU", "NYSE"));
    live_marketdata.push_back(md->RequestIndex("SPX", "CBOE"));
    live_marketdata.push_back(md->RequestIndex("VIX", "CBOE"));
  }
}

static void RequestOptionChain(ib::services::MarketDataInterface* md)
{
  CHECK(md);
  using namespace ib::services;
  MarketDataInterface::OptionType side = (FLAGS_option_chain_call) ?
      MarketDataInterface::CALL : MarketDataInterface::PUT;

  if (FLAGS_option_chain_symbol.length() > 0) {
    LOG(INFO) << "Requesting contracts for " << FLAGS_option_chain_symbol;
    md->RequestOptionChain(FLAGS_option_chain_symbol, side,
                           FLAGS_option_chain_year,
                           FLAGS_option_chain_month,
                           FLAGS_option_chain_day);
  }
}

static void RequestStockData(vector<string> symbols,
                             vector<string> bookdata_symbols,
                             ib::services::MarketDataInterface* md)
{
  vector<string>::iterator itr;
  unsigned int id;
  for (itr = symbols.begin(); itr != symbols.end(); ++itr) {
    if (itr->length()) {

      // also check to see if the symbol is for bookdata
      vector<string>::iterator inBookdataSymbols;
      inBookdataSymbols = find(bookdata_symbols.begin(),
                               bookdata_symbols.end(),
                               *itr);
      bool getBook = inBookdataSymbols != bookdata_symbols.end();

      id = md->RequestTicks(*itr, getBook);

      VLOG(1) << "Requested " << *itr
              << ", tickerId=" << id;

      live_marketdata.push_back(id);
    }
  }
}

static string FormatOptionExpiry(int year, int month, int day,
                                  string* formatted)
{
  ostringstream s1;
  string fmt = (month > 9) ? "%4d%2d" : "%4d0%1d";
  string fmt2 = (day > 9) ? "%2d" : "0%1d";
  s1 << boost::format(fmt) % year % month << boost::format(fmt2) % day;
  formatted->assign(s1.str());
  return *formatted;
}

static void RequestOptionData(ib::services::MarketDataInterface* md)
{
  ib::services::MarketDataInterface::OptionType option_type =
      FLAGS_option_call ?
      ib::services::MarketDataInterface::CALL :
      ib::services::MarketDataInterface::PUT;

  live_marketdata.push_back(
      md->RequestOptionData(FLAGS_option_symbol,
                            option_type,
                            FLAGS_option_strike,
                            FLAGS_option_year,
                            FLAGS_option_month,
                            FLAGS_option_day, false));

  string formatted;
  VLOG(1) << "Requested " << FLAGS_option_symbol << ",side = " << option_type
          << ", strike = " << FLAGS_option_strike
          << ", expiry = " << FormatOptionExpiry(FLAGS_option_year,
                                                 FLAGS_option_month,
                                                 FLAGS_option_day,
                                                 &formatted);

  if (FLAGS_option_straddle) {
    ib::services::MarketDataInterface::OptionType option_type2 =
        FLAGS_option_call ?
        ib::services::MarketDataInterface::PUT:
        ib::services::MarketDataInterface::CALL;
    live_marketdata.push_back(
        md->RequestOptionData(FLAGS_option_symbol,
                              option_type2,
                              FLAGS_option_strike,
                              FLAGS_option_year,
                              FLAGS_option_month,
                              FLAGS_option_day, false));

    string formatted;
    VLOG(1) << "Requested " << FLAGS_option_symbol << ",side = " << option_type2
            << ", strike = " << FLAGS_option_strike
            << ", expiry = " << FormatOptionExpiry(FLAGS_option_year,
                                                   FLAGS_option_month,
                                                   FLAGS_option_day,
                                                   &formatted);
  }
}

void OnTerminate(int param)
{
  LOG(INFO) << "===================== SHUTTING DOWN =======================";

  ib::services::MarketDataInterface* md = session->AccessMarketData();
  if (md) {
  // First cancel data subscriptions
    vector<unsigned int>::iterator itr;
    for (itr = live_marketdata.begin(); itr != live_marketdata.end(); ++itr) {
      LOG(INFO) << "Cancel market data for " << *itr;
      md->CancelMarketData(*itr);
    }
  }
  sleep(5);
  LOG(INFO) << "Calling session Stop.";
  session->Stop();
  LOG(INFO) << "Waiting for shutdown....";
  session->Join();
  LOG(INFO) << "Bye.";
  exit(1);
}

void OnConnectConfirm()
{
  LOG(INFO) << "================== CONNECTION CONFIRMED ===================";

  ib::services::MarketDataInterface* md = session->AccessMarketData();
  if (md) {
    RequestOptionChain(md);
    RequestIndexData(md);
    RequestOptionData(md);
    RequestStockData(tickdata_tokens, bookdata_tokens, md);
  }
}

void OnDisconnect()
{
  LOG(INFO) << "=================== DISCONNECTED ==========================";
  live_marketdata.clear();
  LOG(INFO) << "Cleared live_marketdata list.";
}

int main(int argc, char** argv)
{
  // Signal handler
  void (*terminate)(int);
  terminate = signal(SIGTERM, OnTerminate);
  if (terminate == SIG_IGN) {
    LOG(INFO) << "********** RESETTING SIGNAL SIGTERM";
    signal(SIGTERM, SIG_IGN);
  }

  google::SetUsageMessage("IB API client and logwriter of market data..");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  const string host = FLAGS_host;
  const int port = FLAGS_port;
  const int connection_id = FLAGS_client_id;

  // Split the string flag by comma:
  boost::split(tickdata_tokens, FLAGS_tickdata_symbols, boost::is_any_of(","));
  boost::split(bookdata_tokens, FLAGS_bookdata_symbols, boost::is_any_of(","));

  // Connect
  session = new ib::Session(host, port, connection_id);

  VLOG(1) << "Session created for " << host << ":" << port << " @ "
          << connection_id;

  session->Start();

  // register callback
  session->RegisterCallbackOnConnect(boost::bind(OnConnectConfirm));
  session->RegisterCallbackOnDisconnect(boost::bind(OnDisconnect));

  // Receive signals
  BidAskReceiver receiver1(1);
  BidAskReceiver receiver2(2);

  ib::signal::Selection select1;
  ib::signal::Selection select2;

  select1 << "GOOG" << "AAPL";
  select2 << "SPY" << "QQQQ";

  ib::BackPlane* backplane = session->GetBackPlane();
  backplane->Register(&receiver1, &select1);
  backplane->Register(&receiver2, &select2);


  // just wait for connection and disconnect events.
  session->Join();

  delete session;
}
