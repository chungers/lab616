
#include <iostream>
#include <istream>
#include <ostream>
#include <string>
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include "asio_echo_server.hpp"
#include "asio_http.hpp"

#include <gflags/gflags.h>
#include <glog/logging.h>

using boost::asio::ip::tcp;

class HttpClient;

DEFINE_string(test, "http", "Which test to run.");
DEFINE_string(host, "www.boost.org", "The host to connect to.");
DEFINE_string(path, "/LICENSE_1_0.txt", "The path.");
DEFINE_int32(port, 7777, "Port number (for EchoServer).");

int main(int argc, char* argv[])
{
  google::SetUsageMessage("Prototype for the mongoose httpd.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);

  boost::asio::io_service io_service;

  LOG(INFO) << "io service started." << std::endl;

  try {

    if (FLAGS_test.compare("http") == 0) {

      LOG(INFO) << "Starting http client." << std::endl;
      HttpClient c(io_service, FLAGS_host, FLAGS_path);
      io_service.run();

    } else if (FLAGS_test.compare("echoServer") == 0) {

      LOG(INFO) << "Starting echo server." << std::endl;
      EchoServer s(io_service, (short)FLAGS_port);
      io_service.run();
    }

  } catch (std::exception& e) {
    std::cout << "Exception: " << e.what() << "\n";
  }
  return 0;
}
