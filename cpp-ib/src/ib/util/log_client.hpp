#ifndef IB_UTIL_LOG_CLIENT_H_
#define IB_UTIL_LOG_CLIENT_H_

#include "log_common.h"

#define LOG_START \
  VLOG(VLOG_LEVEL_ECLIENT - 1) \
  << "id=" << connection_id_ \
  << ",ts=" << (call_start_ = now_micros()) \
  << ",action=" << __func__

#define LOG_END \
  VLOG(VLOG_LEVEL_ECLIENT) \
  << "id=" << connection_id_ \
  << ",ts=" << (call_start_ = now_micros()) \
  << ",action=" << __func__ \
  << ",elapsed=" << (now_micros() - call_start_)

#define __f__(m) "" << ',' << #m << '=' << m

namespace ib {
namespace util {

class LogClientSocket : public EPosixClientSocket {

 public:
  LogClientSocket(int connection_id, EWrapper* wrapper) :
      EPosixClientSocket::EPosixClientSocket(wrapper),
      connection_id_(connection_id) {}
  ~LogClientSocket() {}

 private:
  const int connection_id_;
  int64 call_start_;

 public:
  bool eConnect(const char *host, unsigned int port, int clientId=0) {
    LOG_START <<
        __f__(host) <<
        __f__(port) <<
        __f__(clientId);
    bool v = EPosixClientSocket::eConnect(host, port, clientId);
    LOG_END;
    return v;
  }
  void eDisconnect() {
    LOG_START;
    EPosixClientSocket::eDisconnect();
    LOG_END;
  }
  int serverVersion() {
    LOG_START;
    int v = EPosixClientSocket::serverVersion();
    LOG_END;
    return v;
  }
  IBString TwsConnectionTime() {
    LOG_START;
    IBString v = EPosixClientSocket::TwsConnectionTime();
    LOG_END;
    return v;
  }
  void reqMktData(TickerId id, const Contract &contract,
                  const IBString& genericTicks, bool snapshot) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(genericTicks)
        << __f__(snapshot);
    EPosixClientSocket::reqMktData(id, contract, genericTicks, snapshot);
    LOG_END;
  }
  void cancelMktData(TickerId id) {
    LOG_START;
    LOG_END;
 }
  void placeOrder(OrderId id, const Contract &contract, const Order &order) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(&order);
    EPosixClientSocket::placeOrder(id, contract, order);
    LOG_END;
 }
  void cancelOrder(OrderId id) {
    LOG_START
        << __f__(id);
    EPosixClientSocket::cancelOrder(id);
    LOG_END;
 }
  void reqOpenOrders() {
    LOG_START;
    EPosixClientSocket::reqOpenOrders();
    LOG_END;
 }
  void reqAccountUpdates(bool subscribe, const IBString& acctCode) {
    LOG_START
        << __f__(subscribe)
        << __f__(acctCode);
    EPosixClientSocket::reqAccountUpdates(subscribe, acctCode);
    LOG_END;
 }
  void reqExecutions(int reqId, const ExecutionFilter& filter) {
    LOG_START
        << __f__(reqId)
        << __f__(&filter);
    EPosixClientSocket::reqExecutions(reqId, filter);
    LOG_END;
 }
  void reqIds(int numIds) {
    LOG_START
        << __f__(numIds);
    EPosixClientSocket::reqIds(numIds);
    LOG_END;
 }
  bool checkMessages() {
    LOG_START;
    bool v = EPosixClientSocket::checkMessages();
    LOG_END;
    return v;
 }
  void reqContractDetails(int reqId, const Contract &contract) {
    LOG_START
        << __f__(reqId)
        << __f__(&contract);
    EPosixClientSocket::reqContractDetails(reqId, contract);
    LOG_END;
 }
  void reqMktDepth(TickerId id, const Contract &contract, int numRows) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(numRows);
    EPosixClientSocket::reqMktDepth(id, contract, numRows);
    LOG_END;
 }
  void cancelMktDepth(TickerId id) {
    LOG_START
        << __f__(id);
    EPosixClientSocket::cancelMktDepth(id);
    LOG_END;
 }
  void reqNewsBulletins(bool allMsgs) {
    LOG_START
        << __f__(allMsgs);
    EPosixClientSocket::reqNewsBulletins(allMsgs);
    LOG_END;
 }
  void cancelNewsBulletins() {
    LOG_START;
    EPosixClientSocket::cancelNewsBulletins();
    LOG_END;
 }
  void setServerLogLevel(int level) {
    LOG_START
        << __f__(level);
    EPosixClientSocket::setServerLogLevel(level);
    LOG_END;
 }
  void reqAutoOpenOrders(bool bAutoBind) {
    LOG_START
        << __f__(bAutoBind);
    EPosixClientSocket::reqAutoOpenOrders(bAutoBind);
    LOG_END;
 }
  void reqAllOpenOrders() {
    LOG_START;
    EPosixClientSocket::reqAllOpenOrders();
    LOG_END;
 }
  void reqManagedAccts() {
    LOG_START;
    EPosixClientSocket::reqManagedAccts();
    LOG_END;
 }
  void requestFA(faDataType pFaDataType) {
    LOG_START
        << __f__(pFaDataType);
    EPosixClientSocket::requestFA(pFaDataType);
    LOG_END;
 }
  void replaceFA(faDataType pFaDataType, const IBString& cxml) {
    LOG_START
        << __f__(pFaDataType)
        << __f__(cxml);
    EPosixClientSocket::replaceFA(pFaDataType, cxml);
    LOG_END;
 }
  void reqHistoricalData(
      TickerId id, const Contract &contract,
      const IBString &endDateTime,
      const IBString &durationStr, const IBString &barSizeSetting,
      const IBString &whatToShow, int useRTH, int formatDate) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(endDateTime)
        << __f__(durationStr)
        << __f__(barSizeSetting)
        << __f__(whatToShow)
        << __f__(useRTH)
        << __f__(formatDate);
    EPosixClientSocket::reqHistoricalData(
        id, contract, endDateTime,
        durationStr, barSizeSetting,
        whatToShow, useRTH, formatDate);
    LOG_END;
 }
  void exerciseOptions(
      TickerId id, const Contract &contract,
      int exerciseAction, int exerciseQuantity,
      const IBString &account, int override) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(exerciseAction)
        << __f__(exerciseQuantity)
        << __f__(account)
        << __f__(override);
    EPosixClientSocket::exerciseOptions(
        id, contract,
        exerciseAction, exerciseQuantity,
        account, override);
    LOG_END;
 }
  void cancelHistoricalData(TickerId tickerId ) {
    LOG_START
        << __f__(tickerId);
    EPosixClientSocket::cancelHistoricalData(tickerId);
    LOG_END;
 }
  void reqRealTimeBars(TickerId id, const Contract &contract, int barSize,
                       const IBString &whatToShow, bool useRTH) {
    LOG_START
        << __f__(id)
        << __f__(&contract)
        << __f__(barSize)
        << __f__(whatToShow)
        << __f__(useRTH);
    EPosixClientSocket::reqRealTimeBars(id, contract, barSize,
                             whatToShow, useRTH);
    LOG_END;
 }
  void cancelRealTimeBars(TickerId tickerId) {
    LOG_START
        << __f__(tickerId);
    EPosixClientSocket::cancelRealTimeBars(tickerId);
    LOG_END;
 }
  void cancelScannerSubscription(int tickerId) {
    LOG_START
        << __f__(tickerId);
    EPosixClientSocket::cancelScannerSubscription(tickerId);
    LOG_END;
 }
  void reqScannerParameters() {
    LOG_START;
    EPosixClientSocket::reqScannerParameters();
    LOG_END;
 }
  void reqScannerSubscription(
      int tickerId, const ScannerSubscription &subscription) {
    LOG_START
        << __f__(tickerId)
        << __f__(&subscription);
    EPosixClientSocket::reqScannerSubscription(tickerId, subscription);
    LOG_END;
 }
  void reqCurrentTime() {
    LOG_START;
    EPosixClientSocket::reqCurrentTime();
    LOG_END;
 }
  void reqFundamentalData(
      TickerId reqId, const Contract& contract, const IBString& reportType) {
    LOG_START
        << __f__(reqId)
        << __f__(&contract)
        << __f__(reportType);
    EPosixClientSocket::reqFundamentalData(reqId, contract, reportType);
    LOG_END;
 }
  void cancelFundamentalData(TickerId reqId) {
    LOG_START
        << __f__(reqId);
    EPosixClientSocket::cancelFundamentalData(reqId);
    LOG_END;
 }
  void calculateImpliedVolatility(
      TickerId reqId, const Contract &contract,
      double optionPrice, double underPrice) {
    LOG_START
        << __f__(reqId)
        << __f__(&contract)
        << __f__(optionPrice)
        << __f__(underPrice);
    EPosixClientSocket::calculateImpliedVolatility(reqId, contract,
                                                   optionPrice, underPrice);
    LOG_END;
  }
};
}
}
#endif // IB_UTIL_LOG_CLIENT_H_
