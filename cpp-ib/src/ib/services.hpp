#ifndef IB_SERVICES_H_
#define IB_SERVICES_H_

#include <string>
#include <boost/scoped_ptr.hpp>

using namespace std;

namespace ib {
namespace services {


// Service interface for Requesting Market Data.
class MarketDataInterface
{
 public:
  virtual ~MarketDataInterface() {}

  // Dow = (INDU, NYSE), S&P = (SPX, CBOE), VIX = (VIX, CBOE)
  virtual unsigned int RequestIndex(const string& symbol,
                                    const string& exchange) = 0;

  // Requests tick data for the given symbol.
  // Returns the unique ticker id.
  virtual unsigned int RequestTicks(const string& symbol,
                                    bool marketDepth) = 0;

  enum OptionType { CALL = 0, PUT = 1 };

  virtual unsigned int RequestOptionChain(const string& symbol,
                                          OptionType option_type,
                                          int year, int month, int day) = 0;

  // Option data
  virtual unsigned int RequestOptionData(const string& symbol,
                                         OptionType option_type,
                                         double strike,
                                         int year, const int month,
                                         int day,
                                         bool marketDepth) = 0;

  // Cancels the requested data.  The id determines stock or option.
  virtual void CancelMarketData(unsigned int id) = 0;
};


} // namespace services
} // namespace ib
#endif // IB_SERVICES_H_
