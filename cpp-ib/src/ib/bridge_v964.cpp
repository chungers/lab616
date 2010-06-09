// bridge.cpp
// IB Bridge
#include "bridge_v964.h"

#include <arpa/inet.h>
#include <errno.h>
#include <sys/select.h>

// helpers
inline bool SocketsInit() { return true; };
inline bool SocketsDestroy() { return true; };
inline int SocketClose(int sockfd) { return close(sockfd); };

const int PING_DEADLINE = 2; // seconds
const int SLEEP_BETWEEN_PINGS = 30; // seconds

///////////////////////////////////////////////////////////
// member funcs
V964Bridge::V964Bridge()
    : m_pClient(new EPosixClientSocket(this))
    , m_state(ST_CONNECT)
    , m_sleepDeadline(0)
    , m_orderId(0)
{
}

V964Bridge::~V964Bridge()
{
}

bool V964Bridge::connect(const char *host, unsigned int port, int clientId)
{
  // trying to connect
  printf("Connecting to %s:%d clientId:%d\n",
         !(host && *host) ? "127.0.0.1" : host,
         port,
         clientId);
  
  bool bRes = m_pClient->eConnect(host, port, clientId);
  
  if (bRes) {
    printf("Connected to %s:%d clientId:%d\n", !(host && *host) ? "127.0.0.1" : host, port, clientId);
  }
  else
    printf("Cannot connect to %s:%d clientId:%d\n", !(host && *host) ? "127.0.0.1" : host, port, clientId);

  return bRes;
}

void V964Bridge::disconnect() const
{
  m_pClient->eDisconnect();

  printf ("Disconnected\n");
}

bool V964Bridge::isConnected() const
{
  return m_pClient->isConnected();
}

void V964Bridge::processMessages()
{
  fd_set readSet, writeSet, errorSet;

  struct timeval tval;
  tval.tv_usec = 0;
  tval.tv_sec = 0;

  time_t now = time(NULL);

  switch (m_state) {
    case ST_PLACEORDER:
      placeOrder();
      break;
    case ST_PLACEORDER_ACK:
      break;
    case ST_CANCELORDER:
      cancelOrder();
      break;
    case ST_CANCELORDER_ACK:
      break;
    case ST_PING:
      reqCurrentTime();
      break;
    case ST_PING_ACK:
      if(m_sleepDeadline < now) {
        disconnect();
        return;
      }
      break;
    case ST_IDLE:
      if(m_sleepDeadline < now) {
        m_state = ST_PING;
        return;
      }
      break;
  }

  if(m_sleepDeadline > 0) {
    // initialize timeout with m_sleepDeadline - now
    tval.tv_sec = m_sleepDeadline - now;
  }

  if(m_pClient->fd() >= 0 ) {

    FD_ZERO(&readSet);
    errorSet = writeSet = readSet;

    FD_SET(m_pClient->fd(), &readSet);

    if(!m_pClient->isOutBufferEmpty())
      FD_SET(m_pClient->fd(), &writeSet);

    FD_CLR(m_pClient->fd(), &errorSet);

    int ret = select(m_pClient->fd() + 1, &readSet, &writeSet, &errorSet, &tval);

    if(ret == 0) { // timeout
      return;
    }

    if(ret < 0) {	// error
      disconnect();
      return;
    }

    if(m_pClient->fd() < 0)
      return;

    if(FD_ISSET(m_pClient->fd(), &errorSet)) {
      // error on socket
      m_pClient->onError();
    }

    if(m_pClient->fd() < 0)
      return;

    if(FD_ISSET(m_pClient->fd(), &writeSet)) {
      // socket is ready for writing
      m_pClient->onSend();
    }

    if(m_pClient->fd() < 0)
      return;

    if(FD_ISSET(m_pClient->fd(), &readSet)) {
      // socket is ready for reading
      m_pClient->onReceive();
    }
  }
}

//////////////////////////////////////////////////////////////////
// methods
void V964Bridge::reqCurrentTime()
{
  printf("Requesting Current Time\n");

  // set ping deadline to "now + n seconds"
  m_sleepDeadline = time(NULL) + PING_DEADLINE;

  m_state = ST_PING_ACK;

  m_pClient->reqCurrentTime();
}

void V964Bridge::placeOrder()
{
  Contract contract;
  Order order;

  contract.symbol = "MSFT";
  contract.secType = "STK";
  contract.exchange = "SMART";
  contract.currency = "USD";

  order.action = "BUY";
  order.totalQuantity = 1000;
  order.orderType = "LMT";
  order.lmtPrice = 0.01;

  printf("Placing Order %ld: %s %ld %s at %f\n", m_orderId, order.action.c_str(), order.totalQuantity, contract.symbol.c_str(), order.lmtPrice);

  m_state = ST_PLACEORDER_ACK;

  m_pClient->placeOrder(m_orderId, contract, order);
}

void V964Bridge::cancelOrder()
{
  printf("Cancelling Order %ld\n", m_orderId);

  m_state = ST_CANCELORDER_ACK;

  m_pClient->cancelOrder(m_orderId);
}

///////////////////////////////////////////////////////////////////
// events
void V964Bridge::orderStatus(OrderId orderId, const IBString &status, int filled,
                         int remaining, double avgFillPrice, int permId, int parentId,
                         double lastFillPrice, int clientId, const IBString& whyHeld)

{
  if(orderId == m_orderId) {
    if(m_state == ST_PLACEORDER_ACK && (status == "PreSubmitted" || status == "Submitted"))
      m_state = ST_CANCELORDER;

    if(m_state == ST_CANCELORDER_ACK && status == "Cancelled")
      m_state = ST_PING;

    printf("Order: id=%ld, status=%s\n", orderId, status.c_str());
  }
}

void V964Bridge::nextValidId(OrderId orderId)
{
  m_orderId = orderId;

  m_state = ST_PLACEORDER;
}

void V964Bridge::currentTime(long time)
{
  if (m_state == ST_PING_ACK) {
    time_t t = (time_t)time;
    struct tm * timeinfo = localtime (&t);
    printf("The current date/time is: %s", asctime(timeinfo));

    time_t now = ::time(NULL);
    m_sleepDeadline = now + SLEEP_BETWEEN_PINGS;

    m_state = ST_IDLE;
  }
}

void V964Bridge::error(const int id, const int errorCode, const IBString errorString)
{
  //	printf("Error id=%d, errorCode=%d, msg=%s\n", id, errorCode, errorString.c_str());

  if(id == -1 && errorCode == 1100) // if "Connectivity between IB and TWS has been lost"
    disconnect();
}

void V964Bridge::tickPrice(TickerId tickerId, TickType field, double price, int canAutoExecute) {}
void V964Bridge::tickSize(TickerId tickerId, TickType field, int size) {}

void V964Bridge::tickOptionComputation(
    TickerId tickerId, TickType tickType,
    double impliedVol,
    double delta, double optPrice, double pvDividend,
    double gamma, double vega, double theta, double undPrice) {}

void V964Bridge::tickGeneric(TickerId tickerId, TickType tickType, double value) {}
void V964Bridge::tickString(TickerId tickerId, TickType tickType, const IBString& value) {}
void V964Bridge::tickEFP(TickerId tickerId, TickType tickType, double basisPoints, const IBString& formattedBasisPoints,
                     double totalDividends, int holdDays, const IBString& futureExpiry, double dividendImpact, double dividendsToExpiry) {}
void V964Bridge::openOrder(OrderId orderId, const Contract&, const Order&, const OrderState& ostate) {}
void V964Bridge::openOrderEnd() {}
void V964Bridge::winError(const IBString &str, int lastError) {}
void V964Bridge::connectionClosed() {}
void V964Bridge::updateAccountValue(const IBString& key, const IBString& val,
                                const IBString& currency, const IBString& accountName) {}
void V964Bridge::updatePortfolio(const Contract& contract, int position,
                             double marketPrice, double marketValue, double averageCost,
                             double unrealizedPNL, double realizedPNL, const IBString& accountName){}
void V964Bridge::updateAccountTime(const IBString& timeStamp) {}
void V964Bridge::accountDownloadEnd(const IBString& accountName) {}
void V964Bridge::contractDetails(int reqId, const ContractDetails& contractDetails) {}
void V964Bridge::bondContractDetails(int reqId, const ContractDetails& contractDetails) {}
void V964Bridge::contractDetailsEnd(int reqId) {}
void V964Bridge::execDetails(int reqId, const Contract& contract, const Execution& execution) {}
void V964Bridge::execDetailsEnd(int reqId) {}

void V964Bridge::updateMktDepth(TickerId id, int position, int operation, int side,
                            double price, int size) {}
void V964Bridge::updateMktDepthL2(TickerId id, int position, IBString marketMaker, int operation,
                              int side, double price, int size) {}
void V964Bridge::updateNewsBulletin(int msgId, int msgType, const IBString& newsMessage, const IBString& originExch) {}
void V964Bridge::managedAccounts(const IBString& accountsList) {}
void V964Bridge::receiveFA(faDataType pFaDataType, const IBString& cxml) {}
void V964Bridge::historicalData(TickerId reqId, const IBString& date, double open, double high,
                            double low, double close, int volume, int barCount, double WAP, int hasGaps) {}
void V964Bridge::scannerParameters(const IBString &xml) {}
void V964Bridge::scannerData(int reqId, int rank, const ContractDetails &contractDetails,
                         const IBString &distance, const IBString &benchmark, const IBString &projection,
                         const IBString &legsStr) {}
void V964Bridge::scannerDataEnd(int reqId) {}
void V964Bridge::realtimeBar(TickerId reqId, long time, double open, double high, double low, double close,
                         long volume, double wap, int count) {}
void V964Bridge::fundamentalData(TickerId reqId, const IBString& data) {}
void V964Bridge::deltaNeutralValidation(int reqId, const UnderComp& underComp) {}
void V964Bridge::tickSnapshotEnd(int reqId) {}

