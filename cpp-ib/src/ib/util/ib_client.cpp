
#include "ib/util/internal.hpp"

#include <glog/logging.h>

#include <arpa/inet.h>
#include <errno.h>
#include <sys/select.h>

// helpers
inline bool SocketsInit() { return true; };
inline bool SocketsDestroy() { return true; };
inline int SocketClose(int sockfd) { return close(sockfd); };

const int PING_DEADLINE = 2; // seconds
const int SLEEP_BETWEEN_PINGS = 60 * 60 * 10; // seconds

const int LEVEL = 2;

using namespace ib::util;

///////////////////////////////////////////////////////////
// member funcs
IbClient::IbClient(int id) :
    LogWrapper::LogWrapper(id)
    , connection_id(id)
    , m_pClient(new LogClientSocket(id, this))
    , m_state(ST_CONNECT)
    , m_sleepDeadline(0)
    , m_orderId(0)
{

}

IbClient::~IbClient()
{
}

bool IbClient::connect(const char *host, unsigned int port, int clientId)
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

void IbClient::disconnect() const
{
  m_pClient->eDisconnect();

  printf ("Disconnected\n");
}

bool IbClient::isConnected() const
{
  return m_pClient->isConnected();
}

void IbClient::processMessages()
{
  fd_set readSet, writeSet, errorSet;

  struct timeval tval;
  tval.tv_usec = 0;
  tval.tv_sec = 0;

  time_t now = time(NULL);

  switch (m_state) {
    case ST_MARKETDATA:
      requestMarketData();
      break;
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
void IbClient::reqCurrentTime()
{
  printf("Requesting Current Time\n");

  // set ping deadline to "now + n seconds"
  m_sleepDeadline = time(NULL) + PING_DEADLINE;

  m_state = ST_PING_ACK;

  m_pClient->reqCurrentTime();
}

Contract CreateContractForStock(std::string symbol)
{
  VLOG(LEVEL) << "Creating contract for " << symbol;

  Contract contract;
  contract.symbol = symbol;
  contract.secType = "STK";
  contract.exchange = "SMART";
  contract.currency = "USD";
  return contract;
}

void IbClient::addSymbol(std::string symbol) {
  VLOG(LEVEL) << "Adding symbol to watch: " << symbol;
  symbols_.push_back(symbol);
}

void IbClient::requestMarketData() {
  list<string>::iterator iter;
  int i = 0;
  for (iter = symbols_.begin(); iter != symbols_.end();  iter++, i++) {
    Contract c = CreateContractForStock(*iter);
    TickerId id = i;
    m_pClient->reqMktData(id, c, "", false);
  }
  m_state = ST_PING;
}

void IbClient::placeOrder()
{
  VLOG(LEVEL)  << "Placing order.";
  Contract contract = CreateContractForStock("MSFT");
  Order order;

  order.action = "BUY";
  order.totalQuantity = 1000;
  order.orderType = "LMT";
  order.lmtPrice = 0.01;

  VLOG(LEVEL)
      << "Placing Order " << m_orderId << " " << order.action
      << " " << order.totalQuantity << " " << contract.symbol
      << " @ " << order.lmtPrice;

  m_state = ST_PLACEORDER_ACK;

  m_pClient->placeOrder(m_orderId, contract, order);
  VLOG(LEVEL) << "Order placed.";
}

void IbClient::cancelOrder()
{
  VLOG(LEVEL) << "Cancelling Order " << m_orderId;

  m_state = ST_CANCELORDER_ACK;

  m_pClient->cancelOrder(m_orderId);
  VLOG(LEVEL) << "State = " << m_state;
}


///////////////////////////////////////////////////////////////////
// events
void IbClient::orderStatus(OrderId orderId, const IBString &status, int filled,
                           int remaining, double avgFillPrice,
                           int permId, int parentId,
                           double lastFillPrice, int clientId,
                           const IBString& whyHeld)

{
  LogWrapper::orderStatus(orderId, status, filled, remaining, avgFillPrice,
                          permId, parentId, lastFillPrice, clientId, whyHeld);

  if(orderId == m_orderId) {
    if(m_state == ST_PLACEORDER_ACK &&
       (status == "PreSubmitted" || status == "Submitted"))
      m_state = ST_CANCELORDER;

    if(m_state == ST_CANCELORDER_ACK && status == "Cancelled")
      m_state = ST_PING;

    printf("Order: id=%ld, status=%s\n", orderId, status.c_str());
  }
}

void IbClient::nextValidId(OrderId orderId)
{
  LogWrapper::nextValidId(orderId);
  m_orderId = orderId;
  m_state = ST_MARKETDATA; // PLACEORDER;
  VLOG(LEVEL) << "Transition to state " << m_state;
}

void IbClient::currentTime(long time)
{
  LogWrapper::currentTime(time);
  if (m_state == ST_PING_ACK) {
    time_t t = (time_t)time;
    struct tm * timeinfo = localtime (&t);
    printf("The current date/time is: %s", asctime(timeinfo));

    time_t now = ::time(NULL);
    m_sleepDeadline = now + SLEEP_BETWEEN_PINGS;

    m_state = ST_IDLE;
  }
}

void IbClient::error(const int id, const int errorCode,
                     const IBString errorString)
{
  LogWrapper::error(id, errorCode, errorString);

  // Connectivity between IB and TWS has been lost"
  if(id == -1 && errorCode == 1100) {
    disconnect();
  }
}
