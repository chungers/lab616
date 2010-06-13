#ifndef IB_UTIL_PIPELINE_H_
#define IB_UTIL_PIPELINE_H_

// Required
#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

// API includes. See the CMakeList.txt file for the
// actual include directory under src/ib/api.
#include "Shared/Contract.h"
#include "Shared/Order.h"
#include "Shared/EWrapper.h"

#include <glog/logging.h>
#include <memory>

namespace ib {
namespace util {

class LogWrapper : public EWrapper {
 public:
  LogWrapper() {}
  ~LogWrapper() {}

  // EWrapper methods.
 public:
  void tickPrice(
      TickerId tickerId, TickType field, double price, int canAutoExecute) {
    VLOG(1) <<
        "tickPrice:" << tickerId << "," << field <<
        "," << price << "," << canAutoExecute;
  }
  void tickSize(
      TickerId tickerId, TickType field, int size) {}
  void tickOptionComputation(
      TickerId tickerId, TickType tickType,
      double impliedVol,
      double delta, double optPrice, double pvDividend,
      double gamma, double vega, double theta, double undPrice);
  void tickGeneric(
      TickerId tickerId, TickType tickType, double value) {}
  void tickString(
      TickerId tickerId, TickType tickType, const IBString& value) {}
  void tickEFP(
      TickerId tickerId, TickType tickType,
      double basisPoints, const IBString& formattedBasisPoints,
      double totalDividends, int holdDays,
      const IBString& futureExpiry, double dividendImpact,
      double dividendsToExpiry) {}
  void openOrder(
      OrderId orderId, const Contract&,
      const Order&, const OrderState& state) {}
  void openOrderEnd();
  void winError(const IBString &str, int lastError) {}
  void connectionClosed() {}
  void updateAccountValue(
      const IBString& key, const IBString& val,
      const IBString& currency, const IBString& accountName) {}
  void updatePortfolio(
      const Contract& contract, int position,
      double marketPrice, double marketValue, double averageCost,
      double unrealizedPNL, double realizedPNL, const IBString& accountName) {}
  void updateAccountTime(const IBString& timeStamp) {}
  void accountDownloadEnd(const IBString& accountName) {}
  void contractDetails(int reqId, const ContractDetails& contractDetails) {}
  void bondContractDetails(int reqId, const ContractDetails& contractDetails) {}
  void contractDetailsEnd(int reqId) {}
  void execDetails(
      int reqId, const Contract& contract, const Execution& execution) {}
  void execDetailsEnd(int reqId) {}
  void updateMktDepth(
      TickerId id, int position, int operation, int side,
      double price, int size) {}
  void updateMktDepthL2(
      TickerId id, int position, IBString marketMaker, int operation,
      int side, double price, int size) {}
  void updateNewsBulletin(
      int msgId, int msgType, const IBString& newsMessage,
      const IBString& originExch) {}
  void managedAccounts(const IBString& accountsList) {}
  void receiveFA(faDataType pFaDataType, const IBString& cxml) {}
  void historicalData(
      TickerId reqId, const IBString& date, double open, double high,
      double low, double close, int volume,
      int barCount, double WAP, int hasGaps) {}
  void scannerParameters(const IBString &xml) {}
  void scannerData(
      int reqId, int rank, const ContractDetails &contractDetails,
      const IBString &distance, const IBString &benchmark,
      const IBString &projection, const IBString &legsStr) {}
  void scannerDataEnd(int reqId) {}
  void realtimeBar(
      TickerId reqId, long time,
      double open, double high, double low, double close,
      long volume, double wap, int count) {}

  void fundamentalData(TickerId reqId, const IBString& data) {}
  void deltaNeutralValidation(int reqId, const UnderComp& underComp) {}
  void tickSnapshotEnd(int reqId) {}

  void orderStatus(OrderId orderId, const IBString &status, int filled,
                   int remaining, double avgFillPrice, int permId, int parentId,
                   double lastFillPrice, int clientId,
                   const IBString& whyHeld) {}
  void nextValidId(OrderId orderId) {}
  void currentTime(long time) {}
  void error(const int id, const int errorCode,
             const IBString errorString) {}

};

class MarketData : LogWrapper {
 public:
  MarketData() {}
  ~MarketData() {}

 public: // EWrapper events
  void tickPrice(
      TickerId tickerId, TickType field, double price, int canAutoExecute) = 0;

  void tickSize(
      TickerId tickerId, TickType field, int size) = 0;

  void tickOptionComputation(
      TickerId tickerId, TickType tickType,
      double impliedVol,
      double delta, double optPrice, double pvDividend,
      double gamma, double vega, double theta, double undPrice) = 0;

  void tickGeneric(
      TickerId tickerId, TickType tickType, double value) = 0;

  void tickString(
      TickerId tickerId, TickType tickType, const IBString& value) = 0;
  void tickEFP(
      TickerId tickerId, TickType tickType,
      double basisPoints, const IBString& formattedBasisPoints,
      double totalDividends, int holdDays,
      const IBString& futureExpiry, double dividendImpact,
      double dividendsToExpiry) = 0;
};
}
}
#endif // IB_UTIL_PIPELINE_H_
