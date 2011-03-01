#include "common.hpp"
#include "messaging.hpp"
#include "utils.hpp"
#include "ib/ticker_id.hpp"

#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>

#include <zmq.hpp>

#include <boost/algorithm/string.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/format.hpp>
#include <boost/thread.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include "ib/services.hpp"
#include "ib/session.hpp"


using namespace std;

#define DEBUG1 VLOG(10)
#define DEBUG2 VLOG(20)
#define DEBUG3 VLOG(30)
#define DEBUG4 VLOG(40)


DEFINE_string(file, "logfile", "The name of the log file.");
DEFINE_string(endpoint, "tcp://*:5555", "End point string.");
DEFINE_bool(publish, false, "True to publish at given endpoint.");
DEFINE_int32(playback, 1, "X times actual speed in log. 2 for 2X. 0 to scan.");
DEFINE_int32(starthour, 9, "Hour EST to start.");

const char* NUMERIC_EVENTS[] = { "tickPrice", "tickSize", "tickGeneric" };

// Determines if the event has a numeric value.  This corresponds
// all the IB's tickPrice, tickSize, and tickGeneric events.
static bool IsMarketDataEvent(map<string, string>& event) {
  bool hasMatch = false;
  map<string, string>::iterator found = event.find("event");
  if (found != event.end()) {
    for (int i = 0; i < 3; ++i) {
      if (found->second.compare(NUMERIC_EVENTS[i]) == 0) {
        hasMatch = true;
        break;
      }
    }
  }
  return hasMatch;
}

static bool Get(map<string, string>& nv, string key, uint64_t* out) {
  map<string, string>::iterator found = nv.find(key);
  if (found != nv.end()) {
    char* end;
    *out = static_cast<uint64_t>(strtoll(found->second.c_str(), &end, 10));
    return true;
  }
  return false;
}

static bool Get(map<string, string>& nv, string key, string* out) {
  map<string, string>::iterator found = nv.find(key);
  if (found != nv.end()) {
    out->assign(nv[key]);
    return true;
  }
  return false;
}

static bool Get(map<string, string>& nv, string key, int* out) {
  map<string, string>::iterator found = nv.find(key);
  if (found != nv.end()) {
    *out = atoi(nv[key].c_str());
    return true;
  }
  return false;
}

static bool Get(map<string, string>& nv, string key, double* out) {
  map<string, string>::iterator found = nv.find(key);
  if (found != nv.end()) {
    *out = atof(nv[key].c_str());
    return true;
  }
  return false;
}

const uint64_t SECOND_MICROS = 1000 * 1000LL;
const uint64_t MINUTE_MICROS = 60 * SECOND_MICROS;
const uint64_t HOUR_MICROS = 60 * MINUTE_MICROS;
const uint64_t DAY_MICROS = 24 * HOUR_MICROS;
const int GMT_NY_OFFSET = -5;

inline static int hour_of_day(uint64_t ts) {
  return (ts % DAY_MICROS) / HOUR_MICROS + GMT_NY_OFFSET;
}

inline static int minute_of_hour(uint64_t ts) {
  return (ts % HOUR_MICROS) / MINUTE_MICROS;
}

inline static int second_of_minute(uint64_t ts) {
  return (ts % MINUTE_MICROS) / SECOND_MICROS;
}

// Struct for holding market data.  Multipart data frames are
// in the order of the fields.
// e.g. AAPL|BID|121334233343|350.00
struct MarketData {
  string symbol;
  string event;
  uint64_t ts;
  double value;
};

// Process a parsed map.
  static bool process(map<string, string>& nv, MarketData* marketData) {

  if (!IsMarketDataEvent(nv)) {
    DEBUG4 << "Not a numeric event. " << endl;
    return false;
  }

  uint64_t ts;
  string symbol;
  string event;
  double value;

  ////////// Timestamp
  if (!Get(nv, "ts_utc", &ts)) {
    return false;
  }
  DEBUG3 << "Timestamp ==> " << ts << endl;

  ////////// TickerId / Symbol:
  int code = 0;
  if (!Get(nv, "tickerId", &code)) {
    return false;
  }
  ib::internal::SymbolFromTickerId(code, &symbol);
  nv["symbol"] = symbol;
  DEBUG3 << "symbol ==> " << symbol << "(" << code << ")" << endl;

  ////////// Event
  if (!Get(nv, "field", &event)) {
    return false;
  }
  DEBUG3 << "event ==> " << event << endl;

  ////////// Price / Size
  if (!Get(nv, "price", &value)) {
    if (!Get(nv, "size", &value)) {
      if (!Get(nv, "value", &value)) {
        return false;
      }
    }
  }
  DEBUG3 << "value ==> " << value << endl;

  DEBUG1
      << symbol << ' '
      << event << ' '
      << ts << ' '
      << value << endl;

  marketData->ts = ts;
  marketData->symbol = symbol;
  marketData->event = event;
  marketData->value = value;

  return true;
}


inline static bool ParseMap(const string& token, map<string, string>& nv) {
  // Split by the '=' into name-value pairs:
  vector<string> nvpairs_vec;
  boost::split(nvpairs_vec, token, boost::is_any_of(","));

  if (nvpairs_vec.empty()) {
    return false;
  }

  // Split the name-value pairs by '=' into a map:
  vector<string>::iterator itr;
  for (itr = nvpairs_vec.begin(); itr != nvpairs_vec.end(); ++itr) {
    int sep = itr->find('=');
    string key = itr->substr(0, sep);
    string val = itr->substr(sep + 1, itr->length() - sep);
    DEBUG4 << "Name-Value Pair: " << *itr
           << " (" << key << ", " << val << ") " << endl;
    nv[key] = val;
  }
  DEBUG4 << "Map, size=" << nv.size() << endl;
  return true;
}

////////////////////////////////////////////////////////
//
// MAIN
//
int  main(int argc, char** argv)
{
  google::SetUsageMessage("Logreader: reads and publishes market data from logfile.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  const string filename = FLAGS_file;

  // Open the file inputstream
  DEBUG1 << "Opening file " << filename << endl;

  ifstream infile(filename.c_str());

  if (!infile) {
    LOG(ERROR) << "Unable to open " << filename << endl;
    return -1;
  }

  // If publish, open zmq socket:
  zmq::context_t* context = NULL;
  zmq::socket_t* socket = NULL;
  if (FLAGS_publish) {
    context = new zmq::context_t(1);
    socket = new zmq::socket_t(*context, ZMQ_PUB);
    socket->bind(FLAGS_endpoint.c_str());

    LOG(INFO) << "Publishing market data at " << FLAGS_endpoint << endl;
  }

  string token;

  MarketData* last = NULL;
  MarketData* curr = NULL;
  lab616::messaging::Message* publish = NULL;

  while (infile >> token) {
    // The tokens are space separated
    if (token.find(',') != string::npos) {
      DEBUG4 << "Log entry = " << token << endl;

      curr = new MarketData();
      map<string, string> nv;
      if (ParseMap(token, nv)) {

        uint64_t t1 = lab616::utils::now_micros();
        bool ok = process(nv, curr);
        if (ok && context != NULL) {

          if (last == NULL) {
            last = curr;
          } else {
            publish = new lab616::messaging::Message();
            publish->add(last->symbol);
            publish->add(last->event);
            publish->add(last->ts);
            publish->add(last->value);

            uint64_t dt = lab616::utils::now_micros() - t1;

            // calculate the sleep interval
            int sleep = curr->ts - last->ts - dt;

            bool filtered_hour = hour_of_day(last->ts) >= FLAGS_starthour;

            if (FLAGS_playback > 0 && filtered_hour) {
              lab616::utils::sleep_micros(sleep / FLAGS_playback);

              publish->send(*socket);
              LOG(INFO) << "["
                        << hour_of_day(last->ts) << ":"
                        << minute_of_hour(last->ts) << ":"
                        << second_of_minute(last->ts)
                        << "] "
                        << "publish "
                        << last->symbol << ' '
                        << last->event << ' '
                        << last->ts << ' '
                        << last->value << endl;

            }
            // clean up
            delete last;
            last = curr;
          }
        }
      }
    }
  }
  // The last event:
  if (last != NULL) {
    publish = new lab616::messaging::Message();
    publish->add(last->symbol);
    publish->add(last->event);
    publish->add(last->ts);
    publish->add(last->value);
    publish->send(*socket);
    delete publish;
    delete last;
  }
  infile.close();
  infile.clear();

  if (context != NULL) {
    delete socket;
    delete context;
  }
}
