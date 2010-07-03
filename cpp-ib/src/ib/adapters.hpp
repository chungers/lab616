#ifndef IB_ADAPTERS_H_
#define IB_ADAPTERS_H_

// Header file for EWrapper and EClient derived classes for IB API Version 9.64
//
// This file contains the logging adapters for the EWrapper and EClient classes
// in the IB API.  This head file contains all the IB-specific dependencies and
// requires update whenever IB changes their API.
//
// By default, all methods are logged, both for the EClient (outbound message)
// and the EWrapper (inbound message).  These classes are intended to be
// subclassed so that methods are overridden as necessary.  Those methods
// not handled will then output log entries so that all events and actions
// via the IB API are accounted for.

#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

#include <sys/time.h>
#include <boost/thread.hpp>

#include <Shared/Contract.h>
#include <Shared/EClient.h>
#include <Shared/EWrapper.h>
#include <Shared/Order.h>
#include <PosixSocketClient/EPosixClientSocket.h>

using namespace std;

namespace ib {
namespace adapter {

// From EWrapper.h
const std::string kTickTypes[] = {
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



class LoggingEWrapper : public EWrapper {

 public:

  LoggingEWrapper(const string host,
                  const unsigned int port,
                  const unsigned int connection_id);
  ~LoggingEWrapper();

 protected:
  void set_connection_id(const unsigned int id);

 private:
  const string host_;
  const unsigned int port_;
  unsigned int connection_id_;

 public:

  template <typename State_t> const State_t get_current_state();
  template <typename State_t> const State_t get_previous_state();

  const string get_host();
  const unsigned int get_port();
  const unsigned int get_connection_id();

 public:

  // The methods below are copied directly from EWrapper.h

  void tickPrice(TickerId tickerId, TickType field, double price,
                 int canAutoExecute);
  void tickSize(TickerId tickerId, TickType field, int size);
  void tickOptionComputation(TickerId tickerId, TickType tickType,
                             double impliedVol,
                             double delta, double optPrice, double pvDividend,
                             double gamma, double vega,
                             double theta, double undPrice);
  void tickGeneric(TickerId tickerId, TickType tickType, double value);
  void tickString(TickerId tickerId,
                  TickType tickType, const IBString& value);
  void tickEFP(TickerId tickerId, TickType tickType,
               double basisPoints, const IBString& formattedBasisPoints,
               double totalDividends, int holdDays,
               const IBString& futureExpiry, double dividendImpact,
               double dividendsToExpiry);
  void orderStatus(OrderId orderId, const IBString &status, int filled,
                   int remaining, double avgFillPrice, int permId, int parentId,
                   double lastFillPrice, int clientId, const IBString& whyHeld);
  void openOrder(OrderId orderId, const Contract&, const Order&,
                 const OrderState&);
  void openOrderEnd();
  void winError(const IBString &str, int lastError);
  void connectionClosed();
  void updateAccountValue(const IBString& key, const IBString& val,
                          const IBString& currency,
                          const IBString& accountName);
  void updatePortfolio(const Contract& contract, int position,
                       double marketPrice, double marketValue,
                       double averageCost,
                       double unrealizedPNL, double realizedPNL,
                       const IBString& accountName);
  void updateAccountTime(const IBString& timeStamp);
  void accountDownloadEnd(const IBString& accountName);
  void nextValidId(OrderId orderId);
  void contractDetails(int reqId, const ContractDetails& contractDetails);
  void bondContractDetails(int reqId,
                           const ContractDetails& contractDetails);
  void contractDetailsEnd(int reqId);
  void execDetails(int reqId, const Contract& contract,
                   const Execution& execution);
  void execDetailsEnd(int reqId);
  void error(const int id, const int errorCode, const IBString errorString);
  void updateMktDepth(TickerId id, int position, int operation, int side,
                      double price, int size);
  void updateMktDepthL2(TickerId id, int position, IBString marketMaker,
                        int operation,
                        int side, double price, int size);
  void updateNewsBulletin(int msgId, int msgType,
                          const IBString& newsMessage,
                          const IBString& originExch);
  void managedAccounts(const IBString& accountsList);
  void receiveFA(faDataType pFaDataType, const IBString& cxml);
  void historicalData(TickerId reqId, const IBString& date, double open,
                      double high, double low, double close, int volume,
                      int barCount, double WAP, int hasGaps);
  void scannerParameters(const IBString &xml);
  void scannerData(int reqId, int rank,
                   const ContractDetails &contractDetails,
                   const IBString &distance, const IBString &benchmark,
                   const IBString &projection,
                   const IBString &legsStr);
  void scannerDataEnd(int reqId);
  void realtimeBar(TickerId reqId, long time, double open, double high,
                   double low, double close,
                   long volume, double wap, int count);
  void currentTime(long time);
  void fundamentalData(TickerId reqId, const IBString& data);
  void deltaNeutralValidation(int reqId, const UnderComp& underComp);
  void tickSnapshotEnd(int reqId);
};


class LoggingEClientSocket : public EPosixClientSocket {
 public:

  LoggingEClientSocket(unsigned int connection_id, EWrapper* e_wrapper);
  ~LoggingEClientSocket();

 private:

  boost::mutex socket_write_mutex_;  // For outbound messages only.
  const unsigned int connection_id_;
  uint64_t call_start_;

 public:

  const unsigned int get_connection_id();

  // Methods from EPosixSocketClient

  bool eConnect(const char *host, unsigned int port, int clientId=0);
  void eDisconnect();
  int serverVersion();
  IBString TwsConnectionTime();
  void reqMktData(TickerId id, const Contract &contract,
                  const IBString& genericTicks, bool snapshot);
  void cancelMktData(TickerId id);
  void placeOrder(OrderId id, const Contract &contract, const Order &order);
  void cancelOrder(OrderId id);
  void reqOpenOrders();
  void reqAccountUpdates(bool subscribe, const IBString& acctCode);
  void reqExecutions(int reqId, const ExecutionFilter& filter);
  void reqIds(int numIds);
  void reqContractDetails(int reqId, const Contract &contract);
  void reqMktDepth(TickerId id, const Contract &contract, int numRows);
  void cancelMktDepth(TickerId id);
  void reqNewsBulletins(bool allMsgs);
  void cancelNewsBulletins();
  void setServerLogLevel(int level);
  void reqAutoOpenOrders(bool bAutoBind);
  void reqAllOpenOrders();
  void reqManagedAccts();
  void requestFA(faDataType pFaDataType);
  void replaceFA(faDataType pFaDataType, const IBString& cxml);
  void reqHistoricalData(TickerId id, const Contract &contract,
                         const IBString &endDateTime,
                         const IBString &durationStr,
                         const IBString &barSizeSetting,
                         const IBString &whatToShow,
                         int useRTH, int formatDate);
  void exerciseOptions(TickerId id, const Contract &contract,
                       int exerciseAction, int exerciseQuantity,
                       const IBString &account, int override);
  void cancelHistoricalData(TickerId tickerId );
  void reqRealTimeBars(TickerId id, const Contract &contract, int barSize,
                       const IBString &whatToShow, bool useRTH);
  void cancelRealTimeBars(TickerId tickerId);
  void cancelScannerSubscription(int tickerId);
  void reqScannerParameters();
  void reqScannerSubscription(int tickerId,
                              const ScannerSubscription &subscription);
  void reqCurrentTime();
  void reqFundamentalData(TickerId reqId, const Contract& contract,
                          const IBString& reportType);
  void cancelFundamentalData(TickerId reqId);
  void calculateImpliedVolatility(TickerId reqId, const Contract &contract,
                                  double optionPrice, double underPrice);

};
} // namespace adapter
}  // namespace ib
#endif  // IB_ADAPTERS_H_
