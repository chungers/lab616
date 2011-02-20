#include "common.hpp"
#include "utils.hpp"
#include "ib/ticker_id.hpp"

#include <iostream>
#include <fstream>
#include <sstream>

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


DEFINE_string(file, "logfile", "The name of the log file.");

const char* NUMERIC_EVENTS[] = { "tickPrice", "tickSize", "tickGeneric" };

static bool process(map<string, string>& nv) {
  const int LEVEL = 20;
  // There should not be a key called 'action':
  map<string, string>::iterator itr = nv.find("action");
  if (itr != nv.end()) {
    VLOG(40) << "Not a market data event: " << itr->second << endl;
    return false;
  }

  // Determine if we have a numeric event.
  bool hasMatch = false;
  map<string, string>::iterator eventItr = nv.find("event");
  if (eventItr != nv.end()) {
    for (int i = 0; i < 3; ++i) {
      if (eventItr->second.compare(NUMERIC_EVENTS[i]) == 0) {
        hasMatch = true;
        break;
      }
    }
  }
  if (!hasMatch) {
    VLOG(40) << "Not a numeric event: " << eventItr->second << endl;
    return false;
  }

  uint64_t ts;
  string symbol;
  string event;
  double value;

  // Timestamp
  map<string, string>::iterator tsItr = nv.find("ts_utc");
  if (tsItr != nv.end()) {
    char* end;
    ts = static_cast<uint64_t>(strtoll(tsItr->second.c_str(), &end, 10));
    VLOG(LEVEL) << "Timestamp ==> " << ts << endl;
  } else {
    return false;
  }

  // TickerId / Symbol:
  map<string, string>::iterator tickerIdItr = nv.find("tickerId");
  if (tickerIdItr != nv.end()) {
    int id = atoi(tickerIdItr->second.c_str());  // 4 bytes for int.
    ib::internal::SymbolFromTickerId(id, &symbol);
    VLOG(LEVEL) << "symbol ==> " << symbol << "(" << id << ")" << endl;
    nv["symbol"] = symbol;
  } else {
    return false;
  }

  // Event
  map<string, string>::iterator fieldItr = nv.find("field");
  if (fieldItr != nv.end()) {
    event = nv["field"];
    VLOG(LEVEL) << "event ==> " << event << endl;
  } else {
    return false;
  }

  // Price / Size
  itr = nv.find("price");
  if (itr != nv.end()) {
    value = atof(itr->second.c_str());
  } else {
    itr = nv.find("size");
    if (itr != nv.end()) {
      value = atof(itr->second.c_str());
    } else {
      itr = nv.find("value");
      if (itr != nv.end()) {
        value = atof(itr->second.c_str());
      } else {
        return false;
      }
    }
  }
  VLOG(LEVEL) << "value ==> " << value << endl;

  VLOG(LEVEL - 10)
      << symbol << ' '
      << event << ' '
      << ts << ' '
      << value << endl;
  return true;
}

int main(int argc, char** argv)
{
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  const string filename = FLAGS_file;

  // Open the file inputstream
  VLOG(10) << "Opening file " << filename << endl;

  ifstream infile(filename.c_str());


  if (!infile) {
    LOG(ERROR) << "Unable to open " << filename << endl;
    return -1;
  }

  string token;
  while (infile >> token) {
    // The tokens are space separated
    if (token.find(',') != string::npos) {
      VLOG(40) << "Log entry = " << token << endl;

      // Split by the '=' into name-value pairs:
      vector<string> nvpairs_vec;
      boost::split(nvpairs_vec, token, boost::is_any_of(","));

      // Split the name-value pairs by '=' into a map:
      map<string, string> nv;
      vector<string>::iterator itr;
      for (itr = nvpairs_vec.begin(); itr != nvpairs_vec.end(); ++itr) {
        int sep = itr->find('=');
        string key = itr->substr(0, sep);
        string val = itr->substr(sep + 1, itr->length() - sep);
        VLOG(30) << "Name-Value Pair: " << *itr
                 << " (" << key << ", " << val << ") " << endl;
        nv[key] = val;
      }
      VLOG(30) << "Map, size=" << nv.size() << endl;

      process(nv);
    }
  }
  infile.close();
  infile.clear();
}
