#ifndef IB_INTERNAL_MARKETDATA_H_
#define IB_INTERNAL_MARKETDATA_H_

#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

#include <Shared/Contract.h>
#include <Shared/EClient.h>
#include <Shared/Order.h>

#include <ib/services.hpp>

using namespace std;

namespace ib {
namespace internal {


class MarketDataImpl : public ib::services::MarketDataInterface
{
 public:
  MarketDataImpl(EClient* eclient);
  ~MarketDataImpl();

 private:
  EClient* eclient_;

 public:
  virtual unsigned int RequestIndex(const string& symbol,
                                    const string& exchange);

  virtual unsigned int RequestContractDetails(const string& symbol);

  virtual unsigned int RequestTicks(const string& symbol,
                                    bool marketDepth);

  virtual unsigned int RequestOptionChain(const string& symbol,
                                          OptionType option_type,
                                          int year, int month, int day);

  virtual unsigned int RequestOptionData(const string& symbol,
                                         OptionType option_type,
                                         double strike,
                                         int year,
                                         int month,
                                         int day,
                                         bool marketDepth);

  virtual void CancelMarketData(unsigned int id);
};


}; // namespace internal
}; // namespace ib

#endif // IB_INTERNAL_MARKETDATA_H_
