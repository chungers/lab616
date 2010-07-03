
#include <ib/adapters.hpp>
#include <glog/logging.h>
#include <sys/time.h>

// Verbose level.  Use flag --v=N where N >= VLOG_LEVEL_* to see.
#define VLOG_LEVEL_ECLIENT  2
#define VLOG_LEVEL_EWRAPPER 1

typedef uint64_t int64;
inline int64 now_micros() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return static_cast<int64>(tv.tv_sec) * 1000000 + tv.tv_usec;
}

#define __f__(m) "," << #m << '=' << m

#define LOG_EVENT				\
  VLOG(VLOG_LEVEL_EWRAPPER)			\
  << "cid=" << connection_id_			\
  << ",ts=" << now_micros()			\
  << ",event=" << __func__

#define __tick_type_enum(m) ",field=" << kTickTypes[m]

namespace ib {
namespace adapter {

LoggingEWrapper::LoggingEWrapper(const string host,
                                 const unsigned int port,
                                 const unsigned int connection_id)
    : host_(host)
    , port_(port)
    , connection_id_(connection_id)
{
}

LoggingEWrapper::~LoggingEWrapper() {
}

void LoggingEWrapper::set_connection_id(const unsigned int id)
{
  connection_id_ = id;
  VLOG(VLOG_LEVEL_EWRAPPER) << "Connection id updated to " << id;
}

const string LoggingEWrapper::get_host()
{
  return host_;
}

const unsigned int LoggingEWrapper::get_port()
{
  return port_;
}

const unsigned int LoggingEWrapper::get_connection_id()
{
  return connection_id_;
}

////////////////////////////////////////////////////////////////////////////////
// EWrapper Methods
//
void LoggingEWrapper::tickPrice(TickerId tickerId, TickType field,
                                double price, int canAutoExecute) {
  LOG_EVENT
      << __f__(tickerId)
      << __tick_type_enum(field)
      << __f__(price)
      << __f__(canAutoExecute);
}
void LoggingEWrapper::tickSize(TickerId tickerId, TickType field, int size) {
  LOG_EVENT
      << __f__(tickerId)
      << __tick_type_enum(field)
      << __f__(size);
}
void LoggingEWrapper::tickOptionComputation(
    TickerId tickerId, TickType tickType,
    double impliedVol,
    double delta, double optPrice, double pvDividend,
    double gamma, double vega,
    double theta, double undPrice) {
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
void LoggingEWrapper::tickGeneric(
    TickerId tickerId, TickType tickType, double value) {
  LOG_EVENT
      << __f__(tickerId)
      << __tick_type_enum(tickType)
      << __f__(value);
}
void LoggingEWrapper::tickString(TickerId tickerId, TickType tickType,
                                 const IBString& value) {
  LOG_EVENT
      << __f__(tickerId)
      << __tick_type_enum(tickType)
      << __f__(value);
}
void LoggingEWrapper::tickEFP(TickerId tickerId, TickType tickType,
                              double basisPoints,
                              const IBString& formattedBasisPoints,
                              double totalDividends, int holdDays,
                              const IBString& futureExpiry,
                              double dividendImpact,
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
void LoggingEWrapper::openOrder(OrderId orderId, const Contract& contract,
                                const Order& order, const OrderState& state) {
  LOG_EVENT
      << __f__(orderId)
      << __f__(&contract)
      << __f__(&order)
      << __f__(&state);
}
void LoggingEWrapper::openOrderEnd() {
  LOG_EVENT;
}
void LoggingEWrapper::winError(const IBString &str, int lastError) {
  LOG_EVENT
      << __f__(str)
      << __f__(lastError);
}
void LoggingEWrapper::connectionClosed() {
  LOG_EVENT;
}
void LoggingEWrapper::updateAccountValue(const IBString& key,
                                         const IBString& val,
                                         const IBString& currency,
                                         const IBString& accountName) {
  LOG_EVENT
      << __f__(key)
      << __f__(val)
      << __f__(currency)
      << __f__(accountName);
}
void LoggingEWrapper::updatePortfolio(const Contract& contract, int position,
                                      double marketPrice, double marketValue,
                                      double averageCost,
                                      double unrealizedPNL, double realizedPNL,
                                      const IBString& accountName) {
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
void LoggingEWrapper::updateAccountTime(const IBString& timeStamp) {
  LOG_EVENT
      << __f__(timeStamp);
}
void LoggingEWrapper::accountDownloadEnd(const IBString& accountName) {
  LOG_EVENT
      << __f__(accountName);
}
void LoggingEWrapper::contractDetails(int reqId,
                                      const ContractDetails& contractDetails) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(&contractDetails);
}
void LoggingEWrapper::bondContractDetails(
    int reqId, const ContractDetails& contractDetails) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(&contractDetails);
}
void LoggingEWrapper::contractDetailsEnd(int reqId) {
  LOG_EVENT
      << __f__(reqId);
}
void LoggingEWrapper::execDetails(int reqId, const Contract& contract,
                                  const Execution& execution) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(&contract)
      << __f__(&execution);
}
void LoggingEWrapper::execDetailsEnd(int reqId) {
  LOG_EVENT
      << __f__(reqId);
}
void LoggingEWrapper::updateMktDepth(TickerId id, int position,
                                     int operation, int side,
                                     double price, int size) {
    LOG_EVENT
        << __f__(id)
        << __f__(position)
        << __f__(operation)
        << __f__(side)
        << __f__(price)
        << __f__(size);
  }
void LoggingEWrapper::updateMktDepthL2(TickerId id, int position,
                                       IBString marketMaker, int operation,
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
void LoggingEWrapper::updateNewsBulletin(int msgId, int msgType,
                                         const IBString& newsMessage,
                                         const IBString& originExch) {
  LOG_EVENT
      << __f__(msgId)
      << __f__(msgType)
      << __f__(newsMessage)
      << __f__(originExch);
}
void LoggingEWrapper::managedAccounts(const IBString& accountsList) {
  LOG_EVENT
      << __f__(accountsList);
}
void LoggingEWrapper::receiveFA(faDataType pFaDataType, const IBString& cxml) {
  LOG_EVENT
      << __f__(pFaDataType)
      << __f__(cxml);
}
void LoggingEWrapper::historicalData(TickerId reqId, const IBString& date,
                                     double open, double high,
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
void LoggingEWrapper::scannerParameters(const IBString &xml) {
  LOG_EVENT
      << __f__(xml);
}
void LoggingEWrapper::scannerData(int reqId, int rank,
                                  const ContractDetails &contractDetails,
                                  const IBString &distance,
                                  const IBString &benchmark,
                                  const IBString &projection,
                                  const IBString &legsStr) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(rank)
      << __f__(&contractDetails)
      << __f__(distance)
      << __f__(benchmark)
      << __f__(projection)
      << __f__(legsStr);
}
void LoggingEWrapper::scannerDataEnd(int reqId) {
  LOG_EVENT
      << __f__(reqId);
}
void LoggingEWrapper::realtimeBar(TickerId reqId, long time,
                                  double open, double high,
                                  double low, double close,
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
void LoggingEWrapper::fundamentalData(TickerId reqId, const IBString& data) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(data);
}
void LoggingEWrapper::deltaNeutralValidation(int reqId,
                                             const UnderComp& underComp) {
  LOG_EVENT
      << __f__(reqId)
      << __f__(&underComp);
}
void LoggingEWrapper::tickSnapshotEnd(int reqId) {
  LOG_EVENT
      << __f__(reqId);
}
void LoggingEWrapper::orderStatus(OrderId orderId, const IBString &status,
                                  int filled,  int remaining,
                                  double avgFillPrice,
                                  int permId, int parentId,
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
void LoggingEWrapper::nextValidId(OrderId orderId) {
  LOG_EVENT
      << __f__(orderId);
}
void LoggingEWrapper::currentTime(long time) {
  LOG_EVENT
      << __f__(time);
}
void LoggingEWrapper::error(const int id, const int errorCode,
                            const IBString errorString) {
  LOG_EVENT
      << __f__(id)
      << __f__(errorCode)
      << __f__(errorString);
}

#define LOG_START                               \
  VLOG(VLOG_LEVEL_ECLIENT - 1)                  \
  << "cid=" << connection_id_                   \
  << ",ts=" << (call_start_ = now_micros())     \
  << ",action=" << __func__

#define LOG_END                                         \
  VLOG(VLOG_LEVEL_ECLIENT)                              \
  << "cid=" << connection_id_                           \
  << ",ts=" << (call_start_ = now_micros())             \
  << ",action=" << __func__                             \
  << ",elapsed=" << (now_micros() - call_start_)


typedef boost::unique_lock<boost::mutex> write_lock;

LoggingEClientSocket::LoggingEClientSocket(
    unsigned int connection_id,
    EWrapper* e_wrapper)
    : EPosixClientSocket::EPosixClientSocket(e_wrapper)
    , connection_id_(connection_id) {
}

LoggingEClientSocket::~LoggingEClientSocket() {
}

const unsigned int LoggingEClientSocket::get_connection_id()
{
  return connection_id_;
}

bool LoggingEClientSocket::eConnect(const char *host,
                                    unsigned int port, int clientId) {
  LOG_START <<
      __f__(host) <<
      __f__(port) <<
      __f__(clientId);
  write_lock lock(socket_write_mutex_);
  bool v = EPosixClientSocket::eConnect(host, port, clientId);
  LOG_END;
  return v;
}
void LoggingEClientSocket::eDisconnect() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::eDisconnect();
  LOG_END;
}
int LoggingEClientSocket::serverVersion() {
  LOG_START;
  int v = EPosixClientSocket::serverVersion();
  LOG_END;
  return v;
}
IBString LoggingEClientSocket::TwsConnectionTime() {
  LOG_START;
  IBString v = EPosixClientSocket::TwsConnectionTime();
  LOG_END;
  return v;
}
void LoggingEClientSocket::reqMktData(TickerId id, const Contract &contract,
                                      const IBString& genericTicks,
                                      bool snapshot) {
  LOG_START
      << __f__(id)
      << __f__(&contract)
      << __f__(genericTicks)
      << __f__(snapshot);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqMktData(id, contract, genericTicks, snapshot);
  LOG_END;
}
void LoggingEClientSocket::cancelMktData(TickerId id) {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelMktData(id);
  LOG_END;
}
void LoggingEClientSocket::placeOrder(OrderId id, const Contract &contract,
                                      const Order &order) {
  LOG_START
      << __f__(id)
      << __f__(&contract)
      << __f__(&order);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::placeOrder(id, contract, order);
  LOG_END;
}
void LoggingEClientSocket::cancelOrder(OrderId id) {
  LOG_START
      << __f__(id);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelOrder(id);
  LOG_END;
}
void LoggingEClientSocket::reqOpenOrders() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqOpenOrders();
  LOG_END;
}
void LoggingEClientSocket::reqAccountUpdates(bool subscribe,
                                             const IBString& acctCode) {
  LOG_START
      << __f__(subscribe)
      << __f__(acctCode);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqAccountUpdates(subscribe, acctCode);
  LOG_END;
}
void LoggingEClientSocket::reqExecutions(int reqId,
                                         const ExecutionFilter& filter) {
  LOG_START
      << __f__(reqId)
      << __f__(&filter);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqExecutions(reqId, filter);
  LOG_END;
}
void LoggingEClientSocket::reqIds(int numIds) {
  LOG_START
      << __f__(numIds);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqIds(numIds);
  LOG_END;
}
void LoggingEClientSocket::reqContractDetails(int reqId,
                                              const Contract &contract) {
  LOG_START
      << __f__(reqId)
      << __f__(&contract);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqContractDetails(reqId, contract);
  LOG_END;
}
void LoggingEClientSocket::reqMktDepth(TickerId id,
                                       const Contract &contract, int numRows) {
  LOG_START
      << __f__(id)
      << __f__(&contract)
      << __f__(numRows);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqMktDepth(id, contract, numRows);
  LOG_END;
}
void LoggingEClientSocket::cancelMktDepth(TickerId id) {
  LOG_START
      << __f__(id);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelMktDepth(id);
  LOG_END;
}
void LoggingEClientSocket::reqNewsBulletins(bool allMsgs) {
  LOG_START
      << __f__(allMsgs);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqNewsBulletins(allMsgs);
  LOG_END;
}
void LoggingEClientSocket::cancelNewsBulletins() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelNewsBulletins();
  LOG_END;
}
void LoggingEClientSocket::setServerLogLevel(int level) {
  LOG_START
      << __f__(level);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::setServerLogLevel(level);
  LOG_END;
}
void LoggingEClientSocket::reqAutoOpenOrders(bool bAutoBind) {
  LOG_START
      << __f__(bAutoBind);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqAutoOpenOrders(bAutoBind);
  LOG_END;
}
void LoggingEClientSocket::reqAllOpenOrders() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqAllOpenOrders();
  LOG_END;
}
void LoggingEClientSocket::reqManagedAccts() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqManagedAccts();
  LOG_END;
}
void LoggingEClientSocket::requestFA(faDataType pFaDataType) {
  LOG_START
      << __f__(pFaDataType);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::requestFA(pFaDataType);
  LOG_END;
}
void LoggingEClientSocket::replaceFA(faDataType pFaDataType,
                                     const IBString& cxml) {
  LOG_START
      << __f__(pFaDataType)
      << __f__(cxml);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::replaceFA(pFaDataType, cxml);
  LOG_END;
}
void LoggingEClientSocket::reqHistoricalData(
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
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqHistoricalData(
      id, contract, endDateTime,
      durationStr, barSizeSetting,
      whatToShow, useRTH, formatDate);
  LOG_END;
}
void LoggingEClientSocket::exerciseOptions(
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
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::exerciseOptions(
      id, contract,
      exerciseAction, exerciseQuantity,
      account, override);
  LOG_END;
}
void LoggingEClientSocket::cancelHistoricalData(TickerId tickerId ) {
  LOG_START
      << __f__(tickerId);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelHistoricalData(tickerId);
  LOG_END;
}
void LoggingEClientSocket::reqRealTimeBars(TickerId id,
                                           const Contract &contract,
                                           int barSize,
                                           const IBString &whatToShow,
                                           bool useRTH) {
  LOG_START
      << __f__(id)
      << __f__(&contract)
      << __f__(barSize)
      << __f__(whatToShow)
      << __f__(useRTH);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqRealTimeBars(id, contract, barSize,
                                      whatToShow, useRTH);
  LOG_END;
}
void LoggingEClientSocket::cancelRealTimeBars(TickerId tickerId) {
  LOG_START
      << __f__(tickerId);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelRealTimeBars(tickerId);
  LOG_END;
}
void LoggingEClientSocket::cancelScannerSubscription(int tickerId) {
  LOG_START
      << __f__(tickerId);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelScannerSubscription(tickerId);
  LOG_END;
}
void LoggingEClientSocket::reqScannerParameters() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqScannerParameters();
  LOG_END;
}
void LoggingEClientSocket::reqScannerSubscription(
    int tickerId, const ScannerSubscription &subscription) {
  LOG_START
      << __f__(tickerId)
      << __f__(&subscription);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqScannerSubscription(tickerId, subscription);
  LOG_END;
}
void LoggingEClientSocket::reqCurrentTime() {
  LOG_START;
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqCurrentTime();
  LOG_END;
}
void LoggingEClientSocket::reqFundamentalData(
    TickerId reqId, const Contract& contract, const IBString& reportType) {
  LOG_START
      << __f__(reqId)
      << __f__(&contract)
      << __f__(reportType);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::reqFundamentalData(reqId, contract, reportType);
  LOG_END;
}
void LoggingEClientSocket::cancelFundamentalData(TickerId reqId) {
  LOG_START
      << __f__(reqId);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::cancelFundamentalData(reqId);
  LOG_END;
}
void LoggingEClientSocket::calculateImpliedVolatility(
    TickerId reqId, const Contract &contract,
    double optionPrice, double underPrice) {
  LOG_START
      << __f__(reqId)
      << __f__(&contract)
      << __f__(optionPrice)
      << __f__(underPrice);
  write_lock lock(socket_write_mutex_);
  EPosixClientSocket::calculateImpliedVolatility(reqId, contract,
                                                 optionPrice, underPrice);
  LOG_END;
}

};  // namespace adapter
};  // namespace ib
