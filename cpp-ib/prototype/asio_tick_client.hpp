#include <cstdlib>
#include <cstring>
#include <iostream>
#include <sstream>
#include <boost/asio.hpp>
#include <boost/bind.hpp>

#include <gflags/gflags.h>
#include <glog/logging.h>

using boost::asio::ip::tcp;

DEFINE_bool(asyncConnect, false, "True if connect is handled asynchronously.");

class TickClient
{
 public:
  TickClient(boost::asio::io_service& io_service,
             const std::string& server, const short port)
      : socket_(io_service)
  {
    if (FLAGS_asyncConnect) {
      std::ostringstream service_name;
      service_name << port;
      tcp::resolver resolver(io_service);
      tcp::resolver::query query(tcp::v4(), server, service_name.str());
      tcp::resolver::iterator iterator = resolver.resolve(query);
      tcp::endpoint endpoint = *iterator;
      socket_.async_connect(endpoint,
                            boost::bind(&TickClient::handle_connect,
                                        this, boost::asio::placeholders::error,
                                        ++iterator));
      LOG(INFO) << "Set up connection handler.";
    } else {
      LOG(INFO) << "Connect synchronously.";
      // Connect synchronously.
      // Here the server string must be an ip address (eg. 127.0.0.1)
      tcp::endpoint endpoint(boost::asio::ip::address::from_string(server), port);
      bool ok = false;
      try {
        socket_.connect(endpoint);
        ok = true;

        LOG(INFO) << "Connected without error." << std::endl;

        // Set up the async read handler:
        socket_.async_receive(boost::asio::buffer(data_), 0,
                              boost::bind(&TickClient::handle_event, this,
                                          boost::asio::placeholders::error,
                                          boost::asio::placeholders::bytes_transferred));
      } catch (boost::system::system_error e) {
        LOG(WARNING) << "Exception while connecting: " << e.what() << std::endl;
      }
    }
  }

  tcp::socket& socket() {
    return socket_;
  }

 private:

  void handle_connect(const boost::system::error_code& error,
                      tcp::resolver::iterator endpoint_iterator) {
    if (!error) {
      LOG(INFO) << "Connected without error." << std::endl;

      // Set up the async read handler:
      socket_.async_receive(boost::asio::buffer(data_),
                            0,
                            boost::bind(&TickClient::handle_event, this,
                                        boost::asio::placeholders::error,
                                        boost::asio::placeholders::bytes_transferred));

      LOG(INFO) << "Set up read handler." << std::endl;

    } else if (endpoint_iterator != tcp::resolver::iterator()) {
      // The case where we have another end point to connect to and should
      // close the previous connection.
      socket_.close();
      tcp::endpoint next = *endpoint_iterator;
      socket_.async_connect(next,
                            boost::bind(&TickClient::handle_connect,
                                        this,
                                        boost::asio::placeholders::error,
                                        ++endpoint_iterator));
      LOG(INFO) << "Reconnecting to another endpoint." << std::endl;
    }
  }

  void handle_event(const boost::system::error_code& error,
                    size_t bytes_read) {
    LOG(INFO) << "handle event (" << bytes_read << " bytes read) " << std::endl;

    if (!error) {
      LOG(INFO) << "Got event = " << data_ << std::endl;

      // Set up the async read handler:
      socket_.async_receive(boost::asio::buffer(data_), 0,
                            boost::bind(&TickClient::handle_event, this,
                                        boost::asio::placeholders::error,
                                        boost::asio::placeholders::bytes_transferred));
    } else {
      LOG(ERROR) << "Got error:" << error.message() << std::endl;
    }
  }

  tcp::socket socket_;
  enum { max_length = 20, msg_length = 5 };
  char data_[max_length];

};
