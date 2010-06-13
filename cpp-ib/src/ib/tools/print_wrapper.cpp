#include "ib/tools/internal.hpp"

#include <arpa/inet.h>
#include <errno.h>
#include <sys/select.h>

PrintWrapper::PrintWrapper()
{
}

PrintWrapper::~PrintWrapper()
{
}

///////////////////////////////////////////////////////////////////
// events
void PrintWrapper::orderStatus(OrderId orderId, const IBString &status, int filled,
                           int remaining, double avgFillPrice, int permId,
                           int parentId, double lastFillPrice, int clientId,
                           const IBString& whyHeld)
{
}

void PrintWrapper::nextValidId(OrderId orderId)
{
}

void PrintWrapper::currentTime(long time)
{
}

void PrintWrapper::error(const int id, const int errorCode,
                     const IBString errorString)
{
}

void PrintWrapper::tickPrice(TickerId tickerId, TickType field,
                         double price, int canAutoExecute)
{
}

void PrintWrapper::tickSize(TickerId tickerId, TickType field, int size)
{
}

void PrintWrapper::tickOptionComputation(
    TickerId tickerId, TickType tickType,
    double impliedVol,
    double delta, double optPrice, double pvDividend,
    double gamma, double vega, double theta, double undPrice) {}

void PrintWrapper::tickGeneric(TickerId tickerId, TickType tickType, double value)
{
}

void PrintWrapper::tickString(TickerId tickerId, TickType tickType,
                          const IBString& value)
{
}

void PrintWrapper::tickEFP(TickerId tickerId, TickType tickType,
                       double basisPoints, const IBString& formattedBasisPoints,
                       double totalDividends, int holdDays,
                       const IBString& futureExpiry, double dividendImpact,
                       double dividendsToExpiry)
{
}

void PrintWrapper::openOrder(OrderId orderId, const Contract&,
                         const Order&, const OrderState& ostate)
{
}

void PrintWrapper::openOrderEnd() {}
void PrintWrapper::winError(const IBString &str, int lastError) {}
void PrintWrapper::connectionClosed() {}
void PrintWrapper::updateAccountValue(const IBString& key, const IBString& val,
                                  const IBString& currency,
                                  const IBString& accountName)
{
}

void PrintWrapper::updatePortfolio(const Contract& contract, int position,
                               double marketPrice, double marketValue,
                               double averageCost, double unrealizedPNL,
                               double realizedPNL, const IBString& accountName)
{
}

void PrintWrapper::updateAccountTime(const IBString& timeStamp) {}
void PrintWrapper::accountDownloadEnd(const IBString& accountName) {}
void PrintWrapper::contractDetails(int reqId,
                               const ContractDetails& contractDetails)
{
}

void PrintWrapper::bondContractDetails(int reqId,
                                   const ContractDetails& contractDetails)
{
}

void PrintWrapper::contractDetailsEnd(int reqId) {}
void PrintWrapper::execDetails(int reqId, const Contract& contract,
                           const Execution& execution)
{
}

void PrintWrapper::execDetailsEnd(int reqId) {}

void PrintWrapper::updateMktDepth(TickerId id, int position,
                              int operation, int side,
                              double price, int size)
{
}

void PrintWrapper::updateMktDepthL2(TickerId id, int position,
                                IBString marketMaker, int operation,
                                int side, double price, int size)
{
}

void PrintWrapper::updateNewsBulletin(int msgId, int msgType,
                                  const IBString& newsMessage,
                                  const IBString& originExch)
{
}

void PrintWrapper::managedAccounts(const IBString& accountsList) {}
void PrintWrapper::receiveFA(faDataType pFaDataType, const IBString& cxml) {}

void PrintWrapper::historicalData(TickerId reqId, const IBString& date,
                              double open, double high,
                              double low, double close, int volume,
                              int barCount, double WAP, int hasGaps)
{
}

void PrintWrapper::scannerParameters(const IBString &xml) {}
void PrintWrapper::scannerData(int reqId, int rank,
                           const ContractDetails &contractDetails,
                         const IBString &distance,
                           const IBString &benchmark,
                           const IBString &projection,
                         const IBString &legsStr)
{
}

void PrintWrapper::scannerDataEnd(int reqId) {}

void PrintWrapper::realtimeBar(TickerId reqId, long time, double open,
                           double high, double low, double close,
                           long volume, double wap, int count)
{
}

void PrintWrapper::fundamentalData(TickerId reqId, const IBString& data) {}
void PrintWrapper::deltaNeutralValidation(int reqId, const UnderComp& underComp) {}
void PrintWrapper::tickSnapshotEnd(int reqId) {}

