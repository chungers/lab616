#ifndef IB_SERVICES_H_
#define IB_SERVICES_H_

#include <boost/scoped_ptr.hpp>
#include <string>

using namespace std;

namespace ib {
namespace services {


// Service interface for Requesting Market Data.
class IMarketData
{
 public:
  virtual ~IMarketData() {}

  // Requests tick data for the given symbol.
  // Returns the unique ticker id.
  virtual unsigned int requestTicks(const string& symbol) = 0;
};


} // namespace services
} // namespace ib
#endif // IB_SERVICES_H_
