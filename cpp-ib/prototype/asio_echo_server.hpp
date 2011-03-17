#include <cstdlib>
#include <iostream>
#include <boost/bind.hpp>
#include <boost/asio.hpp>

#include <glog/logging.h>

using boost::asio::ip::tcp;

class EchoSession
{
public:
  EchoSession(boost::asio::io_service& io_service)
    : socket_(io_service)
  {
  }

  tcp::socket& socket()
  {
    return socket_;
  }

  void start()
  {
    LOG(INFO) << "Starting session." << std::endl;
    socket_.async_read_some(boost::asio::buffer(data_, max_length),
        boost::bind(&EchoSession::handle_read, this,
          boost::asio::placeholders::error,
          boost::asio::placeholders::bytes_transferred));
  }

  void handle_read(const boost::system::error_code& error,
      size_t bytes_transferred)
  {
    if (!error)
    {
      boost::asio::async_write(socket_,
          boost::asio::buffer(data_, bytes_transferred),
          boost::bind(&EchoSession::handle_write, this,
            boost::asio::placeholders::error));
    }
    else
    {
      LOG(ERROR) << "Read error: " << error.message() << std::endl;
      delete this;
    }
  }

  void handle_write(const boost::system::error_code& error)
  {
    if (!error)
    {
      socket_.async_read_some(boost::asio::buffer(data_, max_length),
          boost::bind(&EchoSession::handle_read, this,
            boost::asio::placeholders::error,
            boost::asio::placeholders::bytes_transferred));
    }
    else
    {
      LOG(ERROR) << "Write error: " << error.message() << std::endl;
      delete this;
    }
  }

private:
  tcp::socket socket_;
  enum { max_length = 1024 };
  char data_[max_length];
};

class EchoServer
{
public:
  EchoServer(boost::asio::io_service& io_service, short port)
    : io_service_(io_service),
      acceptor_(io_service, tcp::endpoint(tcp::v4(), port))
  {
    EchoSession* new_session = new EchoSession(io_service_);

    LOG(INFO) << "New session. Start to accept." << std::endl;

    acceptor_.async_accept(new_session->socket(),
        boost::bind(&EchoServer::handle_accept, this, new_session,
          boost::asio::placeholders::error));
  }

  void handle_accept(EchoSession* new_session,
      const boost::system::error_code& error)
  {
    if (!error)
    {
      new_session->start();
      new_session = new EchoSession(io_service_);
      acceptor_.async_accept(new_session->socket(),
          boost::bind(&EchoServer::handle_accept, this, new_session,
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
