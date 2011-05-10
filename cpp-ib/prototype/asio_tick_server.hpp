#include <cstdlib>

#include <stdio.h>
#include <iostream>
#include <boost/bind.hpp>
#include <boost/asio.hpp>

#include <glog/logging.h>

using boost::asio::ip::tcp;

class TickSession
{
public:
  TickSession(boost::asio::io_service& io_service)
    : socket_(io_service)
  {
  }

  tcp::socket& socket()
  {
    return socket_;
  }

  void start()
  {
    LOG(INFO) << "Starting sending ticks." << std::endl;

    sleep(1);
    std::string msg("hello");
    LOG(INFO) << "Sending " << msg << std::endl;

    boost::asio::async_write(socket_,
                             boost::asio::buffer(msg),
                             boost::bind(&TickSession::handle_write, this,
                                         boost::asio::placeholders::error));

    LOG(INFO) << "Event submitted." << std::endl;
  }

  void handle_write(const boost::system::error_code& error)
  {
    if (!error) {
      LOG(INFO) << "Sent " << std::endl;
      start();
    }
    else {
      LOG(ERROR) << "Write error: " << error.message() << std::endl;
      delete this;
    }
  }

private:
  tcp::socket socket_;
  enum { max_length = 1024 };
  char data_[max_length];
};

class TickServer
{
public:
  TickServer(boost::asio::io_service& io_service, short port)
    : io_service_(io_service),
      acceptor_(io_service, tcp::endpoint(tcp::v4(), port))
  {
    TickSession* new_session = new TickSession(io_service_);

    LOG(INFO) << "New session. Start to accept." << std::endl;

    acceptor_.async_accept(new_session->socket(),
        boost::bind(&TickServer::handle_accept, this, new_session,
          boost::asio::placeholders::error));
  }

  void handle_accept(TickSession* new_session,
      const boost::system::error_code& error)
  {
    if (!error)
    {
      new_session->start();

      new_session = new TickSession(io_service_);
      acceptor_.async_accept(new_session->socket(),
          boost::bind(&TickServer::handle_accept, this, new_session,
            boost::asio::placeholders::error));
    }
    else
    {
      LOG(ERROR) << "Error accept: " << error.message() << std::endl;

      delete new_session;
    }
  }

private:
  boost::asio::io_service& io_service_;
  tcp::acceptor acceptor_;
};
