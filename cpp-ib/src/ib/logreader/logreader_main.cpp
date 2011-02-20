#include "common.hpp"
#include "utils.hpp"

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
        VLOG(20) << "Name-Value Pair: " << *itr
                 << " (" << key << ", " << val << ") " << endl;
        nv[key] = val;
      }
      VLOG(20) << "Map, size=" << nv.size() << endl;
    }
  }
  infile.close();
  infile.clear();
}
