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
}
