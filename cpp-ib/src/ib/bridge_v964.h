
#ifndef IB_USE_STD_STRING
#define IB_USE_STD_STRING
#endif

#ifndef V964_BRIDGE_H_
#define V964_BRIDGE_H_

#include "api/9.64beta/Shared/Contract.h"
#include "api/9.64beta/Shared/Order.h"
#include "api/9.64beta/Shared/EWrapper.h"
#include "api/9.64beta/PosixSocketClient/EPosixClientSocket.h"

#include <memory>

class EPosixClientSocket;

enum State {
  ST_CONNECT,
  ST_PLACEORDER,
  ST_PLACEORDER_ACK,
  ST_CANCELORDER,
  ST_CANCELORDER_ACK,
  ST_PING,
  ST_PING_ACK,
  ST_IDLE
};


class V964Bridge : public EWrapper
{
public:

  V964Bridge();
  ~V964Bridge();

  void processMessages();

public:

  bool connect(const char * host, unsigned int port, int clientId = 0);
  void disconnect() const;
  bool isConnected() const;

private:

  void reqCurrentTime();
  void placeOrder();
  void cancelOrder();

public:
  // events
  void tickPrice(TickerId tickerId, TickType field, double price, int canAutoExecute);
  void tickSize(TickerId tickerId, TickType field, int size);
  void tickOptionComputation(TickerId tickerId, TickType tickType,
                             double impliedVol,
                             double delta, double optPrice, double pvDividend,
                             double gamma, double vega, double theta, double undPrice);
  void tickGeneric(TickerId tickerId, TickType tickType, double value);
  void tickString(TickerId tickerId, TickType tickType, const IBString& value);
  void tickEFP(TickerId tickerId, TickType tickType, double basisPoints, const IBString& formattedBasisPoints,
               double totalDividends, int holdDays, const IBString& futureExpiry, double dividendImpact, double dividendsToExpiry);
  void orderStatus(OrderId orderId, const IBString &status, int filled,
                   int remaining, double avgFillPrice, int permId, int parentId,
                   double lastFillPrice, int clientId, const IBString& whyHeld);
  void openOrder(OrderId orderId, const Contract&, const Order&, const OrderState&);
  void openOrderEnd();
  void winError(const IBString &str, int lastError);
  void connectionClosed();
  void updateAccountValue(const IBString& key, const IBString& val,
                          const IBString& currency, const IBString& accountName);
  void updatePortfolio(const Contract& contract, int position,
                       double marketPrice, double marketValue, double averageCost,
                       double unrealizedPNL, double realizedPNL, const IBString& accountName);
  void updateAccountTime(const IBString& timeStamp);
  void accountDownloadEnd(const IBString& accountName);
  void nextValidId(OrderId orderId);
  void contractDetails(int reqId, const ContractDetails& contractDetails);
  void bondContractDetails(int reqId, const ContractDetails& contractDetails);
  void contractDetailsEnd(int reqId);
  void execDetails(int reqId, const Contract& contract, const Execution& execution);
  void execDetailsEnd(int reqId);
  void error(const int id, const int errorCode, const IBString errorString);
  void updateMktDepth(TickerId id, int position, int operation, int side,
                      double price, int size);
  void updateMktDepthL2(TickerId id, int position, IBString marketMaker, int operation,
                        int side, double price, int size);
  void updateNewsBulletin(int msgId, int msgType, const IBString& newsMessage, const IBString& originExch);
  void managedAccounts(const IBString& accountsList);
  void receiveFA(faDataType pFaDataType, const IBString& cxml);
  void historicalData(TickerId reqId, const IBString& date, double open, double high,
                      double low, double close, int volume, int barCount, double WAP, int hasGaps);
  void scannerParameters(const IBString &xml);
  void scannerData(int reqId, int rank, const ContractDetails &contractDetails,
                   const IBString &distance, const IBString &benchmark, const IBString &projection,
                   const IBString &legsStr);
  void scannerDataEnd(int reqId);
  void realtimeBar(TickerId reqId, long time, double open, double high, double low, double close,
                   long volume, double wap, int count);
  void currentTime(long time);
  void fundamentalData(TickerId reqId, const IBString& data);
  void deltaNeutralValidation(int reqId, const UnderComp& underComp);
  void tickSnapshotEnd(int reqId);

private:

  std::auto_ptr<EPosixClientSocket> m_pClient;
  State m_state;
  time_t m_sleepDeadline;

  OrderId m_orderId;
};

#endif // V964_BRIDGE_H_
