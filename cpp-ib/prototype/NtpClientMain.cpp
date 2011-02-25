#include <iostream>
#include "NtpClient.hpp"
#include <boost/date_time/posix_time/posix_time.hpp> // Need I/O.
int main(int argc,char** argv)
{
   if( 1 == argc )
   {
      std::cout << NtpClient::GetTime() << '\n';
   }
   else if( 2 == argc )
   {
      std::cout << NtpClient::GetTime( argv[1] ) << '\n';
   }
   else
   {
      std::cerr
        << "Usage: " << argv[0]
        << " NTP-server\n  NTP-server is pool.ntp.org if not specified\n";
      return 1;
   }
   return 0;
}
