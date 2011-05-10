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
  TickSession(boost::asio::io_service& io_service, int delay)
      : socket_(io_service), delay_(delay)
  {
  }

  tcp::socket& socket()
  {
    return socket_;
  }

  void start()
  {
    LOG(INFO) << "Starting sending ticks." << std::endl;

    sleep(delay_);
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
  int delay_;
  enum { max_length = 1024 };
  char data_[max_length];
};

class TickServer
{
public:
  TickServer(boost::asio::io_service& io_service, short port, int delay)
    : io_service_(io_service),
      acceptor_(io_service, tcp::endpoint(tcp::v4(), port)), delay_(delay)
  {
    TickSession* new_session = new TickSession(io_service_, delay_);

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

      new_session = new TickSession(io_service_, delay_);
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
  int delay_;
};
