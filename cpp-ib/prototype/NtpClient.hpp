#ifndef NTPCLIENT_HPP
#define NTPCLIENT_HPP
#include <boost/asio.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>
class NtpClient
{
   public:
      static boost::posix_time::ptime GetTime()
      {
         return GetTime("pool.ntp.org");
      };
      static boost::posix_time::ptime GetTime( const char* ntpServer )
      {
         using boost::asio::ip::udp;
         boost::asio::io_service io_service;

         udp::resolver resolver(io_service);
         udp::resolver::query query(udp::v4(), ntpServer, "ntp");
         udp::endpoint receiver_endpoint = *resolver.resolve(query);

         udp::endpoint sender_endpoint;

         boost::uint8_t data[48] = {
            0x1B,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
         };

         udp::socket socket(io_service);
         socket.open(udp::v4());

         socket.send_to(
               boost::asio::buffer(data),
               receiver_endpoint);
         socket.receive_from(
               boost::asio::buffer(data),
               sender_endpoint);

         typedef boost::uint32_t u32;
         const u32 iPart(
               static_cast<u32>(data[40]) << 24
               | static_cast<u32>(data[41]) << 16
               | static_cast<u32>(data[42]) << 8
               | static_cast<u32>(data[43])
               );
         const u32 fPart(
               static_cast<u32>(data[44]) << 24
               | static_cast<u32>(data[45]) << 16
               | static_cast<u32>(data[46]) << 8
               | static_cast<u32>(data[47])
               );

         using namespace boost::posix_time;
         const ptime pt(
               boost::gregorian::date(1900,1,1),
               milliseconds(
                  iPart * 1.0E3
                  + fPart * 1.0E3 / 0x100000000ULL )
               );
         return pt;
      };
};
#endif
