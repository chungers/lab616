

#include <ib/services.hpp>
#include <ib/session.hpp>

#include <boost/algorithm/string.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/format.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <vector>


using namespace std;

DEFINE_string(host, "", "Hostname to connect.");
DEFINE_int32(port, 4001, "Port");
DEFINE_string(symbols, "AAPL", "Symbols, comma-delimited.");
DEFINE_int32(client_id, 0, "Client Id.");


DEFINE_bool(book_data, false, "Book data.");
DEFINE_bool(tick_data, true, "Tick data.");
DEFINE_bool(realtime_bars, true, "Realtime bars");

DEFINE_bool(option, true, "Request option data.");
DEFINE_string(option_symbol, "AAPL", "Option underlying symbol");
DEFINE_bool(option_call, true, "True for Calls.");
DEFINE_int32(option_day, 16, "month 1 - 31");
DEFINE_int32(option_month, 7, "month 1 - 12");
DEFINE_int32(option_year, 2010, "YYYY");
DEFINE_double(option_strike, 0.0, "Strike");

ib::Session* session;
vector<string> tokens;

ib::services::IMarketData* WaitForConnectionConfirmation()
{
  time_t deadline = ::time(NULL) + 30; // 30 seconds;
  ib::services::IMarketData* md = NULL;
  while (!md && deadline > ::time(NULL)) {
    VLOG(1) << "Trying to access market data.";
    md = session->access_market_data();
    LOG_IF(WARNING, !md) << "No market data.  Connection not confirmed.";

    if (md) return md;;
    // Sleep for a bit.
    sleep(1);
  }
  return md;
}

void RequestStockData(vector<string> symbols,
                      ib::services::IMarketData* md)
{
  vector<string>::iterator itr;
  for (itr = symbols.begin(); itr != symbols.end(); itr++) {
    VLOG(1) << "Requested " << *itr
            << ", tickerId=" << md->requestTicks(*itr);
  }
}

string FormatOptionExpiry(int year, int month, int day)
{
  ostringstream s1;
  string fmt = (month > 9) ? "%4d%2d" : "%4d0%1d";
  string fmt2 = (day > 9) ? "%2d" : "0%1d";
  s1 << boost::format(fmt) % year % month << boost::format(fmt2) % day;
  return s1.str();
}

void RequestOptionData(ib::services::IMarketData* md)
{
  md->requestOptionData(FLAGS_option_symbol,
                        FLAGS_option_call,
                        FLAGS_option_strike,
                        FLAGS_option_year,
                        FLAGS_option_month,
                        FLAGS_option_day);
  VLOG(1) << "Requested " << FLAGS_option_symbol
          << ", strike = " << FLAGS_option_strike
          << ", expiry = " << FormatOptionExpiry(FLAGS_option_year,
                                                 FLAGS_option_month,
                                                 FLAGS_option_day);
}

void OnConnectConfirm()
{
  LOG(INFO) << "================== CONNECTION CONFIRMED ===================";

  ib::services::IMarketData* md = session->access_market_data();
  if (md) {
    if (FLAGS_option) {
      RequestOptionData(md);
    } else {
      RequestStockData(tokens, md);
    }
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
  boost::split(tokens, FLAGS_symbols, boost::is_any_of(","));

  // Connect
  session = new ib::Session(host, port, connection_id);

  VLOG(1) << "Session created for " << host << ":" << port << " @ "
          << connection_id;

  session->start();

  // register callback
  session->register_callback_on_connect(boost::bind(OnConnectConfirm));
  session->register_callback_on_disconnect(boost::bind(OnDisconnect));

  // just wait for connection and disconnect events.
  session->join();

  delete session;
}
