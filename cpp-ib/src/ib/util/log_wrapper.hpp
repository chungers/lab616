#ifndef IB_UTIL_LOG_WRAPPER_H_
#define IB_UTIL_LOG_WRAPPER_H_

#include "log_common.h"

#define LOG_EVENT \
  VLOG(VLOG_LEVEL_EWRAPPER) \
  << "cid=" << connection_id_ \
  << ",ts=" << now_micros() \
  << ",event=" << __func__

// From EWrapper.h
const std::string TickType_str[] = {
  "BID_SIZE","BID","ASK","ASK_SIZE", "LAST", "LAST_SIZE","HIGH",
      "LOW", "VOLUME", "CLOSE","BID_OPTION_COMPUTATION",
      "ASK_OPTION_COMPUTATION","LAST_OPTION_COMPUTATION",
      "MODEL_OPTION","OPEN","LOW_13_WEEK","HIGH_13_WEEK",
      "LOW_26_WEEK","HIGH_26_WEEK","LOW_52_WEEK","HIGH_52_WEEK",
      "AVG_VOLUME","OPEN_INTEREST","OPTION_HISTORICAL_VOL",
      "OPTION_IMPLIED_VOL","OPTION_BID_EXCH","OPTION_ASK_EXCH",
      "OPTION_CALL_OPEN_INTEREST","OPTION_PUT_OPEN_INTEREST",
      "OPTION_CALL_VOLUME","OPTION_PUT_VOLUME","INDEX_FUTURE_PREMIUM",
      "BID_EXCH","ASK_EXCH","AUCTION_VOLUME","AUCTION_PRICE",
      "AUCTION_IMBALANCE","MARK_PRICE","BID_EFP_COMPUTATION",
      "ASK_EFP_COMPUTATION","LAST_EFP_COMPUTATION","OPEN_EFP_COMPUTATION",
      "HIGH_EFP_COMPUTATION","LOW_EFP_COMPUTATION","CLOSE_EFP_COMPUTATION",
      "LAST_TIMESTAMP","SHORTABLE","FUNDAMENTAL_RATIOS","RT_VOLUME",
      "HALTED","BID_YIELD","ASK_YIELD","LAST_YIELD",
      "CUST_OPTION_COMPUTATION","NOT_SET" };

#define __tick_type_enum(m) ",field=" << TickType_str[m]


// TODO :
// Add higher log level to log structs like OrderState and Contract.

namespace ib {
namespace util {

class LogWrapper : public EWrapper {
 public:
  LogWrapper(int connection_id) : connection_id_(connection_id) {}
  ~LogWrapper() {}

 private:
  int connection_id_;

  // EWrapper methods.
 public:
  void tickPrice(
      TickerId tickerId, TickType field, double price, int canAutoExecute) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(field)
        << __f__(price)
        << __f__(canAutoExecute);
  }
  void tickSize(
      TickerId tickerId, TickType field, int size) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(field)
        << __f__(size);
  }
  void tickOptionComputation(
      TickerId tickerId, TickType tickType,
      double impliedVol,
      double delta, double optPrice, double pvDividend,
      double gamma, double vega, double theta, double undPrice) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(tickType)
        << __f__(impliedVol)
        << __f__(delta)
        << __f__(optPrice)
        << __f__(pvDividend)
        << __f__(gamma)
        << __f__(vega)
        << __f__(theta)
        << __f__(undPrice);
  }
  void tickGeneric(
      TickerId tickerId, TickType tickType, double value) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(tickType)
        << __f__(value);
  }
  void tickString(
      TickerId tickerId, TickType tickType, const IBString& value) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(tickType)
        << __f__(value);
  }
  void tickEFP(
      TickerId tickerId, TickType tickType,
      double basisPoints, const IBString& formattedBasisPoints,
      double totalDividends, int holdDays,
      const IBString& futureExpiry, double dividendImpact,
      double dividendsToExpiry) {
    LOG_EVENT
        << __f__(tickerId)
        << __tick_type_enum(tickType)
        << __f__(basisPoints)
        << __f__(formattedBasisPoints)
        << __f__(totalDividends)
        << __f__(holdDays)
        << __f__(futureExpiry)
        << __f__(dividendImpact)
        << __f__(dividendsToExpiry);
  }
  void openOrder(
      OrderId orderId, const Contract& contract,
      const Order& order, const OrderState& state) {
    LOG_EVENT
        << __f__(orderId)
        << __f__(&contract)
        << __f__(&order)
        << __f__(&state);
  }
  void openOrderEnd() {
    LOG_EVENT;
  }
  void winError(const IBString &str, int lastError) {
    LOG_EVENT
        << __f__(str)
        << __f__(lastError);
  }
  void connectionClosed() {
    LOG_EVENT;
  }
  void updateAccountValue(
      const IBString& key, const IBString& val,
      const IBString& currency, const IBString& accountName) {
    LOG_EVENT
        << __f__(key)
        << __f__(val)
        << __f__(currency)
        << __f__(accountName);
  }
  void updatePortfolio(
      const Contract& contract, int position,
      double marketPrice, double marketValue, double averageCost,
      double unrealizedPNL, double realizedPNL, const IBString& accountName) {
    LOG_EVENT
        << __f__(&contract)
        << __f__(position)
        << __f__(marketPrice)
        << __f__(marketValue)
        << __f__(averageCost)
        << __f__(unrealizedPNL)
        << __f__(realizedPNL)
        << __f__(accountName);
  }
  void updateAccountTime(const IBString& timeStamp) {
    LOG_EVENT
        << __f__(timeStamp);
  }
  void accountDownloadEnd(const IBString& accountName) {
    LOG_EVENT
        << __f__(accountName);
  }
  void contractDetails(int reqId, const ContractDetails& contractDetails) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(&contractDetails);
  }
  void bondContractDetails(int reqId, const ContractDetails& contractDetails) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(&contractDetails);
  }
  void contractDetailsEnd(int reqId) {
    LOG_EVENT
        << __f__(reqId);
  }
  void execDetails(
      int reqId, const Contract& contract, const Execution& execution) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(&contract)
        << __f__(&execution);
  }
  void execDetailsEnd(int reqId) {
    LOG_EVENT
        << __f__(reqId);
  }
  void updateMktDepth(
      TickerId id, int position, int operation, int side,
      double price, int size) {
    LOG_EVENT
        << __f__(id)
        << __f__(position)
        << __f__(operation)
        << __f__(side)
        << __f__(price)
        << __f__(size);
  }
  void updateMktDepthL2(
      TickerId id, int position, IBString marketMaker, int operation,
      int side, double price, int size) {
    LOG_EVENT
        << __f__(id)
        << __f__(position)
        << __f__(marketMaker)
        << __f__(operation)
        << __f__(side)
        << __f__(price)
        << __f__(size);
  }
  void updateNewsBulletin(
      int msgId, int msgType, const IBString& newsMessage,
      const IBString& originExch) {
    LOG_EVENT
        << __f__(msgId)
        << __f__(msgType)
        << __f__(newsMessage)
        << __f__(originExch);
  }
  void managedAccounts(const IBString& accountsList) {
    LOG_EVENT
        << __f__(accountsList);
  }
  void receiveFA(faDataType pFaDataType, const IBString& cxml) {
    LOG_EVENT
        << __f__(pFaDataType)
        << __f__(cxml);
  }
  void historicalData(
      TickerId reqId, const IBString& date, double open, double high,
      double low, double close, int volume,
      int barCount, double WAP, int hasGaps) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(date)
        << __f__(open)
        << __f__(high)
        << __f__(low)
        << __f__(close)
        << __f__(volume)
        << __f__(barCount)
        << __f__(WAP)
        << __f__(hasGaps);
  }
  void scannerParameters(const IBString &xml) {
    LOG_EVENT
        << __f__(xml);
  }
  void scannerData(
      int reqId, int rank, const ContractDetails &contractDetails,
      const IBString &distance, const IBString &benchmark,
      const IBString &projection, const IBString &legsStr) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(rank)
        << __f__(&contractDetails)
        << __f__(distance)
        << __f__(benchmark)
        << __f__(projection)
        << __f__(legsStr);
  }
  void scannerDataEnd(int reqId) {
    LOG_EVENT
        << __f__(reqId);
  }
  void realtimeBar(
      TickerId reqId, long time,
      double open, double high, double low, double close,
      long volume, double wap, int count) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(time)
        << __f__(open)
        << __f__(high)
        << __f__(low)
        << __f__(close)
        << __f__(volume)
        << __f__(wap)
        << __f__(count);
  }
  void fundamentalData(TickerId reqId, const IBString& data) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(data);
  }
  void deltaNeutralValidation(int reqId, const UnderComp& underComp) {
    LOG_EVENT
        << __f__(reqId)
        << __f__(&underComp);
  }
  void tickSnapshotEnd(int reqId) {
    LOG_EVENT
        << __f__(reqId);
  }
  void orderStatus(
      OrderId orderId, const IBString &status, int filled,
      int remaining, double avgFillPrice, int permId, int parentId,
      double lastFillPrice, int clientId,
      const IBString& whyHeld) {
    LOG_EVENT
        << __f__(orderId)
        << __f__(status)
        << __f__(filled)
        << __f__(remaining)
        << __f__(avgFillPrice)
        << __f__(permId)
        << __f__(parentId)
        << __f__(lastFillPrice)
        << __f__(clientId)
        << __f__(whyHeld);
  }
  void nextValidId(OrderId orderId) {
    LOG_EVENT
        << __f__(orderId);
  }
  void currentTime(long time) {
    LOG_EVENT
        << __f__(time);
  }
  void error(
      const int id, const int errorCode,
      const IBString errorString) {
    LOG_EVENT
        << __f__(id)
        << __f__(errorCode)
        << __f__(errorString);
  }

};

class MarketData : LogWrapper {
 public:
  MarketData(int id) : LogWrapper(id) {};
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
#endif // IB_UTIL_LOG_WRAPPER_H_

