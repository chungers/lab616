#include <cstdlib>
#include <cstring>
#include <iostream>
#include <sstream>
#include <boost/asio.hpp>

#include <glog/logging.h>

using boost::asio::ip::tcp;

class EchoClient
{
 public:
  EchoClient(boost::asio::io_service& io_service,
             const std::string& server, const short port)
      : socket_(io_service)
  {
    std::ostringstream service_name;
    service_name << port;
    tcp::resolver resolver(io_service);
    tcp::resolver::query query(tcp::v4(), server, service_name.str());
    tcp::resolver::iterator iterator = resolver.resolve(query);

    socket_.connect(*iterator);

    LOG(INFO) << "Connected.";
  }

  tcp::socket& socket() {
    return socket_;
  }

  void start() {
    try {
      using namespace std; // For strlen.
      std::cout << "Enter message: ";
      char request[max_length];
      std::cin.getline(request, max_length);
      size_t request_length = strlen(request);
      boost::asio::write(socket_, boost::asio::buffer(request, request_length));

      char reply[max_length];
      size_t reply_length =
          boost::asio::read(socket_,
                            boost::asio::buffer(reply, request_length));
      std::cout << "Reply is: ";
      std::cout.write(reply, reply_length);
      std::cout << "\n";
    }
    catch (std::exception& e) {
      std::cerr << "Exception: " << e.what() << "\n";
    }
  }

 private:
  tcp::socket socket_;
  enum { max_length = 1024 };
  char data_[max_length];

};
  /*
int main(int argc, char* argv[])
{
  try
  {
    if (argc != 3)
    {
      std::cerr << "Usage: blocking_tcp_echo_client <host> <port>\n";
      return 1;
    }

    boost::asio::io_service io_service;

    tcp::resolver resolver(io_service);
    tcp::resolver::query query(tcp::v4(), argv[1], argv[2]);
    tcp::resolver::iterator iterator = resolver.resolve(query);

    tcp::socket s(io_service);
    s.connect(*iterator);

    using namespace std; // For strlen.
    std::cout << "Enter message: ";
    char request[max_length];
    std::cin.getline(request, max_length);
    size_t request_length = strlen(request);
    boost::asio::write(s, boost::asio::buffer(request, request_length));

    char reply[max_length];
    size_t reply_length = boost::asio::read(s,
        boost::asio::buffer(reply, request_length));
    std::cout << "Reply is: ";
    std::cout.write(reply, reply_length);
    std::cout << "\n";
  }
  catch (std::exception& e)
  {
    std::cerr << "Exception: " << e.what() << "\n";
  }

  return 0;
}
  */
