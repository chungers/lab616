

#include <vector>
#include <boost/algorithm/string.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/format.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include "ib/services.hpp"
#include "ib/session.hpp"



using namespace std;

DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");


DEFINE_string(tickdata_symbols,
              "AMZN,ABK,BAC,DDM,DXD,EUO,ULE,FAS,FAZ,FSLR,GOOG,GS,\
JPM,NFLX,QID,QQQQ,PCLN,RIMM,RTH,SDS,SPY",
              "Symbols for tickdata only, comma-delimited.");

DEFINE_int32(client_id, 0, "Client Id.");

DEFINE_string(option_symbol, "AAPL", "Option underlying symbol");
DEFINE_bool(option_call, true, "True for Calls.");
DEFINE_int32(option_day, 20, "month 1 - 31");
DEFINE_int32(option_month, 8, "month 1 - 12");
DEFINE_int32(option_year, 2010, "YYYY");
DEFINE_double(option_strike, 260.0, "Strike");
DEFINE_bool(option_book, true, "Book data for option legs.");

ib::Session* session;
vector<string> tickdata_tokens;

static void RequestIndexData(ib::services::MarketDataInterface* md)
{
  md->RequestIndex("INDU", "NYSE");
  md->RequestIndex("SPX", "CBOE");
  md->RequestIndex("VIX", "CBOE");
  // In case we can't get spx, use spy as a substitute.
  string sym("SPY");
  VLOG(1) << "Requested " << sym
          << ", tickerId=" << md->RequestTicks(sym, false);
}

static void RequestStockData(vector<string> symbols,
                             ib::services::MarketDataInterface* md)
{
  vector<string>::iterator itr;
  for (itr = symbols.begin(); itr != symbols.end(); itr++) {
    VLOG(1) << "Requested " << *itr
            << ", tickerId=" << md->RequestTicks(*itr, false);
  }
}

void OnConnectConfirm()
{
  LOG(INFO) << "================== CONNECTION CONFIRMED ===================";

  ib::services::MarketDataInterface* md = session->AccessMarketData();
  if (md) {
    // First request index: INDU
    RequestIndexData(md);
    RequestStockData(tickdata_tokens, md);
  }
}

void OnDisconnect()
{
  LOG(WARNING) << "=================== DISCONNECTED ==========================";
}

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  const string host = FLAGS_host;
  const int port = FLAGS_port;
  const int connection_id = FLAGS_client_id;

  // Split the string flag by comma:
  boost::split(tickdata_tokens, FLAGS_tickdata_symbols, boost::is_any_of(","));

  // Connect
  session = new ib::Session(host, port, connection_id);

  VLOG(1) << "Session created for " << host << ":" << port << " @ "
          << connection_id;

  session->Start();

  // register callback
  session->RegisterCallbackOnConnect(boost::bind(OnConnectConfirm));
  session->RegisterCallbackOnDisconnect(boost::bind(OnDisconnect));

  // just wait for connection and disconnect events.
  session->Join();

  delete session;
}
