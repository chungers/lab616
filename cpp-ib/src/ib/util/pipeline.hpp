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


#include <sys/time.h>
typedef uint64_t int64;
inline int64 now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<int64>(tv.tv_sec) * 1000000 + tv.tv_usec;
}
#define TS "ts=" << now_micros() << ","



// Verbose level.  Use flag --v=N where N >= VLOG_LEVEL to see.
#define VLOG_LEVEL 1
#define LOG_EVENT VLOG(VLOG_LEVEL) << TS << "event=" << __func__
#define __f__(m) "" << ',' << #m << '=' << m

// TODO :
// Add higher log level to log structs like OrderState and Contract.

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
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(field) <<
        __f__(price) <<
        __f__(canAutoExecute);
  }
  void tickSize(
      TickerId tickerId, TickType field, int size) {
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(field) <<
        __f__(size);
  }
  void tickOptionComputation(
      TickerId tickerId, TickType tickType,
      double impliedVol,
      double delta, double optPrice, double pvDividend,
      double gamma, double vega, double theta, double undPrice) {
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(tickType) <<
        __f__(impliedVol) <<
        __f__(delta) <<
        __f__(optPrice) <<
        __f__(pvDividend) <<
        __f__(gamma) <<
        __f__(vega) <<
        __f__(theta) <<
        __f__(undPrice);
  }
  void tickGeneric(
      TickerId tickerId, TickType tickType, double value) {
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(tickType) <<
        __f__(value);
  }
  void tickString(
      TickerId tickerId, TickType tickType, const IBString& value) {
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(tickType) <<
        __f__(value);
  }
  void tickEFP(
      TickerId tickerId, TickType tickType,
      double basisPoints, const IBString& formattedBasisPoints,
      double totalDividends, int holdDays,
      const IBString& futureExpiry, double dividendImpact,
      double dividendsToExpiry) {
    LOG_EVENT <<
        __f__(tickerId) <<
        __f__(tickType) <<
        __f__(basisPoints) <<
        __f__(formattedBasisPoints) <<
        __f__(totalDividends) <<
        __f__(holdDays) <<
        __f__(futureExpiry) <<
        __f__(dividendImpact) <<
        __f__(dividendsToExpiry);
  }
  void openOrder(
      OrderId orderId, const Contract& contract,
      const Order& order, const OrderState& state) {
    LOG_EVENT <<
        __f__(orderId) <<
        __f__(&contract) <<
        __f__(&order) <<
        __f__(&state);
  }
  void openOrderEnd() {
    LOG_EVENT;
  }
  void winError(const IBString &str, int lastError) {
    LOG_EVENT <<
        __f__(str) <<
        __f__(lastError);
  }
  void connectionClosed() {
    LOG_EVENT;
  }
  void updateAccountValue(
      const IBString& key, const IBString& val,
      const IBString& currency, const IBString& accountName) {
    LOG_EVENT <<
        __f__(key) <<
        __f__(val) <<
        __f__(currency) <<
        __f__(accountName);
  }
  void updatePortfolio(
      const Contract& contract, int position,
      double marketPrice, double marketValue, double averageCost,
      double unrealizedPNL, double realizedPNL, const IBString& accountName) {
    LOG_EVENT <<
        __f__(&contract) <<
        __f__(position) <<
        __f__(marketPrice) <<
        __f__(marketValue) <<
        __f__(averageCost) <<
        __f__(unrealizedPNL) <<
        __f__(realizedPNL) <<
        __f__(accountName);
  }
  void updateAccountTime(const IBString& timeStamp) {
    LOG_EVENT <<
        __f__(timeStamp);
  }
  void accountDownloadEnd(const IBString& accountName) {
    LOG_EVENT <<
        __f__(accountName);
  }
  void contractDetails(int reqId, const ContractDetails& contractDetails) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(&contractDetails);
  }
  void bondContractDetails(int reqId, const ContractDetails& contractDetails) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(&contractDetails);
  }
  void contractDetailsEnd(int reqId) {
    LOG_EVENT <<
        __f__(reqId);
  }
  void execDetails(
      int reqId, const Contract& contract, const Execution& execution) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(&contract) <<
        __f__(&execution);
  }
  void execDetailsEnd(int reqId) {
    LOG_EVENT <<
        __f__(reqId);
  }
  void updateMktDepth(
      TickerId id, int position, int operation, int side,
      double price, int size) {
    LOG_EVENT <<
        __f__(id) <<
        __f__(position) <<
        __f__(operation) <<
        __f__(side) <<
        __f__(price) <<
        __f__(size);
  }
  void updateMktDepthL2(
      TickerId id, int position, IBString marketMaker, int operation,
      int side, double price, int size) {
    LOG_EVENT <<
        __f__(id) <<
        __f__(position) <<
        __f__(marketMaker) <<
        __f__(operation) <<
        __f__(side) <<
        __f__(price) <<
        __f__(size);
  }
  void updateNewsBulletin(
      int msgId, int msgType, const IBString& newsMessage,
      const IBString& originExch) {
    LOG_EVENT <<
        __f__(msgId) <<
        __f__(msgType) <<
        __f__(newsMessage) <<
        __f__(originExch);
  }
  void managedAccounts(const IBString& accountsList) {
    LOG_EVENT <<
        __f__(accountsList);
  }
  void receiveFA(faDataType pFaDataType, const IBString& cxml) {
    LOG_EVENT <<
        __f__(pFaDataType) <<
        __f__(cxml);
  }
  void historicalData(
      TickerId reqId, const IBString& date, double open, double high,
      double low, double close, int volume,
      int barCount, double WAP, int hasGaps) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(date) <<
        __f__(open) <<
        __f__(high) <<
        __f__(low) <<
        __f__(close) <<
        __f__(volume) <<
        __f__(barCount) <<
        __f__(WAP) <<
        __f__(hasGaps);
  }
  void scannerParameters(const IBString &xml) {
    LOG_EVENT <<
        __f__(xml);
  }
  void scannerData(
      int reqId, int rank, const ContractDetails &contractDetails,
      const IBString &distance, const IBString &benchmark,
      const IBString &projection, const IBString &legsStr) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(rank) <<
        __f__(&contractDetails) <<
        __f__(distance) <<
        __f__(benchmark) <<
        __f__(projection) <<
        __f__(legsStr);
  }
  void scannerDataEnd(int reqId) {
    LOG_EVENT <<
        __f__(reqId);
  }
  void realtimeBar(
      TickerId reqId, long time,
      double open, double high, double low, double close,
      long volume, double wap, int count) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(time) <<
        __f__(open) <<
        __f__(high) <<
        __f__(low) <<
        __f__(close) <<
        __f__(volume) <<
        __f__(wap) <<
        __f__(count);
  }
  void fundamentalData(TickerId reqId, const IBString& data) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(data);
  }
  void deltaNeutralValidation(int reqId, const UnderComp& underComp) {
    LOG_EVENT <<
        __f__(reqId) <<
        __f__(&underComp);
  }
  void tickSnapshotEnd(int reqId) {
    LOG_EVENT <<
        __f__(reqId);
  }
  void orderStatus(
      OrderId orderId, const IBString &status, int filled,
      int remaining, double avgFillPrice, int permId, int parentId,
      double lastFillPrice, int clientId,
      const IBString& whyHeld) {
    LOG_EVENT <<
        __f__(orderId) <<
        __f__(status) <<
        __f__(filled) <<
        __f__(remaining) <<
        __f__(avgFillPrice) <<
        __f__(permId) <<
        __f__(parentId) <<
        __f__(lastFillPrice) <<
        __f__(clientId) <<
        __f__(whyHeld);
  }
  void nextValidId(OrderId orderId) {
    LOG_EVENT <<
        __f__(orderId);
  }
  void currentTime(long time) {
    LOG_EVENT <<
        __f__(time);
  }
  void error(
      const int id, const int errorCode,
      const IBString errorString) {
    LOG_EVENT <<
        __f__(id) <<
        __f__(errorCode) <<
        __f__(errorString);
  }

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

