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


class MarketDataImpl : public ib::services::IMarketData
{
 public:
  MarketDataImpl(EClient* eclient);
  ~MarketDataImpl();

 private:
  EClient* eclient_;

 public:
  virtual unsigned int requestTicks(const string& symbol);

  virtual unsigned int requestOptionData(const string& symbol,
                                         bool call,
                                         const double strike,
                                         const int year,
                                         const int month,
                                         const int day);
};


}; // namespace internal
}; // namespace ib

#endif // IB_INTERNAL_MARKETDATA_H_
